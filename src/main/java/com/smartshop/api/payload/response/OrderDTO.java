package com.smartshop.api.payload.response;

import com.smartshop.api.models.Address;
import com.smartshop.api.models.Order;
import com.smartshop.api.models.OrderItem;
import com.smartshop.api.models.OrderStatus;
import com.smartshop.api.models.PaymentStatus;
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
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private List<OrderItemDTO> items;
    private AddressDTO shippingAddress;
    private AddressDTO deliveryAddress;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    
    public OrderDTO(Order order) {
        this(order, null);
    }
    
    public OrderDTO(Order order, String baseUrl) {
        this.id = order.getId();
        this.orderNumber = "ORD-" + String.format("%06d", order.getId());
        this.items = order.getItems().stream()
                .map(item -> convertToOrderItemDTO(item, baseUrl))
                .collect(Collectors.toList());
        this.shippingAddress = convertToAddressDTO(order.getShippingAddress());
        this.deliveryAddress = convertToAddressDTO(order.getDeliveryAddress());
        this.status = order.getStatus();
        this.paymentStatus = order.getPaymentStatus();
        this.paymentMethod = order.getPaymentMethod().toString();
        this.subtotal = order.getSubtotal();
        this.tax = order.getTax();
        this.shippingCost = order.getShippingCost();
        this.total = order.getTotal();
        this.createdAt = order.getCreatedAt();
        this.deliveredAt = order.getDeliveredAt();
    }
    
    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        return convertToOrderItemDTO(orderItem, null);
    }
    
    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem, String baseUrl) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        
        // Set product image with full URL if baseUrl is provided
        String imageUrl = orderItem.getProduct().getImageUrl();
        if (baseUrl != null && imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
            imageUrl = baseUrl + "/files/" + imageUrl;
        }
        dto.setProductImage(imageUrl);
        
        dto.setPrice(orderItem.getPrice());
        dto.setQuantity(orderItem.getQuantity());
        dto.setTotalPrice(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        return dto;
    }
    
    private AddressDTO convertToAddressDTO(Address address) {
        if (address == null) {
            return null;
        }
        
        AddressDTO dto = new AddressDTO();
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        return dto;
    }
} 