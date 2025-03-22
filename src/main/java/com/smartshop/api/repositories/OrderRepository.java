package com.smartshop.api.repositories;

import com.smartshop.api.models.Order;
import com.smartshop.api.models.OrderStatus;
import com.smartshop.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    List<Order> findByUserAndStatus(User user, String status);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = ?1")
    Order findByIdWithItems(Long id);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items LEFT JOIN FETCH o.shippingAddress LEFT JOIN FETCH o.deliveryAddress")
    List<Order> findAllWithDetails();
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items LEFT JOIN FETCH o.shippingAddress LEFT JOIN FETCH o.deliveryAddress WHERE o.user = ?1 ORDER BY o.createdAt DESC")
    List<Order> findByUserWithDetailsOrderByCreatedAtDesc(User user);
    
    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o")
    Long countDistinctUsers();
    
    @Query("SELECT SUM(o.total) FROM Order o")
    BigDecimal sumOrderTotal();
    
    @Query("SELECT o.status as status, COUNT(o) as count FROM Order o GROUP BY o.status")
    List<Object[]> countGroupByStatus();
    
    @Query("SELECT o FROM Order o WHERE o.createdAt >= ?1")
    List<Order> findByCreatedAtAfter(LocalDateTime date);
    
    default Map<String, Long> countByStatus() {
        Map<String, Long> result = new java.util.HashMap<>();
        List<Object[]> counts = countGroupByStatus();
        
        for (Object[] count : counts) {
            OrderStatus status = (OrderStatus) count[0];
            Long countValue = ((Number) count[1]).longValue();
            result.put(status.name(), countValue);
        }
        
        return result;
    }
} 