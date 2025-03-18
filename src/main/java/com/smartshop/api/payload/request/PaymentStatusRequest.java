package com.smartshop.api.payload.request;

import com.smartshop.api.models.PaymentStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PaymentStatusRequest {
    @NotNull
    private PaymentStatus status;
    
    private String transactionId;
} 