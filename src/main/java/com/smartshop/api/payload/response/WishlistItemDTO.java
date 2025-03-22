package com.smartshop.api.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String imageRelativePath;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal discountedPrice;
} 