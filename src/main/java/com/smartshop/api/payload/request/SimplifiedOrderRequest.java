package com.smartshop.api.payload.request;

import com.smartshop.api.models.PaymentMethod;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SimplifiedOrderRequest {
    
    @NotBlank(message = "Product IDs are required")
    private String productIds; // Comma-separated product IDs
    
    @NotBlank(message = "Quantities are required")
    private String quantities; // Comma-separated quantities
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private String notes;
} 