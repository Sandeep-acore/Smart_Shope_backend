package com.smartshop.api.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsResponse {
    private Long totalOrders;
    private Long totalUsers;
    private BigDecimal totalAmount;
    private Long totalProductsSold;
    private Map<String, Long> ordersByStatus;
    private Map<String, BigDecimal> revenueByDay;
    private Long pendingOrders;
    private Long completedOrders;
    private BigDecimal averageOrderValue;
} 