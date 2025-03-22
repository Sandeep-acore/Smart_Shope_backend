package com.smartshop.api.controllers;

import com.smartshop.api.models.Order;
import com.smartshop.api.models.OrderItem;
import com.smartshop.api.models.OrderStatus;
import com.smartshop.api.models.User;
import com.smartshop.api.payload.response.MessageResponse;
import com.smartshop.api.payload.response.OrderDetailsResponse;
import com.smartshop.api.payload.response.OrderStatisticsResponse;
import com.smartshop.api.repositories.OrderItemRepository;
import com.smartshop.api.repositories.OrderRepository;
import com.smartshop.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<List<OrderDetailsResponse>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        
        List<OrderDetailsResponse> responses = orders.stream()
            .map(order -> OrderDetailsResponse.fromOrder(order, baseUrl))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailsResponse> getOrderById(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
            
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return ResponseEntity.ok(OrderDetailsResponse.fromOrder(order, baseUrl));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDetailsResponse>> getOrdersByUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            
        List<Order> orders = orderRepository.findByUser(user);
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        
        List<OrderDetailsResponse> responses = orders.stream()
            .map(order -> OrderDetailsResponse.fromOrder(order, baseUrl))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        try {
            Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
                
            // Validate status
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                order.setStatus(orderStatus);
                orderRepository.save(order);
                
                String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
                return ResponseEntity.ok(OrderDetailsResponse.fromOrder(order, baseUrl));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid order status. Valid statuses are: " + 
                    Arrays.stream(OrderStatus.values())
                        .map(OrderStatus::name)
                        .collect(Collectors.joining(", "))));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error updating order status: " + e.getMessage()));
        }
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<OrderStatisticsResponse> getOrderStatistics() {
        OrderStatisticsResponse statistics = new OrderStatisticsResponse();
        
        // Total orders
        Long totalOrders = orderRepository.count();
        statistics.setTotalOrders(totalOrders);
        
        // Total users who have placed orders
        Long totalUsers = orderRepository.countDistinctUsers();
        statistics.setTotalUsers(totalUsers);
        
        // Total amount of all orders
        BigDecimal totalAmount = orderRepository.sumOrderTotal();
        statistics.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        
        // Total products sold
        Long totalProductsSold = orderItemRepository.sumTotalQuantity();
        statistics.setTotalProductsSold(totalProductsSold != null ? totalProductsSold : 0L);
        
        // Orders by status
        Map<String, Long> ordersByStatus = orderRepository.countByStatus();
        statistics.setOrdersByStatus(ordersByStatus);
        
        // Pending orders
        Long pendingOrders = ordersByStatus.getOrDefault("PENDING", 0L);
        statistics.setPendingOrders(pendingOrders);
        
        // Completed orders
        Long completedOrders = ordersByStatus.getOrDefault("DELIVERED", 0L);
        statistics.setCompletedOrders(completedOrders);
        
        // Average order value
        if (totalOrders > 0 && totalAmount != null) {
            BigDecimal averageOrderValue = totalAmount.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
            statistics.setAverageOrderValue(averageOrderValue);
        } else {
            statistics.setAverageOrderValue(BigDecimal.ZERO);
        }
        
        // Revenue by day (last 7 days)
        Map<String, BigDecimal> revenueByDay = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Initialize last 7 days with zero values
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            revenueByDay.put(date.format(formatter), BigDecimal.ZERO);
        }
        
        // Get actual revenue data
        List<Order> recentOrders = orderRepository.findByCreatedAtAfter(LocalDateTime.now().minusDays(7));
        for (Order order : recentOrders) {
            String orderDate = order.getCreatedAt().toLocalDate().format(formatter);
            BigDecimal currentAmount = revenueByDay.getOrDefault(orderDate, BigDecimal.ZERO);
            revenueByDay.put(orderDate, currentAmount.add(order.getTotal()));
        }
        
        statistics.setRevenueByDay(revenueByDay);
        
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/with-users")
    public ResponseEntity<List<Map<String, Object>>> getAllOrdersWithUserDetails() {
        List<Order> orders = orderRepository.findAll();
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        
        List<Map<String, Object>> responseList = orders.stream()
            .map(order -> {
                Map<String, Object> response = new HashMap<>();
                
                // Order details
                response.put("order", OrderDetailsResponse.fromOrder(order, baseUrl));
                
                // Detailed user information
                User user = order.getUser();
                Map<String, Object> userDetails = new HashMap<>();
                userDetails.put("id", user.getId());
                userDetails.put("name", user.getName());
                userDetails.put("email", user.getEmail());
                userDetails.put("phone", user.getPhone());
                userDetails.put("profileImage", user.getProfileImage() != null ? 
                    baseUrl + "/api/files/" + user.getProfileImage() : null);
                userDetails.put("addressLine1", user.getAddressLine1());
                userDetails.put("addressLine2", user.getAddressLine2());
                userDetails.put("city", user.getCity());
                userDetails.put("state", user.getState());
                userDetails.put("postalCode", user.getPostalCode());
                userDetails.put("country", user.getCountry());
                userDetails.put("roles", user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList()));
                userDetails.put("createdAt", user.getCreatedAt());
                
                response.put("userDetails", userDetails);
                
                return response;
            })
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(responseList);
    }
} 