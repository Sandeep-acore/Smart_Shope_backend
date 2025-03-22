package com.smartshop.api.payload.response;

import com.smartshop.api.models.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
        
        // Set the full image URL
        if (category.getImagePath() != null && !category.getImagePath().isEmpty()) {
            if (category.getImagePath().startsWith("http")) {
                response.setImageUrl(category.getImagePath());
            } else {
                response.setImageUrl(baseUrl + "/files/" + category.getImagePath());
            }
        }
        
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        return response;
    }
} 