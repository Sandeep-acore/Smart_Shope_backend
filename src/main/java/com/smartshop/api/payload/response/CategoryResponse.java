package com.smartshop.api.payload.response;

import com.smartshop.api.models.Category;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CategoryResponse fromCategory(Category category, String baseUrl) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        
        // Set image URL
        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            if (category.getImageUrl().startsWith("http")) {
                response.setImageUrl(category.getImageUrl());
            } else {
                response.setImageUrl(baseUrl + "/files/" + category.getImageUrl());
            }
        }
        
        
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        return response;
    }
} 