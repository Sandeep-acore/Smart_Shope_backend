package com.smartshop.api.controllers;

import com.smartshop.api.models.*;
import com.smartshop.api.payload.request.*;
import com.smartshop.api.payload.response.MessageResponse;
import com.smartshop.api.payload.response.OrderDTO;
import com.smartshop.api.repositories.OrderRepository;
import com.smartshop.api.repositories.ProductRepository;
import com.smartshop.api.repositories.UserRepository;
import com.smartshop.api.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<List<OrderDTO>> getUserOrders() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            
            List<Order> orders;
            
            // If admin or delivery partner, return all orders
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                                  a.getAuthority().equals("ROLE_DELIVERY_PARTNER"))) {
                orders = orderRepository.findAllWithDetails();
            } else {
                // Otherwise, return only the user's orders
                orders = orderRepository.findByUserWithDetailsOrderByCreatedAtDesc(user);
            }
            
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(order -> new OrderDTO(order, baseUrl))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            Order order = orderRepository.findByIdWithItems(id);
            if (order == null) {
                throw new EntityNotFoundException("Order not found with id: " + id);
            }
            
            // Check if the user is the owner of the order or an admin/delivery partner
            if (!order.getUser().getId().equals(userDetails.getId()) &&
                    authentication.getAuthorities().stream()
                            .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                                          a.getAuthority().equals("ROLE_DELIVERY_PARTNER"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .build();
            }
            
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return ResponseEntity.ok(new OrderDTO(order, baseUrl));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createOrder(
            @RequestParam("productIds") String productIds,
            @RequestParam("quantities") String quantities,
            @RequestParam("paymentMethod") String paymentMethodStr,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "useProfileAddress", required = false, defaultValue = "true") Boolean useProfileAddress,
            @RequestParam(value = "deliveryAddressLine1", required = false) String deliveryAddressLine1,
            @RequestParam(value = "deliveryAddressLine2", required = false) String deliveryAddressLine2,
            @RequestParam(value = "deliveryCity", required = false) String deliveryCity,
            @RequestParam(value = "deliveryState", required = false) String deliveryState,
            @RequestParam(value = "deliveryPostalCode", required = false) String deliveryPostalCode,
            @RequestParam(value = "deliveryCountry", required = false) String deliveryCountry) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            
            // Parse payment method
            PaymentMethod paymentMethod;
            try {
                paymentMethod = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Invalid payment method. Valid options are: " + 
                                Arrays.toString(PaymentMethod.values())));
            }
            
            // Create new order
            Order order = new Order();
            order.setUser(user);
            order.setPaymentMethod(paymentMethod);
            order.setNotes(notes);
            
            // Create shipping address from user's profile address
            if (user.getAddressLine1() != null && user.getCity() != null && user.getState() != null && 
                user.getPostalCode() != null && user.getCountry() != null) {
                
                Address shippingAddress = new Address();
                shippingAddress.setAddressLine1(user.getAddressLine1());
                shippingAddress.setAddressLine2(user.getAddressLine2());
                shippingAddress.setCity(user.getCity());
                shippingAddress.setState(user.getState());
                shippingAddress.setPostalCode(user.getPostalCode());
                shippingAddress.setCountry(user.getCountry());
                shippingAddress.setUser(user);
                
                order.setShippingAddress(shippingAddress);
            } else {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("User profile address is incomplete. Please update your profile with a complete address."));
            }
            
            // Create delivery address (either from profile or custom)
            if (useProfileAddress) {
                // Use the same address as shipping
                Address deliveryAddress = new Address();
                deliveryAddress.setAddressLine1(user.getAddressLine1());
                deliveryAddress.setAddressLine2(user.getAddressLine2());
                deliveryAddress.setCity(user.getCity());
                deliveryAddress.setState(user.getState());
                deliveryAddress.setPostalCode(user.getPostalCode());
                deliveryAddress.setCountry(user.getCountry());
                deliveryAddress.setUser(user);
                
                order.setDeliveryAddress(deliveryAddress);
            } else {
                // Use custom delivery address
                if (deliveryAddressLine1 == null || deliveryCity == null || deliveryState == null || 
                    deliveryPostalCode == null || deliveryCountry == null) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Delivery address is incomplete. Please provide all required fields."));
                }
                
                Address deliveryAddress = new Address();
                deliveryAddress.setAddressLine1(deliveryAddressLine1);
                deliveryAddress.setAddressLine2(deliveryAddressLine2);
                deliveryAddress.setCity(deliveryCity);
                deliveryAddress.setState(deliveryState);
                deliveryAddress.setPostalCode(deliveryPostalCode);
                deliveryAddress.setCountry(deliveryCountry);
                deliveryAddress.setUser(user);
                
                order.setDeliveryAddress(deliveryAddress);
            }
            
            // Parse product IDs and quantities
            String[] productIdArray = productIds.split(",");
            String[] quantityArray = quantities.split(",");
            
            if (productIdArray.length != quantityArray.length) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Number of product IDs must match number of quantities"));
            }
            
            if (productIdArray.length == 0) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Order must contain at least one item"));
            }
            
            // Add order items
            for (int i = 0; i < productIdArray.length; i++) {
                try {
                    Long productId = Long.parseLong(productIdArray[i].trim());
                    int quantity = Integer.parseInt(quantityArray[i].trim());
                    
                    if (quantity <= 0) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("Quantity must be greater than 0"));
                    }
                    
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
                    
                    // Check if product is in stock
                    if (product.getStockQuantity() < quantity) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("Product " + product.getName() + " is out of stock or has insufficient quantity."));
                    }
                    
                    // Create order item
                    OrderItem orderItem = new OrderItem(product, quantity);
                    order.addOrderItem(orderItem);
                    
                    // Update product stock
                    product.setStockQuantity(product.getStockQuantity() - quantity);
                    productRepository.save(product);
                    
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Invalid product ID or quantity format"));
                }
            }
            
            // Calculate order totals
            order.calculateTotals();
            
            // Save order
            orderRepository.save(order);
            
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return ResponseEntity.status(HttpStatus.CREATED).body(new OrderDTO(order, baseUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id, 
            @RequestParam("status") String statusStr) {
        
        try {
            OrderStatus status;
            try {
                status = OrderStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Invalid order status. Valid options are: " + 
                                Arrays.toString(OrderStatus.values())));
            }
            
            Order order = orderRepository.findByIdWithItems(id);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            OrderStatus oldStatus = order.getStatus();
            
            // Check if we're transitioning from a non-shipped/delivered status to shipped/delivered
            boolean isShippingOrDelivering = (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) && 
                                           !(oldStatus == OrderStatus.SHIPPED || oldStatus == OrderStatus.DELIVERED);
            
            // If the order is cancelled, don't allow updating to shipped/delivered
            if (oldStatus == OrderStatus.CANCELLED && (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Cannot update cancelled order to " + status + " status."));
            }
            
            // Update the status
            order.setStatus(status);
            
            // If order is delivered, set delivered time
            if (status == OrderStatus.DELIVERED) {
                order.setDeliveredAt(LocalDateTime.now());
            }
            
            // If transitioning to shipped or delivered, update stock quantities
            if (isShippingOrDelivering) {
                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    int currentStock = product.getStockQuantity();
                    int orderedQuantity = item.getQuantity();
                    // Make sure we don't go negative with stock
                    product.setStockQuantity(Math.max(0, currentStock - orderedQuantity));
                    productRepository.save(product);
                }
            }
            
            // Save the updated order
            orderRepository.save(order);
            
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return ResponseEntity.ok(new OrderDTO(order, baseUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error updating order status: " + e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}/payment-status", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long id, 
            @RequestParam("status") String statusStr,
            @RequestParam(value = "transactionId", required = false) String transactionId) {
        
        try {
            PaymentStatus status;
            try {
                status = PaymentStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Invalid payment status. Valid options are: " + 
                                Arrays.toString(PaymentStatus.values())));
            }
            
            Order order = orderRepository.findByIdWithItems(id);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            order.setPaymentStatus(status);
            
            if (transactionId != null) {
                order.setTransactionId(transactionId);
            }
            
            orderRepository.save(order);
            
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return ResponseEntity.ok(new OrderDTO(order, baseUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error updating payment status: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            Order order = orderRepository.findByIdWithItems(id);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            // Check if the user is the owner of the order or an admin
            if (!isAdmin && !order.getUser().getId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("You don't have permission to cancel this order. Only the user who created the order or an admin can cancel it."));
            }
            
            // Check if order can be cancelled (status must be PENDING or PROCESSING)
            if (!isAdmin && order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Cannot cancel order. Order has already been " + order.getStatus().toString().toLowerCase() + ". Only orders with PENDING or PROCESSING status can be cancelled."));
            }
            
            // Update order status to CANCELLED
            order.setStatus(OrderStatus.CANCELLED);
            
            // Restore product quantities
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
            
            orderRepository.save(order);
            
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return ResponseEntity.ok(new MessageResponse("Order cancelled successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error cancelling order: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/invoice")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getInvoice(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        
        // Check if the user is the owner of the order or an admin
        if (!order.getUser().getId().equals(userDetails.getId()) &&
                authentication.getAuthorities().stream()
                        .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        }
        
        // TODO: Generate and return invoice PDF
        // This will be implemented in the next block
        
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return ResponseEntity.ok(new MessageResponse("Invoice generation will be implemented in the next block."));
    }

    @GetMapping("/admin/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOrderAnalytics() {
        try {
            List<Order> allOrders = orderRepository.findAllWithDetails();
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            
            // Calculate basic counts
            int totalOrders = allOrders.size();
            long pendingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
            long processingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PROCESSING).count();
            long shippedOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count();
            long deliveredOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
            long cancelledOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
            
            // Calculate success rate
            double successRate = totalOrders > 0 ? (double)deliveredOrders / totalOrders * 100 : 0;
            
            // Calculate cancellation rate
            double cancellationRate = totalOrders > 0 ? (double)cancelledOrders / totalOrders * 100 : 0;
            
            // Calculate total revenue from successful orders
            BigDecimal totalRevenue = allOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                    .map(Order::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculate revenue from pending/processing orders
            BigDecimal pendingRevenue = allOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.PROCESSING)
                    .map(Order::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculate lost revenue from cancelled orders
            BigDecimal lostRevenue = allOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                    .map(Order::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Count unique customers who have placed orders
            long uniqueCustomers = allOrders.stream()
                    .map(order -> order.getUser().getId())
                    .distinct()
                    .count();
            
            // Get total products sold in all delivered orders
            long totalProductsSold = allOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                    .flatMap(order -> order.getItems().stream())
                    .mapToLong(OrderItem::getQuantity)
                    .sum();
            
            // Calculate average order value
            BigDecimal avgOrderValue = totalOrders > 0 ? 
                    allOrders.stream()
                            .map(Order::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            
            // Collect all ordered products with their quantities
            Map<Long, Map<String, Object>> orderedProducts = new HashMap<>();
            allOrders.stream()
                    .flatMap(order -> order.getItems().stream())
                    .forEach(item -> {
                        Long productId = item.getProduct().getId();
                        String productName = item.getProduct().getName();
                        
                        if (orderedProducts.containsKey(productId)) {
                            Map<String, Object> productInfo = orderedProducts.get(productId);
                            Long currentQuantity = (Long) productInfo.get("quantity");
                            BigDecimal currentRevenue = (BigDecimal) productInfo.get("totalRevenue");
                            BigDecimal itemRevenue = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                            
                            productInfo.put("quantity", currentQuantity + item.getQuantity());
                            productInfo.put("totalRevenue", currentRevenue.add(itemRevenue));
                            
                            // Make sure we already have the full image URL
                            String imageUrl = (String) productInfo.get("imageUrl");
                            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                                productInfo.put("imageUrl", baseUrl + "/files/" + imageUrl);
                            }
                            
                            // Make sure we have the full category image URL too
                            if (item.getProduct().getCategory() != null && !productInfo.containsKey("categoryImage")) {
                                String categoryImagePath = item.getProduct().getCategory().getImagePath();
                                if (categoryImagePath != null && !categoryImagePath.isEmpty()) {
                                    if (!categoryImagePath.startsWith("http")) {
                                        categoryImagePath = baseUrl + "/files/" + categoryImagePath;
                                    }
                                    productInfo.put("categoryImage", categoryImagePath);
                                }
                            }
                        } else {
                            Map<String, Object> productInfo = new HashMap<>();
                            BigDecimal itemRevenue = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                            String imageUrl = item.getProduct().getImageUrl();
                            
                            // Convert relative image path to full URL
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                if (!imageUrl.startsWith("http")) {
                                    imageUrl = baseUrl + "/files/" + imageUrl;
                                }
                            }
                            
                            productInfo.put("id", productId);
                            productInfo.put("name", productName);
                            productInfo.put("quantity", (long) item.getQuantity());
                            productInfo.put("price", item.getProduct().getPrice());
                            productInfo.put("totalRevenue", itemRevenue);
                            productInfo.put("imageUrl", imageUrl);
                            
                            if (item.getProduct().getCategory() != null) {
                                productInfo.put("categoryId", item.getProduct().getCategory().getId());
                                productInfo.put("categoryName", item.getProduct().getCategory().getName());
                                
                                // Add category image with full URL
                                String categoryImagePath = item.getProduct().getCategory().getImagePath();
                                if (categoryImagePath != null && !categoryImagePath.isEmpty()) {
                                    if (!categoryImagePath.startsWith("http")) {
                                        categoryImagePath = baseUrl + "/files/" + categoryImagePath;
                                    }
                                    productInfo.put("categoryImage", categoryImagePath);
                                }
                            }
                            
                            orderedProducts.put(productId, productInfo);
                        }
                    });
            
            // Get payment method statistics
            Map<String, Long> paymentMethodStats = allOrders.stream()
                    .collect(Collectors.groupingBy(
                            order -> order.getPaymentMethod().toString(),
                            Collectors.counting()
                    ));
            
            // Get monthly order counts for the last 6 months
            Map<String, Long> monthlyOrderCounts = new HashMap<>();
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
            
            Map<YearMonth, Long> ordersByMonth = allOrders.stream()
                    .filter(order -> order.getCreatedAt().isAfter(sixMonthsAgo))
                    .collect(Collectors.groupingBy(
                            order -> YearMonth.from(order.getCreatedAt()),
                            Collectors.counting()
                    ));
                    
            ordersByMonth.forEach((month, count) -> {
                monthlyOrderCounts.put(month.toString(), count);
            });
            
            // Create response map
            Map<String, Object> response = new HashMap<>();
            response.put("totalOrders", totalOrders);
            response.put("pendingOrders", pendingOrders);
            response.put("processingOrders", processingOrders);
            response.put("shippedOrders", shippedOrders);
            response.put("deliveredOrders", deliveredOrders);
            response.put("cancelledOrders", cancelledOrders);
            response.put("successRate", successRate);
            response.put("cancellationRate", cancellationRate);
            response.put("totalRevenue", totalRevenue);
            response.put("pendingRevenue", pendingRevenue);
            response.put("lostRevenue", lostRevenue);
            response.put("uniqueCustomers", uniqueCustomers);
            response.put("totalProductsSold", totalProductsSold);
            response.put("averageOrderValue", avgOrderValue);
            response.put("paymentMethodStats", paymentMethodStats);
            response.put("monthlyOrderCounts", monthlyOrderCounts);
            response.put("orderedProducts", new ArrayList<>(orderedProducts.values()));
            
            // Add detailed status report
            Map<String, Object> statusReport = new HashMap<>();
            for (OrderStatus status : OrderStatus.values()) {
                List<Order> ordersWithStatus = allOrders.stream()
                        .filter(o -> o.getStatus() == status)
                        .collect(Collectors.toList());
                
                statusReport.put(status.toString().toLowerCase() + "Count", ordersWithStatus.size());
                statusReport.put(status.toString().toLowerCase() + "Revenue", 
                        ordersWithStatus.stream()
                                .map(Order::getTotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                statusReport.put(status.toString().toLowerCase() + "Products", 
                        ordersWithStatus.stream()
                                .flatMap(order -> order.getItems().stream())
                                .mapToLong(OrderItem::getQuantity)
                                .sum());
            }
            response.put("statusReport", statusReport);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error generating order analytics: " + e.getMessage()));
        }
    }
} 