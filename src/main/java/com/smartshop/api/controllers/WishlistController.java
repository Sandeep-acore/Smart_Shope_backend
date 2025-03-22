package com.smartshop.api.controllers;

import com.smartshop.api.models.Product;
import com.smartshop.api.models.User;
import com.smartshop.api.models.WishlistItem;
import com.smartshop.api.payload.response.MessageResponse;
import com.smartshop.api.payload.response.WishlistResponse;
import com.smartshop.api.repositories.ProductRepository;
import com.smartshop.api.repositories.UserRepository;
import com.smartshop.api.repositories.WishlistItemRepository;
import com.smartshop.api.security.services.UserDetailsImpl;
import com.smartshop.api.services.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/wishlist")
public class WishlistController {
    private static final Logger logger = LoggerFactory.getLogger(WishlistController.class);

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WishlistResponse> getWishlist() {
        try {
            User user = getCurrentUser();
            logger.info("Fetching wishlist for user: {}", user.getEmail());
            
            List<WishlistItem> wishlistItems = wishlistItemRepository.findByUser(user);
            logger.debug("Found {} items in wishlist for user: {}", wishlistItems.size(), user.getEmail());
            
            return ResponseEntity.ok(new WishlistResponse(wishlistItems, fileStorageService));
        } catch (Exception e) {
            logger.error("Error fetching wishlist", e);
            throw e;
        }
    }

    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addToWishlist(@PathVariable Long productId) {
        try {
            User user = getCurrentUser();
            logger.info("Adding product {} to wishlist for user: {}", productId, user.getEmail());
            
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> {
                        logger.error("Product not found with id: {}", productId);
                        return new EntityNotFoundException("Product not found with id: " + productId);
                    });
            
            // Check if product already exists in wishlist
            if (wishlistItemRepository.existsByUserAndProduct(user, product)) {
                logger.warn("Product already exists in wishlist. User: {}, Product ID: {}", 
                        user.getEmail(), productId);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Product already exists in wishlist."));
            }
            
            // Create new wishlist item
            logger.debug("Creating new wishlist item for product: {}", product.getName());
            WishlistItem wishlistItem = new WishlistItem(user, product);
            wishlistItemRepository.save(wishlistItem);
            
            List<WishlistItem> wishlistItems = wishlistItemRepository.findByUser(user);
            logger.info("Product added to wishlist successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(new WishlistResponse(wishlistItems, fileStorageService));
        } catch (Exception e) {
            logger.error("Error adding product to wishlist", e);
            throw e;
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ResponseEntity<?> removeFromWishlist(@PathVariable Long productId) {
        try {
            User user = getCurrentUser();
            logger.info("Removing product {} from wishlist for user: {}", productId, user.getEmail());
            
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> {
                        logger.error("Product not found with id: {}", productId);
                        return new EntityNotFoundException("Product not found with id: " + productId);
                    });
            
            wishlistItemRepository.deleteByUserAndProduct(user, product);
            
            List<WishlistItem> wishlistItems = wishlistItemRepository.findByUser(user);
            logger.info("Product removed from wishlist successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(new WishlistResponse(wishlistItems, fileStorageService));
        } catch (Exception e) {
            logger.error("Error removing product from wishlist", e);
            throw e;
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> clearWishlist() {
        try {
            User user = getCurrentUser();
            logger.info("Clearing wishlist for user: {}", user.getEmail());
            
            List<WishlistItem> wishlistItems = wishlistItemRepository.findByUser(user);
            wishlistItemRepository.deleteAll(wishlistItems);
            
            logger.info("Wishlist cleared successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(new WishlistResponse(wishlistItemRepository.findByUser(user), fileStorageService));
        } catch (Exception e) {
            logger.error("Error clearing wishlist", e);
            throw e;
        }
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
} 