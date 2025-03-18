package com.smartshop.api.payload.response;

import com.smartshop.api.models.WishlistItem;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class WishlistResponse {
    private List<WishlistItemDTO> items;
    private int itemCount;
    
    public WishlistResponse(List<WishlistItem> wishlistItems) {
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
        dto.setProductImage(wishlistItem.getProduct().getImageUrl());
        dto.setPrice(wishlistItem.getProduct().getPrice());
        dto.setDiscountedPrice(wishlistItem.getProduct().getDiscountedPrice());
        return dto;
    }
} 