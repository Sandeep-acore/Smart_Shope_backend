package com.smartshop.api.config;

import com.smartshop.api.models.ERole;
import com.smartshop.api.models.Role;
import com.smartshop.api.models.User;
import com.smartshop.api.repositories.RoleRepository;
import com.smartshop.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        initRoles();
        
        // Create admin user if it doesn't exist
        createAdminUser();
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(ERole.ROLE_USER));
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
            roleRepository.save(new Role(ERole.ROLE_PRODUCT_MANAGER));
            roleRepository.save(new Role(ERole.ROLE_DELIVERY_PARTNER));
            
            System.out.println("Roles initialized successfully.");
        }
    }

    private void createAdminUser() {
        if (!userRepository.existsByEmail("admin@smartshop.com")) {
            User adminUser = new User(
                    "Admin User",
                    "admin@smartshop.com",
                    passwordEncoder.encode("admin123"),
                    "9876543210"
            );

            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Admin Role not found."));
            roles.add(adminRole);
            adminUser.setRoles(roles);

            userRepository.save(adminUser);
            
            System.out.println("Admin user created successfully.");
        }
    }
} 