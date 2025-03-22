package com.smartshop.api.controllers;

import com.smartshop.api.models.ERole;
import com.smartshop.api.models.Role;
import com.smartshop.api.models.User;
import com.smartshop.api.payload.request.LoginRequest;
import com.smartshop.api.payload.request.ResetPasswordRequest;
import com.smartshop.api.payload.request.SignupRequest;
import com.smartshop.api.payload.request.VerifyOtpRequest;
import com.smartshop.api.payload.request.AddressUpdateRequest;
import com.smartshop.api.payload.response.JwtResponse;
import com.smartshop.api.payload.response.MessageResponse;
import com.smartshop.api.payload.response.UserProfileResponse;
import com.smartshop.api.repositories.RoleRepository;
import com.smartshop.api.repositories.UserRepository;
import com.smartshop.api.security.jwt.JwtUtils;
import com.smartshop.api.security.services.UserDetailsImpl;
import com.smartshop.api.services.EmailService;
import com.smartshop.api.services.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    EmailService emailService;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Attempting authentication for user: {}", loginRequest.getEmail());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            logger.info("User authenticated successfully: {}", loginRequest.getEmail());
            JwtResponse response = new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getName(),
                    userDetails.getEmail(),
                    userDetails.getPhone(),
                    userDetails.getProfileImage(),
                    roles);
            
            // Handle profile image URL
            if (userDetails.getProfileImage() != null && !userDetails.getProfileImage().isEmpty()) {
                response.setImageUrl(fileStorageService.getFileUrl(userDetails.getProfileImage()));
            }
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            logger.error("Authentication failed for user: {}", loginRequest.getEmail(), e);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponse.error("Invalid email or password"));
        } catch (Exception e) {
            logger.error("Unexpected error during authentication", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("An unexpected error occurred"));
        }
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phone") String phone,
            @RequestParam(value = "roles", required = false) Set<String> strRoles,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            logger.info("Processing registration request for user: {}", email);

            if (userRepository.existsByEmail(email)) {
                logger.warn("Registration failed - Email already exists: {}", email);
                return ResponseEntity
                        .badRequest()
                        .body(MessageResponse.error("Email is already in use!"));
            }

            // Create new user's account
            User user = new User(name, email, encoder.encode(password), phone);

            // Handle profile image if provided
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                // Check file type
                String contentType = profileImage.getContentType();
                if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"))) {
                    return ResponseEntity.badRequest().body(MessageResponse.error("Only JPEG, JPG and PNG images are supported."));
                }
                
                // Store profile image
                String profileImagePath = fileStorageService.storeFile(profileImage, "profiles");
                    if (profileImagePath == null) {
                        logger.error("Failed to store profile image for user: {}", email);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(MessageResponse.error("Failed to upload profile image. Please try again."));
                    }
                user.setProfileImage(profileImagePath);
                } catch (Exception e) {
                    logger.error("Error handling profile image upload: {}", e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(MessageResponse.error("Error uploading profile image: " + e.getMessage()));
                }
            }

            Set<Role> roles = new HashSet<>();

            if (strRoles == null) {
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> {
                            logger.error("Error: User Role not found in database");
                            return new RuntimeException("Error: Role is not found.");
                        });
                roles.add(userRole);
            } else {
                strRoles.forEach(role -> {
                    logger.debug("Processing role: {}", role);
                    switch (role) {
                        case "admin":
                            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                    .orElseThrow(() -> {
                                        logger.error("Error: Admin Role not found in database");
                                        return new RuntimeException("Error: Role is not found.");
                                    });
                            roles.add(adminRole);
                            break;
                        case "product_manager":
                            Role pmRole = roleRepository.findByName(ERole.ROLE_PRODUCT_MANAGER)
                                    .orElseThrow(() -> {
                                        logger.error("Error: Product Manager Role not found in database");
                                        return new RuntimeException("Error: Role is not found.");
                                    });
                            roles.add(pmRole);
                            break;
                        case "delivery_partner":
                            Role dpRole = roleRepository.findByName(ERole.ROLE_DELIVERY_PARTNER)
                                    .orElseThrow(() -> {
                                        logger.error("Error: Delivery Partner Role not found in database");
                                        return new RuntimeException("Error: Role is not found.");
                                    });
                            roles.add(dpRole);
                            break;
                        default:
                            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                    .orElseThrow(() -> {
                                        logger.error("Error: User Role not found in database");
                                        return new RuntimeException("Error: Role is not found.");
                                    });
                            roles.add(userRole);
                    }
                });
            }

            user.setRoles(roles);
            logger.debug("Saving user with roles: {}", roles);
            
            try {
                User savedUser = userRepository.save(user);
                logger.info("User saved successfully with ID: {}", savedUser.getId());
            } catch (Exception e) {
                logger.error("Failed to save user to database: {}", e.getMessage(), e);
                
                // If we uploaded a profile image, try to delete it since the user wasn't saved
                if (user.getProfileImage() != null) {
                    try {
                        boolean deleted = fileStorageService.deleteFile(user.getProfileImage());
                        if (deleted) {
                            logger.info("Successfully deleted profile image after user save failure");
                        }
                    } catch (Exception ex) {
                        logger.error("Failed to delete profile image after user save failure: {}", ex.getMessage());
                    }
                }
                
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(MessageResponse.error("Failed to register user. Please try again later."));
            }

            // Authenticate the user and generate JWT token
            try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> userRoles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

                logger.info("User registered and authenticated successfully: {}", email);
                JwtResponse response = new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getName(),
                    userDetails.getEmail(),
                    userDetails.getPhone(),
                    userDetails.getProfileImage(),
                        userRoles);
                
                // Handle profile image URL
                if (userDetails.getProfileImage() != null && !userDetails.getProfileImage().isEmpty()) {
                    response.setImageUrl(fileStorageService.getFileUrl(userDetails.getProfileImage()));
                }
                
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } catch (Exception e) {
                logger.error("User registered but authentication failed: {}", email, e);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(MessageResponse.success("User registered successfully! Please log in."));
            }
        } catch (Exception e) {
            logger.error("Error during user registration: {}", email, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register-json")
    public ResponseEntity<?> registerUserJson(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            logger.info("Processing registration request for user: {}", signUpRequest.getEmail());

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                logger.warn("Registration failed - Email already exists: {}", signUpRequest.getEmail());
                return ResponseEntity
                        .badRequest()
                        .body(MessageResponse.error("Email is already in use!"));
            }

            // Create new user's account
            User user = new User(
                    signUpRequest.getName(),
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()),
                    signUpRequest.getPhone());

            Set<String> strRoles = signUpRequest.getRoles();
            Set<Role> roles = new HashSet<>();

            if (strRoles == null) {
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> {
                            logger.error("Error: User Role not found in database");
                            return new RuntimeException("Error: Role is not found.");
                        });
                roles.add(userRole);
            } else {
                strRoles.forEach(role -> {
                    logger.debug("Processing role: {}", role);
                    switch (role) {
                        case "admin":
                            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                    .orElseThrow(() -> {
                                        logger.error("Error: Admin Role not found in database");
                                        return new RuntimeException("Error: Role is not found.");
                                    });
                            roles.add(adminRole);
                            break;
                        case "product_manager":
                            Role pmRole = roleRepository.findByName(ERole.ROLE_PRODUCT_MANAGER)
                                    .orElseThrow(() -> {
                                        logger.error("Error: Product Manager Role not found in database");
                                        return new RuntimeException("Error: Role is not found.");
                                    });
                            roles.add(pmRole);
                            break;
                        case "delivery_partner":
                            Role dpRole = roleRepository.findByName(ERole.ROLE_DELIVERY_PARTNER)
                                    .orElseThrow(() -> {
                                        logger.error("Error: Delivery Partner Role not found in database");
                                        return new RuntimeException("Error: Role is not found.");
                                    });
                            roles.add(dpRole);
                            break;
                        default:
                            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                    .orElseThrow(() -> {
                                        logger.error("Error: User Role not found in database");
                                        return new RuntimeException("Error: Role is not found.");
                                    });
                            roles.add(userRole);
                    }
                });
            }

            user.setRoles(roles);
            logger.debug("Saving user with roles: {}", roles);
            userRepository.save(user);

            // Authenticate the user and generate JWT token
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signUpRequest.getEmail(), signUpRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> userRoles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            logger.info("User registered successfully: {}", signUpRequest.getEmail());
            
            JwtResponse response = new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getName(),
                    userDetails.getEmail(),
                    userDetails.getPhone(),
                    userDetails.getProfileImage(),
                    userRoles);
            
            // Handle profile image URL
            if (userDetails.getProfileImage() != null && !userDetails.getProfileImage().isEmpty()) {
                response.setImageUrl(fileStorageService.getFileUrl(userDetails.getProfileImage()));
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error during user registration: {}", signUpRequest.getEmail(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            logger.info("Processing password reset request for email: {}", request.getEmail());
            
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (!userOptional.isPresent()) {
                logger.warn("Password reset failed - User not found: {}", request.getEmail());
                return ResponseEntity.badRequest().body(MessageResponse.error("User not found with this email."));
            }

            User user = userOptional.get();
            
            // Generate 6-digit OTP
            String otp = String.format("%06d", new Random().nextInt(999999));
            user.setOtp(otp);
            user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10)); // OTP valid for 10 minutes
            userRepository.save(user);

            // Send OTP via email
            emailService.sendOtpEmail(user.getEmail(), otp);

            logger.info("Password reset OTP sent to: {}", request.getEmail());
            return ResponseEntity.ok(MessageResponse.success("Password reset OTP has been sent to your email."));
        } catch (Exception e) {
            logger.error("Error during password reset request: {}", request.getEmail(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            logger.info("Processing OTP verification for email: {}", request.getEmail());
            
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (!userOptional.isPresent()) {
                logger.warn("OTP verification failed - User not found: {}", request.getEmail());
                return ResponseEntity.badRequest().body(MessageResponse.error("User not found with this email."));
            }

            User user = userOptional.get();
            
            // Verify OTP
            if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
                logger.warn("OTP verification failed - Invalid OTP for user: {}", request.getEmail());
                return ResponseEntity.badRequest().body(MessageResponse.error("Invalid OTP."));
            }
            
            // Check if OTP is expired
            if (user.getOtpExpiryTime() == null || LocalDateTime.now().isAfter(user.getOtpExpiryTime())) {
                logger.warn("OTP verification failed - Expired OTP for user: {}", request.getEmail());
                return ResponseEntity.badRequest().body(MessageResponse.error("OTP has expired."));
            }
            
            // Update password
            user.setPassword(encoder.encode(request.getNewPassword()));
            user.setOtp(null);
            user.setOtpExpiryTime(null);
            userRepository.save(user);

            logger.info("Password reset successful for user: {}", request.getEmail());
            return ResponseEntity.ok(MessageResponse.success("Password has been reset successfully."));
        } catch (Exception e) {
            logger.error("Error during OTP verification: {}", request.getEmail(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('PRODUCT_MANAGER') or hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<?> getUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String email = userDetails.getEmail();
            
            logger.info("Fetching profile for user: {}", email);
            
            // Use eager fetching with join to load roles
            User user = userRepository.findByEmailWithRoles(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            UserProfileResponse profile = new UserProfileResponse();
            profile.setId(user.getId());
            profile.setName(user.getName());
            profile.setEmail(user.getEmail());
            profile.setPhone(user.getPhone());
            profile.setImageRelativePath(user.getProfileImage());
            
            // Handle profile image URL
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                profile.setImageUrl(fileStorageService.getFileUrl(user.getProfileImage()));
            }
            
            profile.setAddressLine1(user.getAddressLine1());
            profile.setAddressLine2(user.getAddressLine2());
            profile.setCity(user.getCity());
            profile.setState(user.getState());
            profile.setPostalCode(user.getPostalCode());
            profile.setCountry(user.getCountry());
            profile.setCreatedAt(user.getCreatedAt());
            profile.setUpdatedAt(user.getUpdatedAt());
            
            // Convert roles to strings - now safe since roles are eagerly loaded
            Set<String> roleStrings = user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toSet());
            profile.setRoles(roleStrings);
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error fetching user profile", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error fetching user profile: " + e.getMessage()));
        }
    }

    @PutMapping("/update-profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('PRODUCT_MANAGER') or hasRole('DELIVERY_PARTNER')")
    @Transactional
    public ResponseEntity<?> updateUserProfile(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String addressLine1,
            @RequestParam(required = false) String addressLine2,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String postalCode,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) MultipartFile profileImage) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            logger.info("Updating profile for user: {}", userDetails.getEmail());
            
            Optional<User> userOptional = userRepository.findById(userDetails.getId());
            if (!userOptional.isPresent()) {
                logger.error("Profile update failed - User not found: {}", userDetails.getId());
                return ResponseEntity.badRequest().body(MessageResponse.error("User not found."));
            }
            
            User user = userOptional.get();
            
            // Update user details if provided, otherwise keep existing values
            if (name != null && !name.trim().isEmpty()) {
                logger.debug("Updating name for user {}: {}", user.getEmail(), name);
                user.setName(name);
            }
            
            if (phone != null && !phone.trim().isEmpty()) {
                logger.debug("Updating phone for user {}: {}", user.getEmail(), phone);
                user.setPhone(phone);
            }
            
            // Update address fields if provided, otherwise keep existing values
            // Note: We're treating empty strings as valid input to clear fields
            if (addressLine1 != null) {
                logger.debug("Updating addressLine1 for user {}: {}", user.getEmail(), addressLine1);
                user.setAddressLine1(addressLine1);
            }
            
            if (addressLine2 != null) {
                logger.debug("Updating addressLine2 for user {}: {}", user.getEmail(), addressLine2);
                user.setAddressLine2(addressLine2);
            }
            
            if (city != null) {
                logger.debug("Updating city for user {}: {}", user.getEmail(), city);
                user.setCity(city);
            }
            
            if (state != null) {
                logger.debug("Updating state for user {}: {}", user.getEmail(), state);
                user.setState(state);
            }
            
            if (postalCode != null) {
                logger.debug("Updating postalCode for user {}: {}", user.getEmail(), postalCode);
                user.setPostalCode(postalCode);
            }
            
            if (country != null) {
                logger.debug("Updating country for user {}: {}", user.getEmail(), country);
                user.setCountry(country);
            }
            
            // Handle profile image upload
            String profileImagePath = null;
            String oldProfileImagePath = null;
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                logger.debug("Updating profile image for user: {}", user.getEmail());
                    
                    // Check file type
                    String contentType = profileImage.getContentType();
                    if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"))) {
                        return ResponseEntity.badRequest().body(MessageResponse.error("Only JPEG, JPG and PNG images are supported."));
                    }
                    
                    // Save old profile image path for deletion after successful update
                    oldProfileImagePath = user.getProfileImage();
                
                // Store new profile image
                    profileImagePath = fileStorageService.storeFile(profileImage, "profiles");
                    if (profileImagePath == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(MessageResponse.error("Failed to upload profile image. Please try again."));
                    }
                user.setProfileImage(profileImagePath);
                } catch (Exception e) {
                    logger.error("Error processing profile image: {}", e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(MessageResponse.error("Error processing profile image: " + e.getMessage()));
                }
            }
            
            try {
            userRepository.save(user);
                
                // Delete old profile image if it was replaced
                if (oldProfileImagePath != null && profileImagePath != null) {
                    try {
                        fileStorageService.deleteFile(oldProfileImagePath);
                    } catch (Exception ex) {
                        logger.warn("Could not delete old profile image: {}", oldProfileImagePath, ex);
                        // Continue since this isn't critical
                    }
                }
            
            // Create a response with the updated user profile
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("phone", user.getPhone());
            response.put("profileImage", user.getProfileImage());
                
                // Add full URL for profile image
                if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                    response.put("imageUrl", fileStorageService.getFileUrl(user.getProfileImage()));
                }
            
            // Add address information
            Map<String, Object> address = new HashMap<>();
            address.put("addressLine1", user.getAddressLine1());
            address.put("addressLine2", user.getAddressLine2());
            address.put("city", user.getCity());
            address.put("state", user.getState());
            address.put("postalCode", user.getPostalCode());
            address.put("country", user.getCountry());
            response.put("address", address);
            
            response.put("roles", user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList()));
            response.put("message", "Profile updated successfully");
            
            logger.info("Profile updated successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.error("Error saving user data: {}", e.getMessage(), e);
                
                // If we uploaded a new image but couldn't save the user, delete the new image
                if (profileImagePath != null) {
                    try {
                        fileStorageService.deleteFile(profileImagePath);
                    } catch (Exception ex) {
                        logger.error("Failed to clean up profile image after user save failure: {}", ex.getMessage());
                    }
                }
                
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(MessageResponse.error("Failed to update profile: " + e.getMessage()));
            }
        } catch (Exception e) {
            logger.error("Error updating user profile", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error updating user profile: " + e.getMessage()));
        }
    }

    @PutMapping("/update-address")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('PRODUCT_MANAGER') or hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<?> updateUserAddress(@Valid @RequestBody AddressUpdateRequest addressRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            logger.info("Updating address for user: {}", userDetails.getEmail());
            
            Optional<User> userOptional = userRepository.findById(userDetails.getId());
            if (!userOptional.isPresent()) {
                logger.error("Address update failed - User not found: {}", userDetails.getId());
                return ResponseEntity.badRequest().body(MessageResponse.error("Error: User not found."));
            }
            
            User user = userOptional.get();
            
            // Update address fields
            user.setAddressLine1(addressRequest.getAddressLine1());
            user.setAddressLine2(addressRequest.getAddressLine2());
            user.setCity(addressRequest.getCity());
            user.setState(addressRequest.getState());
            user.setPostalCode(addressRequest.getPostalCode());
            user.setCountry(addressRequest.getCountry());
            
            userRepository.save(user);
            
            // Create response with updated address
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> address = new HashMap<>();
            address.put("addressLine1", user.getAddressLine1());
            address.put("addressLine2", user.getAddressLine2());
            address.put("city", user.getCity());
            address.put("state", user.getState());
            address.put("postalCode", user.getPostalCode());
            address.put("country", user.getCountry());
            response.put("address", address);
            response.put("message", "Address updated successfully");
            
            logger.info("Address updated successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating user address", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/upload-profile-image")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('PRODUCT_MANAGER') or hasRole('DELIVERY_PARTNER')")
    @Transactional
    public ResponseEntity<?> uploadProfileImage(@RequestParam("profileImage") MultipartFile profileImage) {
        try {
            // Get current user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            logger.info("Processing profile image upload for user ID: {}", userId);
            
            if (profileImage == null || profileImage.isEmpty()) {
                logger.warn("No profile image provided for user ID: {}", userId);
                return ResponseEntity.badRequest().body(MessageResponse.error("No profile image provided"));
            }
            
            // Check file type
            String contentType = profileImage.getContentType();
            if (contentType == null) {
                logger.warn("Invalid content type for profile image from user ID: {}", userId);
                return ResponseEntity.badRequest().body(MessageResponse.error("Invalid file type. Content type is null."));
            }
            
            if (!(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"))) {
                logger.warn("Unsupported content type for profile image: {} from user ID: {}", contentType, userId);
                return ResponseEntity.badRequest().body(MessageResponse.error("Only JPEG, JPG and PNG images are supported."));
            }
            
            logger.info("Profile image validated for user ID: {}, size: {}, content type: {}", userId, profileImage.getSize(), contentType);
            
            // Get user from database
            Optional<User> userOptional = userRepository.findById(userId);
            if (!userOptional.isPresent()) {
                logger.error("User not found for ID: {} during profile image upload", userId);
                return ResponseEntity.badRequest().body(MessageResponse.error("User not found"));
            }
            
            User user = userOptional.get();
            logger.info("Found user: {} for profile image upload", user.getEmail());
            
            // Store old profile image path for deletion after successful update
            String oldProfileImagePath = user.getProfileImage();
            logger.debug("Old profile image path: {}", oldProfileImagePath);
            
            try {
                // Store profile image in a separate operation outside the main transaction
                logger.info("Storing profile image for user: {}", user.getEmail());
                String profileImagePath = fileStorageService.storeFile(profileImage, "profiles");
                
                if (profileImagePath == null) {
                    logger.error("Failed to store profile image for user: {}", user.getEmail());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(MessageResponse.error("Failed to upload profile image. Please try again."));
                }
                
                logger.info("Successfully stored profile image at path: {} for user: {}", profileImagePath, user.getEmail());
                
                // Update user with new profile image path
            user.setProfileImage(profileImagePath);
            userRepository.save(user);
                logger.info("Updated user profile with new image path for user: {}", user.getEmail());
                
                // Delete old profile image if it exists (after successful update)
                if (oldProfileImagePath != null && !oldProfileImagePath.isEmpty()) {
                    try {
                        boolean deleted = fileStorageService.deleteFile(oldProfileImagePath);
                        if (deleted) {
                            logger.info("Successfully deleted old profile image: {}", oldProfileImagePath);
                        } else {
                            logger.warn("Failed to delete old profile image: {}", oldProfileImagePath);
                        }
                    } catch (Exception ex) {
                        logger.warn("Error deleting old profile image: {}", oldProfileImagePath, ex);
                        // Continue since this isn't critical
                    }
                }
                
                // Generate full URL for the profile image
                String fileUrl = fileStorageService.getFileUrl(profileImagePath);
                
                // Return success response with profile image URL
            Map<String, Object> response = new HashMap<>();
            response.put("profileImage", profileImagePath);
                response.put("imageUrl", fileUrl);
            response.put("message", "Profile image uploaded successfully");
            
                logger.info("Profile image upload completed successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("Error processing profile image for user: {}", user.getEmail(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(MessageResponse.error("Failed to upload profile image. Please try again."));
            }
        } catch (Exception e) {
            logger.error("Unexpected error uploading profile image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("An unexpected error occurred while uploading profile image."));
        }
    }

    @PostMapping("/add-address")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('PRODUCT_MANAGER') or hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<?> addUserAddress(@Valid @RequestBody AddressUpdateRequest addressRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            logger.info("Adding address for user: {}", userDetails.getEmail());
            
            Optional<User> userOptional = userRepository.findById(userDetails.getId());
            if (!userOptional.isPresent()) {
                logger.error("Address add failed - User not found: {}", userDetails.getId());
                return ResponseEntity.badRequest().body(MessageResponse.error("Error: User not found."));
            }
            
            User user = userOptional.get();
            
            // Add address fields
            user.setAddressLine1(addressRequest.getAddressLine1());
            user.setAddressLine2(addressRequest.getAddressLine2());
            user.setCity(addressRequest.getCity());
            user.setState(addressRequest.getState());
            user.setPostalCode(addressRequest.getPostalCode());
            user.setCountry(addressRequest.getCountry());
            
            userRepository.save(user);
            
            // Create response with added address
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> address = new HashMap<>();
            address.put("addressLine1", user.getAddressLine1());
            address.put("addressLine2", user.getAddressLine2());
            address.put("city", user.getCity());
            address.put("state", user.getState());
            address.put("postalCode", user.getPostalCode());
            address.put("country", user.getCountry());
            response.put("address", address);
            response.put("message", "Address added successfully");
            
            logger.info("Address added successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error adding user address", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/profile-image/{filename:.+}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String filename) {
        try {
            // Load file as Resource
            Resource resource = fileStorageService.loadFileAsResource("profiles/" + filename);
            
            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                logger.info("Could not determine file type.");
            }
            
            // Fallback to the default content type if type could not be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error retrieving profile image: {}", filename, e);
            return ResponseEntity.notFound().build();
        }
    }
} 