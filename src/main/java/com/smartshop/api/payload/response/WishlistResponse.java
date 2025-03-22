package com.smartshop.api.payload.response;

import com.smartshop.api.models.WishlistItem;
import com.smartshop.api.services.FileStorageService;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class WishlistResponse {
    private List<WishlistItemDTO> items;
    private int itemCount;
    
    private final FileStorageService fileStorageService;
    
    public WishlistResponse(List<WishlistItem> wishlistItems, FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        this.items = wishlistItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        this.itemCount = items.size();
    }
    
    private WishlistItemDTO convertToDTO(WishlistItem wishlistItem) {
        WishlistItemDTO dto = new WishlistItemDTO();
        dto.setId(wishlistItem.getId());
        dto.setProductId(wishlistItem.getProduct().getId());
        dto.setProductName(wishlistItem.getProduct().getName());
        
        // Set the product image with both regular path and full URL
        String imagePath = wishlistItem.getProduct().getImageUrl();
        dto.setImageRelativePath(imagePath);
        
        // Add full URL for product image if it exists
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imagePath.startsWith("http")) {
                dto.setImageUrl(imagePath);
            } else {
                dto.setImageUrl(fileStorageService.getFileUrl(imagePath));
            }
        }
        
        dto.setPrice(wishlistItem.getProduct().getPrice());
        dto.setDiscountedPrice(wishlistItem.getProduct().getDiscountedPrice());
        return dto;
    }
} 