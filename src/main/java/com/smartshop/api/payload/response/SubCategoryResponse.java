package com.smartshop.api.payload.response;

import com.smartshop.api.models.SubCategory;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SubCategoryResponse fromSubCategory(SubCategory subCategory, String baseUrl) {
        SubCategoryResponse response = new SubCategoryResponse();
        response.setId(subCategory.getId());
        response.setName(subCategory.getName());
        response.setDescription(subCategory.getDescription());
        
        // Set image URL
        if (subCategory.getImageUrl() != null && !subCategory.getImageUrl().isEmpty()) {
            if (subCategory.getImageUrl().startsWith("http")) {
                response.setImageUrl(subCategory.getImageUrl());
            } else {
                response.setImageUrl(baseUrl + "/files/" + subCategory.getImageUrl());
            }
        }
        
        if (subCategory.getCategory() != null) {
            response.setCategoryId(subCategory.getCategory().getId());
            response.setCategoryName(subCategory.getCategory().getName());
        }
        
        response.setCreatedAt(subCategory.getCreatedAt());
        response.setUpdatedAt(subCategory.getUpdatedAt());
        
        return response;
    }
} 