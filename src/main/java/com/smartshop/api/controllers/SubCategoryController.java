package com.smartshop.api.controllers;

import com.smartshop.api.models.Category;
import com.smartshop.api.models.SubCategory;
import com.smartshop.api.payload.response.MessageResponse;
import com.smartshop.api.payload.response.SubCategoryResponse;
import com.smartshop.api.repositories.CategoryRepository;
import com.smartshop.api.repositories.SubCategoryRepository;
import com.smartshop.api.services.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subcategories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SubCategoryController {
    private static final Logger logger = LoggerFactory.getLogger(SubCategoryController.class);

    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    
    @Value("${app.url:}")
    private String appUrl;

    @Autowired
    public SubCategoryController(SubCategoryRepository subCategoryRepository, CategoryRepository categoryRepository, FileStorageService fileStorageService) {
        this.subCategoryRepository = subCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }
    
    private String getBaseUrl() {
        if (appUrl != null && !appUrl.isEmpty()) {
            return appUrl;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }

    @GetMapping("/category/{categoryId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<SubCategoryResponse>> getSubCategoriesByCategory(@PathVariable Long categoryId) {
        String baseUrl = getBaseUrl();
        List<SubCategory> subCategories = subCategoryRepository.findByCategoryId(categoryId);
        
        List<SubCategoryResponse> responses = subCategories.stream()
            .map(subCategory -> SubCategoryResponse.fromSubCategory(subCategory, baseUrl))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> createSubCategory(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            // Validate name
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Name is required"));
            }

            // Check if category exists
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

            // Check if subcategory with same name exists in the same category
            if (subCategoryRepository.existsByNameAndCategoryId(name, categoryId)) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: A subcategory with name '" + name + "' already exists in category '" + category.getName() + "'"));
            }

            SubCategory subCategory = new SubCategory();
            subCategory.setName(name);
            subCategory.setDescription(description);
            subCategory.setCategory(category);

            // Handle image upload
            if (image != null && !image.isEmpty()) {
                try {
                    // Check image type
                    String contentType = image.getContentType();
                    if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"))) {
                        return ResponseEntity.badRequest().body(new MessageResponse("Error: Only JPEG, JPG and PNG images are supported"));
                    }
                    
                    String imageUrl = fileStorageService.storeFile(image, "subcategories");
                    if (imageUrl == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse("Error: Failed to save image file"));
                    }
                    subCategory.setImageUrl(imageUrl);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse("Error saving subcategory image: " + e.getMessage()));
                }
            }

            SubCategory savedSubCategory = subCategoryRepository.save(subCategory);
            
            String baseUrl = getBaseUrl();
            return ResponseEntity.status(HttpStatus.CREATED).body(SubCategoryResponse.fromSubCategory(savedSubCategory, baseUrl));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<SubCategoryResponse>> getAllSubCategories() {
        String baseUrl = getBaseUrl();
        List<SubCategory> subCategories = subCategoryRepository.findAll();
        
        List<SubCategoryResponse> responses = subCategories.stream()
            .map(subCategory -> SubCategoryResponse.fromSubCategory(subCategory, baseUrl))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<SubCategoryResponse> getSubCategory(@PathVariable Long id) {
        return subCategoryRepository.findById(id)
                .map(subCategory -> {
                    String baseUrl = getBaseUrl();
                    return ResponseEntity.ok(SubCategoryResponse.fromSubCategory(subCategory, baseUrl));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> updateSubCategory(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            SubCategory subCategory = subCategoryRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("SubCategory not found with id: " + id));

            // Update basic properties
            subCategory.setName(name);
            subCategory.setDescription(description);

            // Update category if provided
            if (categoryId != null) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
                subCategory.setCategory(category);
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
                    if (subCategory.getImageUrl() != null && !subCategory.getImageUrl().isEmpty()) {
                        boolean deleted = fileStorageService.deleteFile(subCategory.getImageUrl());
                        if (!deleted) {
                            // Just log warning and continue - don't stop the update
                            logger.warn("Could not delete old subcategory image: {}", subCategory.getImageUrl());
                        }
                    }
                    
                    // Save new image
                    String imageUrl = fileStorageService.storeFile(image, "subcategories");
                    if (imageUrl == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse("Error: Failed to save new image file"));
                    }
                    subCategory.setImageUrl(imageUrl);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse("Error updating subcategory image: " + e.getMessage()));
                }
            }

            SubCategory updatedSubCategory = subCategoryRepository.save(subCategory);
            
            String baseUrl = getBaseUrl();
            return ResponseEntity.ok(SubCategoryResponse.fromSubCategory(updatedSubCategory, baseUrl));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> deleteSubCategory(@PathVariable Long id) {
        try {
            SubCategory subCategory = subCategoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("SubCategory not found with id: " + id));
            
            // Delete subcategory image if exists
            if (subCategory.getImageUrl() != null && !subCategory.getImageUrl().isEmpty()) {
                boolean deleted = fileStorageService.deleteFile(subCategory.getImageUrl());
                if (!deleted) {
                    logger.warn("Could not delete subcategory image: {}", subCategory.getImageUrl());
                }
            }
            
            subCategoryRepository.delete(subCategory);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
} 