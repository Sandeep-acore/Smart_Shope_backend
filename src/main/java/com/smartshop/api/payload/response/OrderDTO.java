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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
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
        
        // Use the order number from the Order entity if available
        if (order.getOrderNumber() != null && !order.getOrderNumber().isEmpty()) {
            this.orderNumber = order.getOrderNumber();
        } else {
            // Fallback to generating a new order number if not available
            String timestamp = "";
            if (order.getCreatedAt() != null) {
                timestamp = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            } else {
                timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            }
            String uniqueId = UUID.randomUUID().toString().substring(0, 4);
            this.orderNumber = "ORD-" + timestamp + "-" + uniqueId;
        }
        
        // Convert order items and calculate totals directly from items
        this.items = order.getItems().stream()
                .map(item -> convertToOrderItemDTO(item, baseUrl))
                .collect(Collectors.toList());
                
        // Calculate subtotal directly from the order items to ensure accuracy
        this.subtotal = this.items.stream()
                .map(item -> item.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        this.shippingAddress = convertToAddressDTO(order.getShippingAddress());
        this.deliveryAddress = convertToAddressDTO(order.getDeliveryAddress());
        this.status = order.getStatus();
        this.paymentStatus = order.getPaymentStatus();
        this.paymentMethod = order.getPaymentMethod().toString();
        
        // Ensure all the order calculation fields are properly set
        this.tax = order.getTax() != null ? order.getTax() : BigDecimal.ZERO;
        this.shippingCost = order.getShippingCost() != null ? order.getShippingCost() : BigDecimal.ZERO;
        
        // Calculate the total directly instead of using order.getTotal()
        BigDecimal discount = order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO;
        this.total = this.subtotal
                .add(this.tax)
                .add(this.shippingCost)
                .subtract(discount);
        
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
        
        // Ensure we're using the discounted price from the OrderItem
        BigDecimal discountedPrice = orderItem.getDiscountedPrice();
        if (discountedPrice == null) {
            // Fallback to the product's discounted price if the OrderItem's price is null
            discountedPrice = orderItem.getProduct().getDiscountedPrice();
            if (discountedPrice == null) {
                // If that's also null, use the regular price
                discountedPrice = orderItem.getProduct().getPrice();
            }
        }
        dto.setPrice(discountedPrice);
        dto.setQuantity(orderItem.getQuantity());
        
        // Calculate the total price correctly based on the discounted price
        dto.setTotalPrice(discountedPrice.multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        
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