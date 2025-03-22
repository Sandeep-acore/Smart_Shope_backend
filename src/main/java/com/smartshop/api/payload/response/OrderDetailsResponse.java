package com.smartshop.api.payload.response;

import com.smartshop.api.models.Order;
import com.smartshop.api.models.OrderItem;
import com.smartshop.api.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsResponse {
    private Long id;
    private String status;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummary user;
    private List<OrderItemResponse> items;
    private String paymentMethod;
    private String address;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String name;
        private String email;
        private String phone;
        
        public static UserSummary fromUser(User user) {
            UserSummary summary = new UserSummary();
            summary.setId(user.getId());
            summary.setName(user.getName());
            summary.setEmail(user.getEmail());
            summary.setPhone(user.getPhone());
            return summary;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private String productName;
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subTotal;
        private String productImage;
        
        public static OrderItemResponse fromOrderItem(OrderItem item, String baseUrl) {
            OrderItemResponse response = new OrderItemResponse();
            response.setId(item.getId());
            response.setProductId(item.getProduct().getId());
            response.setProductName(item.getProduct().getName());
            response.setQuantity(item.getQuantity());
            response.setPrice(item.getPrice());
            response.setSubTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            
            // Set product image URL
            if (item.getProduct().getImageUrl() != null && !item.getProduct().getImageUrl().isEmpty()) {
                if (item.getProduct().getImageUrl().startsWith("http")) {
                    response.setProductImage(item.getProduct().getImageUrl());
                } else {
                    response.setProductImage(baseUrl + "/api/files/" + item.getProduct().getImageUrl());
                }
            }
            
            return response;
        }
    }
    
    public static OrderDetailsResponse fromOrder(Order order, String baseUrl) {
        OrderDetailsResponse response = new OrderDetailsResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus().toString());
        response.setTotal(order.getTotal());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setUser(UserSummary.fromUser(order.getUser()));
        response.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().toString() : null);
        
        // Set shipping address
        if (order.getShippingAddress() != null) {
            StringBuilder sb = new StringBuilder();
            if (order.getShippingAddress().getAddressLine1() != null) {
                sb.append(order.getShippingAddress().getAddressLine1()).append(", ");
            }
            if (order.getShippingAddress().getCity() != null) {
                sb.append(order.getShippingAddress().getCity()).append(", ");
            }
            if (order.getShippingAddress().getState() != null) {
                sb.append(order.getShippingAddress().getState()).append(", ");
            }
            if (order.getShippingAddress().getPostalCode() != null) {
                sb.append(order.getShippingAddress().getPostalCode()).append(", ");
            }
            if (order.getShippingAddress().getCountry() != null) {
                sb.append(order.getShippingAddress().getCountry());
            }
            response.setAddress(sb.toString());
        }
        
        // Map order items
        if (order.getItems() != null) {
            response.setItems(order.getItems().stream()
                .map(item -> OrderItemResponse.fromOrderItem(item, baseUrl))
                .collect(Collectors.toList()));
        }
        
        return response;
    }
} 