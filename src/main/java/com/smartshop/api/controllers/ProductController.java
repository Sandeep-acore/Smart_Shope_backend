package com.smartshop.api.controllers;

import com.smartshop.api.models.Category;
import com.smartshop.api.models.Product;
import com.smartshop.api.payload.response.MessageResponse;
import com.smartshop.api.repositories.CategoryRepository;
import com.smartshop.api.repositories.ProductRepository;
import com.smartshop.api.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        return ResponseEntity.ok(product);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRODUCT_MANAGER')")
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("categoryId") Long categoryId,
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
            
            // Find category or throw appropriate error
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

            Product product = new Product(name, description, price, stockQuantity, category);

            // Handle image upload
            if (image != null && !image.isEmpty()) {
                // Check image type
                String contentType = image.getContentType();
                if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"))) {
                    return ResponseEntity.badRequest().body(new MessageResponse("Error: Only JPEG, JPG and PNG images are supported"));
                }
                
                String imageUrl = fileStorageService.storeFile(image, "products");
                product.setImageUrl(imageUrl);
            }

            productRepository.save(product);

            return ResponseEntity.status(HttpStatus.CREATED).body(product);
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
            // Find product or throw appropriate error
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

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
            
            if (discountPercentage < 0 || discountPercentage > 100) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Discount percentage must be between 0 and 100"));
            }

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
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                // Check file type
                String contentType = image.getContentType();
                if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"))) {
                    return ResponseEntity.badRequest().body(new MessageResponse("Error: Only JPEG, JPG and PNG images are supported"));
                }
                
                // Delete old image if exists
                if (product.getImageUrl() != null) {
                    fileStorageService.deleteFile(product.getImageUrl());
                }
                
                // Store new image
                String imageUrl = fileStorageService.storeFile(image, "products");
                product.setImageUrl(imageUrl);
            }

            productRepository.save(product);

            return ResponseEntity.ok(product);
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
    public ResponseEntity<List<Product>> searchProducts(@RequestParam(required = false) String name,
                                                       @RequestParam(required = false) String query) {
        List<Product> products;
        
        if (name != null && !name.isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCase(name);
        } else if (query != null && !query.isEmpty()) {
            products = productRepository.search(query);
        } else {
            products = productRepository.findAll();
        }
        
        return ResponseEntity.ok(products);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Product>> filterProducts(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        List<Product> products = productRepository.filter(category, minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
} 