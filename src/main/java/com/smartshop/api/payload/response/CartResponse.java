package com.smartshop.api.payload.response;

import com.smartshop.api.models.CartItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CartResponse {
    private List<CartItemDTO> items;
    private BigDecimal subtotal;
    private int itemCount;
    
    public CartResponse(List<CartItem> cartItems) {
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
        dto.setProductImage(cartItem.getProduct().getImageUrl());
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