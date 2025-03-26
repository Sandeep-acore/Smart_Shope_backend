package com.smartshop.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_address_id")
    private Address deliveryAddress;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String transactionId;

    private BigDecimal subtotal;
    
    private BigDecimal shippingCost;
    
    private BigDecimal tax;
    
    private BigDecimal discount;
    
    private BigDecimal total;

    private String couponCode;

    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deliveredAt;

    @PrePersist
    protected void onCreate() {
        // Generate a unique order number when the order is created
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uniqueId = java.util.UUID.randomUUID().toString().substring(0, 4);
        this.orderNumber = "ORD-" + timestamp + "-" + uniqueId;
    }

    public void calculateTotals() {
        // Calculate subtotal based on the discounted price of each item
        this.subtotal = items.stream()
                .map(item -> item.getDiscountedPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Apply discount if any (coupon or promotion discount)
        if (this.discount == null) {
            this.discount = BigDecimal.ZERO;
        }
        
        // Set shipping cost if not set
        if (this.shippingCost == null) {
            // Default shipping cost can be set here or kept as ZERO
            this.shippingCost = BigDecimal.valueOf(0);
        }
        
        // Set tax if not set (can be calculated as a percentage of subtotal)
        if (this.tax == null) {
            // Default tax can be set here or kept as ZERO
            this.tax = BigDecimal.valueOf(0);
        }
        
        // Calculate total
        this.total = this.subtotal
                .add(this.shippingCost)
                .add(this.tax)
                .subtract(this.discount);
    }

    public void addOrderItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
} 