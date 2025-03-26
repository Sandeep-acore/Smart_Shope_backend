package com.smartshop.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal discountedPrice;

    public OrderItem(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice();
        this.discountedPrice = product.getDiscountedPrice() != null ? product.getDiscountedPrice() : product.getPrice();
    }
} 