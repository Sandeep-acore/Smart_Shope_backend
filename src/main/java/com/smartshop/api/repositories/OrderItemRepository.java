package com.smartshop.api.repositories;

import com.smartshop.api.models.Order;
import com.smartshop.api.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
    
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi")
    Long sumTotalQuantity();
} 