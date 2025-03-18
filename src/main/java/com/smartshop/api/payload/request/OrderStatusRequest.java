package com.smartshop.api.payload.request;

import com.smartshop.api.models.OrderStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OrderStatusRequest {
    @NotNull
    private OrderStatus status;
} 