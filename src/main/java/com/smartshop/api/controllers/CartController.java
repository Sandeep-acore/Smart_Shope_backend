package com.smartshop.api.controllers;

import com.smartshop.api.models.CartItem;
import com.smartshop.api.models.Product;
import com.smartshop.api.models.User;
import com.smartshop.api.payload.request.CartItemRequest;
import com.smartshop.api.payload.response.CartResponse;
import com.smartshop.api.payload.response.MessageResponse;
import com.smartshop.api.repositories.CartItemRepository;
import com.smartshop.api.repositories.ProductRepository;
import com.smartshop.api.repositories.UserRepository;
import com.smartshop.api.security.services.UserDetailsImpl;
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
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/cart")
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartResponse> getCart() {
        try {
            User user = getCurrentUser();
            logger.info("Fetching cart for user: {}", user.getEmail());
            
            List<CartItem> cartItems = cartItemRepository.findByUser(user);
            logger.debug("Found {} items in cart for user: {}", cartItems.size(), user.getEmail());
            
            return ResponseEntity.ok(new CartResponse(cartItems));
        } catch (Exception e) {
            logger.error("Error fetching cart", e);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addToCart(
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") Integer quantity) {
        try {
            User user = getCurrentUser();
            logger.info("Adding item to cart for user: {}", user.getEmail());
            logger.debug("Cart request details: productId={}, quantity={}", 
                    productId, quantity);
            
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> {
                        logger.error("Product not found with id: {}", productId);
                        return new EntityNotFoundException("Product not found with id: " + productId);
                    });
            
            // Check if product is in stock
            if (product.getStockQuantity() < quantity) {
                logger.warn("Product out of stock or insufficient quantity. Product ID: {}, Available: {}, Requested: {}", 
                        product.getId(), product.getStockQuantity(), quantity);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Product is out of stock or has insufficient quantity."));
            }
            
            // Check if product already exists in cart
            Optional<CartItem> existingCartItem = cartItemRepository.findByUserAndProduct(user, product);
            
            if (existingCartItem.isPresent()) {
                // Update quantity
                CartItem cartItem = existingCartItem.get();
                int newQuantity = cartItem.getQuantity() + quantity;
                logger.debug("Updating existing cart item. Old quantity: {}, New quantity: {}", 
                        cartItem.getQuantity(), newQuantity);
                cartItem.setQuantity(newQuantity);
                cartItemRepository.save(cartItem);
            } else {
                // Create new cart item
                logger.debug("Creating new cart item for product: {}", product.getName());
                CartItem cartItem = new CartItem(user, product, quantity);
                cartItemRepository.save(cartItem);
            }
            
            List<CartItem> cartItems = cartItemRepository.findByUser(user);
            logger.info("Item added to cart successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(new CartResponse(cartItems));
        } catch (Exception e) {
            logger.error("Error adding item to cart", e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Long id,
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") Integer quantity) {
        try {
            User user = getCurrentUser();
            logger.info("Updating cart item {} for user: {}", id, user.getEmail());
            
            CartItem cartItem = cartItemRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Cart item not found with id: {}", id);
                        return new EntityNotFoundException("Cart item not found with id: " + id);
                    });
            
            // Verify ownership
            if (!cartItem.getUser().getId().equals(user.getId())) {
                logger.warn("Unauthorized attempt to update cart item. User: {}, Cart Item ID: {}", 
                        user.getEmail(), id);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("You don't have permission to update this cart item."));
            }
            
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> {
                        logger.error("Product not found with id: {}", productId);
                        return new EntityNotFoundException("Product not found with id: " + productId);
                    });
            
            // Check if product is in stock
            if (product.getStockQuantity() < quantity) {
                logger.warn("Product out of stock or insufficient quantity. Product ID: {}, Available: {}, Requested: {}", 
                        product.getId(), product.getStockQuantity(), quantity);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Product is out of stock or has insufficient quantity."));
            }
            
            logger.debug("Updating cart item. Old: [productId={}, quantity={}], New: [productId={}, quantity={}]",
                    cartItem.getProduct().getId(), cartItem.getQuantity(),
                    product.getId(), quantity);
            
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
            
            List<CartItem> cartItems = cartItemRepository.findByUser(user);
            logger.info("Cart item updated successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(new CartResponse(cartItems));
        } catch (Exception e) {
            logger.error("Error updating cart item", e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> removeFromCart(@PathVariable Long id) {
        try {
            User user = getCurrentUser();
            logger.info("Removing item {} from cart for user: {}", id, user.getEmail());
            
            CartItem cartItem = cartItemRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Cart item not found with id: {}", id);
                        return new EntityNotFoundException("Cart item not found with id: " + id);
                    });
            
            // Verify ownership
            if (!cartItem.getUser().getId().equals(user.getId())) {
                logger.warn("Unauthorized attempt to remove cart item. User: {}, Cart Item ID: {}", 
                        user.getEmail(), id);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("You don't have permission to delete this cart item."));
            }
            
            cartItemRepository.delete(cartItem);
            logger.debug("Cart item deleted: {}", id);
            
            List<CartItem> cartItems = cartItemRepository.findByUser(user);
            logger.info("Item removed from cart successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(new CartResponse(cartItems));
        } catch (Exception e) {
            logger.error("Error removing item from cart", e);
            throw e;
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ResponseEntity<?> clearCart() {
        try {
            User user = getCurrentUser();
            logger.info("Clearing cart for user: {}", user.getEmail());
            
            cartItemRepository.deleteByUser(user);
            logger.info("Cart cleared successfully for user: {}", user.getEmail());
            
            return ResponseEntity.ok(new MessageResponse("Cart cleared successfully."));
        } catch (Exception e) {
            logger.error("Error clearing cart", e);
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