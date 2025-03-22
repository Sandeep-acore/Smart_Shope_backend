package com.smartshop.api.controllers;

import com.smartshop.api.models.Category;
import com.smartshop.api.models.Product;
import com.smartshop.api.payload.response.MessageResponse;
import com.smartshop.api.payload.response.ProductResponse;
import com.smartshop.api.repositories.CategoryRepository;
import com.smartshop.api.repositories.ProductRepository;
import com.smartshop.api.services.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        
        List<ProductResponse> responses = products.stream()
            .map(product -> ProductResponse.fromProduct(product, baseUrl))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
                
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return ResponseEntity.ok(ProductResponse.fromProduct(product, baseUrl));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "discountPercentage", required = false, defaultValue = "0") Integer discountPercentage,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        try {
            // Validate required fields
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Product name is required"));
            }
            
            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Product description is required"));
            }
            
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Product price must be a positive number"));
            }
            
            if (stockQuantity == null || stockQuantity < 0) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Stock quantity must be a non-negative integer"));
            }
            
            // Validate discount percentage
            if (discountPercentage < 0 || discountPercentage > 100) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Discount percentage must be between 0 and 100"));
            }
            
            // Find category or throw appropriate error
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

            Product product = new Product(name, description, price, stockQuantity, category);
            product.setDiscountPercentage(discountPercentage);

            // Handle image upload
            if (image != null && !image.isEmpty()) {
                try {
                    // Check image type
                    String contentType = image.getContentType();
                    if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"))) {
                        return ResponseEntity.badRequest().body(new MessageResponse("Error: Only JPEG, JPG and PNG images are supported"));
                    }
                    
                    String imageUrl = fileStorageService.storeFile(image, "products");
                    if (imageUrl == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse("Error: Failed to save image file"));
                    }
                    product.setImageUrl(imageUrl);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse("Error saving product image: " + e.getMessage()));
                }
            }

            Product savedProduct = productRepository.save(product);
            
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.fromProduct(savedProduct, baseUrl));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "discountPercentage", required = false, defaultValue = "0") Integer discountPercentage,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        try {
            // Find the product
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

            // Update basic properties
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStockQuantity(stockQuantity);
            product.setDiscountPercentage(discountPercentage);

            // Update category if provided
            if (categoryId != null) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
                product.setCategory(category);
            }

            // Update image if provided
            if (image != null && !image.isEmpty()) {
                try {
                    // Check image type
                    String contentType = image.getContentType();
                    if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"))) {
                        return ResponseEntity.badRequest().body(new MessageResponse("Error: Only JPEG, JPG and PNG images are supported"));
                    }
                    
                    // Delete old image if exists
                    if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                        boolean deleted = fileStorageService.deleteFile(product.getImageUrl());
                        if (!deleted) {
                            // Just log warning and continue - don't stop the update
                            logger.warn("Could not delete old product image: {}", product.getImageUrl());
                        }
                    }
                    
                    // Save new image
                    String imageUrl = fileStorageService.storeFile(image, "products");
                    if (imageUrl == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse("Error: Failed to save new image file"));
                    }
                    product.setImageUrl(imageUrl);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse("Error updating product image: " + e.getMessage()));
                }
            }

            Product updatedProduct = productRepository.save(product);
            
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return ResponseEntity.ok(ProductResponse.fromProduct(updatedProduct, baseUrl));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        // Delete product image if exists
        if (product.getImageUrl() != null) {
            fileStorageService.deleteFile(product.getImageUrl());
        }

        productRepository.delete(product);

        return ResponseEntity.ok(new MessageResponse("Product deleted successfully!"));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam(required = false) String name,
                                                       @RequestParam(required = false) String query) {
        List<Product> products;
        
        if (name != null && !name.isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCase(name);
        } else if (query != null && !query.isEmpty()) {
            products = productRepository.search(query);
        } else {
            products = productRepository.findAll();
        }
        
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        List<ProductResponse> responses = products.stream()
            .map(product -> ProductResponse.fromProduct(product, baseUrl))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ProductResponse>> filterProducts(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        List<Product> products = productRepository.filter(category, minPrice, maxPrice);
        
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        List<ProductResponse> responses = products.stream()
            .map(product -> ProductResponse.fromProduct(product, baseUrl))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(responses);
    }
} 