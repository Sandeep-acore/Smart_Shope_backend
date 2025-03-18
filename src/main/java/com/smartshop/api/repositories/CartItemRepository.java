package com.smartshop.api.repositories;

import com.smartshop.api.models.CartItem;
import com.smartshop.api.models.Product;
import com.smartshop.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    
    void deleteByUserAndProduct(User user, Product product);
    
    void deleteByUser(User user);
} 