package com.smartshop.api.repositories;

import com.smartshop.api.models.Category;
import com.smartshop.api.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    
    List<Product> findByCategory(Category category);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:query% OR p.description LIKE %:query%")
    List<Product> search(@Param("query") String query);
    
    @Query("SELECT p FROM Product p WHERE (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> filter(@Param("categoryId") Long categoryId, 
                         @Param("minPrice") BigDecimal minPrice, 
                         @Param("maxPrice") BigDecimal maxPrice);
    
    List<Product> findBySubCategoryId(Long subCategoryId);
} 