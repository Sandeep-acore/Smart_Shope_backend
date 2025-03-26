package com.smartshop.api.payload.response;

import com.smartshop.api.models.Product;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private Integer stockQuantity;
    private Integer discountPercentage;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Category fields
    private Long categoryId;
    private String categoryName;
    private String categoryImageUrl;
    
    // Subcategory fields
    private Long subCategoryId;
    private String subCategoryName;
    private String subCategoryImageUrl;

    public static ProductResponse fromProduct(Product product, String baseUrl) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setDiscountedPrice(product.getDiscountedPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setDiscountPercentage(product.getDiscountPercentage());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        
        // Set image URL
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            if (product.getImageUrl().startsWith("http")) {
                response.setImageUrl(product.getImageUrl());
            } else {
                response.setImageUrl(baseUrl + "/files/" + product.getImageUrl());
            }
        }
        
        // Set category information
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
            
            // Set category image URL
            if (product.getCategory().getImageUrl() != null && !product.getCategory().getImageUrl().isEmpty()) {
                if (product.getCategory().getImageUrl().startsWith("http")) {
                    response.setCategoryImageUrl(product.getCategory().getImageUrl());
                } else {
                    response.setCategoryImageUrl(baseUrl + "/files/" + product.getCategory().getImageUrl());
                }
            }
        }
        
        // Set subcategory information
        if (product.getSubCategory() != null) {
            response.setSubCategoryId(product.getSubCategory().getId());
            response.setSubCategoryName(product.getSubCategory().getName());
            
            // Set subcategory image URL
            if (product.getSubCategory().getImageUrl() != null && !product.getSubCategory().getImageUrl().isEmpty()) {
                if (product.getSubCategory().getImageUrl().startsWith("http")) {
                    response.setSubCategoryImageUrl(product.getSubCategory().getImageUrl());
                } else {
                    response.setSubCategoryImageUrl(baseUrl + "/files/" + product.getSubCategory().getImageUrl());
                }
            }
        }
        
        return response;
    }
} 