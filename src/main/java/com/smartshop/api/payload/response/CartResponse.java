package com.smartshop.api.payload.response;

import com.smartshop.api.models.CartItem;
import com.smartshop.api.services.FileStorageService;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CartResponse {
    private List<CartItemDTO> items;
    private BigDecimal subtotal;
    private int itemCount;
    
    @JsonIgnore
    private FileStorageService fileStorageService;
    
    public CartResponse(List<CartItem> cartItems, FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        this.items = cartItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        this.itemCount = items.size();
        this.subtotal = calculateSubtotal();
    }
    
    private CartItemDTO convertToDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        
        // Set product image with both regular path and full URL
        String imagePath = cartItem.getProduct().getImageUrl();
        dto.setImageRelativePath(imagePath);
        
        // Add full URL for product image if it exists
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imagePath.startsWith("http")) {
                dto.setImageUrl(imagePath);
            } else {
                dto.setImageUrl(fileStorageService.getFileUrl(imagePath));
            }
        }
        
        dto.setPrice(cartItem.getProduct().getPrice());
        dto.setDiscountedPrice(cartItem.getProduct().getDiscountedPrice());
        dto.setQuantity(cartItem.getQuantity());
        
        // Calculate total price for this item
        BigDecimal priceToUse = cartItem.getProduct().getDiscountedPrice() != null && 
                !cartItem.getProduct().getDiscountedPrice().equals(cartItem.getProduct().getPrice()) ? 
                cartItem.getProduct().getDiscountedPrice() : 
                cartItem.getProduct().getPrice();
        
        dto.setTotalPrice(priceToUse.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        
        return dto;
    }
    
    private BigDecimal calculateSubtotal() {
        return items.stream()
                .map(CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 