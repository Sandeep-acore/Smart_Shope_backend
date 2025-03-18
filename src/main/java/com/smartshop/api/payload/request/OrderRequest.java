package com.smartshop.api.payload.request;

import com.smartshop.api.models.PaymentMethod;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OrderRequest {
    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
    
    @NotNull
    @Valid
    private AddressRequest shippingAddress;
    
    @NotNull
    private PaymentMethod paymentMethod;
    
    private String couponCode;
    
    private String notes;
} 