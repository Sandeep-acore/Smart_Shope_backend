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

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        List<Order> orders;
        
        // If admin or delivery partner, return all orders
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                               a.getAuthority().equals("ROLE_DELIVERY_PARTNER"))) {
            orders = orderRepository.findAll();
        } else {
            // Otherwise, return only the user's orders
            orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        }
        
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        
        // Check if the user is the owner of the order or an admin/delivery partner
        if (!order.getUser().getId().equals(userDetails.getId()) &&
                authentication.getAuthorities().stream()
                        .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                                       a.getAuthority().equals("ROLE_DELIVERY_PARTNER"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        }
        
        return ResponseEntity.ok(new OrderDTO(order));
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createOrder(
            @RequestParam("productIds") String productIds,
            @RequestParam("quantities") String quantities,
            @RequestParam("paymentMethod") PaymentMethod paymentMethod,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "useProfileAddress", required = false, defaultValue = "true") Boolean useProfileAddress,
            @RequestParam(value = "deliveryAddressLine1", required = false) String deliveryAddressLine1,
            @RequestParam(value = "deliveryAddressLine2", required = false) String deliveryAddressLine2,
            @RequestParam(value = "deliveryCity", required = false) String deliveryCity,
            @RequestParam(value = "deliveryState", required = false) String deliveryState,
            @RequestParam(value = "deliveryPostalCode", required = false) String deliveryPostalCode,
            @RequestParam(value = "deliveryCountry", required = false) String deliveryCountry) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
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
        
        return ResponseEntity.status(HttpStatus.CREATED).body(new OrderDTO(order));
    }

    @PutMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id, 
            @RequestParam("status") OrderStatus status) {
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        
        order.setStatus(status);
        
        // If order is delivered, set delivered time
        if (status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        
        orderRepository.save(order);
        
        return ResponseEntity.ok(new OrderDTO(order));
    }

    @PutMapping(value = "/{id}/payment-status", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long id, 
            @RequestParam("status") PaymentStatus status,
            @RequestParam(value = "transactionId", required = false) String transactionId) {
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        
        order.setPaymentStatus(status);
        
        if (transactionId != null) {
            order.setTransactionId(transactionId);
        }
        
        orderRepository.save(order);
        
        return ResponseEntity.ok(new OrderDTO(order));
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
        
        return ResponseEntity.ok(new MessageResponse("Invoice generation will be implemented in the next block."));
    }
} 