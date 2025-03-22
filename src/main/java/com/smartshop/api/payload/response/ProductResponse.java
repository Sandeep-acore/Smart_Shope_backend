package com.smartshop.api.payload.response;

import com.smartshop.api.models.Category;
import com.smartshop.api.models.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Integer discountPercentage;
    private BigDecimal discountedPrice;
    private CategoryResponse category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ProductResponse fromProduct(Product product, String baseUrl) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        
        // Set the full image URL
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            if (product.getImageUrl().startsWith("http")) {
                response.setImageUrl(product.getImageUrl());
            } else {
                response.setImageUrl(baseUrl + "/files/" + product.getImageUrl());
            }
        }
        
        response.setDiscountPercentage(product.getDiscountPercentage());
        response.setDiscountedPrice(product.getDiscountedPrice());
        
        // Convert Category to CategoryResponse to get full image URL
        if (product.getCategory() != null) {
            response.setCategory(CategoryResponse.fromCategory(product.getCategory(), baseUrl));
        }
        
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        
        return response;
    }
} 