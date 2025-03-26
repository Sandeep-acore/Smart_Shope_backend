package com.smartshop.api.controllers;

import com.smartshop.api.models.Category;
import com.smartshop.api.models.Product;
import com.smartshop.api.payload.response.CategoryResponse;
import com.smartshop.api.payload.response.MessageResponse;
import com.smartshop.api.repositories.CategoryRepository;
import com.smartshop.api.repositories.ProductRepository;
import com.smartshop.api.services.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/categories")
public class CategoryController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private FileStorageService fileStorageService;

    @Value("${app.url:}")
    private String appUrl;

    private String getBaseUrl() {
        if (appUrl != null && !appUrl.isEmpty()) {
            return appUrl;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        
        List<CategoryResponse> responses = categories.stream()
            .map(category -> CategoryResponse.fromCategory(category, baseUrl))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
            
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return ResponseEntity.ok(CategoryResponse.fromCategory(category, baseUrl));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> createCategory(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            // Validate name
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Name is required"));
            }

            // Check if category with same name already exists
            if (categoryRepository.existsByName(name)) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: A category with name '" + name + "' already exists"));
            }

            Category category = new Category();
            category.setName(name);
            category.setDescription(description);

            // Handle image upload
            if (image != null && !image.isEmpty()) {
                try {
                    // Check image type
                    String contentType = image.getContentType();
                    if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"))) {
                        return ResponseEntity.badRequest().body(new MessageResponse("Error: Only JPEG, JPG and PNG images are supported"));
                    }
                    
                    String imageUrl = fileStorageService.storeFile(image, "categories");
                    if (imageUrl == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse("Error: Failed to save image file"));
                    }
                    category.setImageUrl(imageUrl);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse("Error saving category image: " + e.getMessage()));
                }
            }

            Category savedCategory = categoryRepository.save(category);
            
            String baseUrl = getBaseUrl();
            return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.fromCategory(savedCategory, baseUrl));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("image") MultipartFile image) {
        
        try {
            logger.info("Updating category with id: {}", id);
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(MessageResponse.error("Category name is required"));
            }
            
            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(MessageResponse.error("Category description is required"));
            }
            
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest().body(MessageResponse.error("Category image is required"));
            }
            
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
            
            // Check if the new name already exists (and it's not the current category)
            if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
                logger.warn("Category update failed - Name already exists: {}", name);
                return ResponseEntity
                        .badRequest()
                        .body(MessageResponse.error("Category name already exists!"));
            }
            
            logger.debug("Updating name for category {}: {}", category.getName(), name);
            category.setName(name);
            
            logger.debug("Updating description for category {}", category.getName());
            category.setDescription(description);
            
            // Check file type
            String contentType = image.getContentType();
            if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"))) {
                return ResponseEntity.badRequest().body(MessageResponse.error("Only JPEG, JPG and PNG images are supported."));
            }
            
            // Delete old image if exists
            if (category.getImageUrl() != null) {
                fileStorageService.deleteFile(category.getImageUrl());
            }
            
            // Store new image
            String imagePath = fileStorageService.storeFile(image, "categories");
            category.setImageUrl(imagePath);
            
            categoryRepository.save(category);
            logger.info("Category updated successfully: {}", category.getName());
            
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return ResponseEntity.ok(CategoryResponse.fromCategory(category, baseUrl));
        } catch (Exception e) {
            logger.error("Error updating category with id: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            logger.info("Deleting category with id: {}", id);
            
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
            
            // Delete category image if exists
            if (category.getImageUrl() != null) {
                fileStorageService.deleteFile(category.getImageUrl());
            }
            
            categoryRepository.delete(category);
            logger.info("Category deleted successfully: {}", category.getName());
            
            return ResponseEntity.ok(MessageResponse.success("Category deleted successfully!"));
        } catch (Exception e) {
            logger.error("Error deleting category with id: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error(e.getMessage()));
        }
    }
} 