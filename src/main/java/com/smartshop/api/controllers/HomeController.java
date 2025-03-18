package com.smartshop.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * HomeController provides basic endpoints to test if the application is running correctly
 */
@RestController
@RequestMapping("/")
public class HomeController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Simple greeting endpoint
     * @return A greeting message
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Smart Shop API");
        response.put("status", "running");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     * @return Health status of the application
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        
        // Test database connection
        try {
            String dbStatus = jdbcTemplate.queryForObject("SELECT 'connected' AS status", String.class);
            health.put("database", dbStatus);
        } catch (Exception e) {
            health.put("database", "error: " + e.getMessage());
        }
        
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }
    
    /**
     * Database test endpoint
     * @return Database connection details
     */
    @GetMapping("/db-test")
    public ResponseEntity<Map<String, Object>> dbTest() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Simple query to test connection
            String dbStatus = jdbcTemplate.queryForObject("SELECT 'connected' AS status", String.class);
            result.put("connection", dbStatus);
            
            // Get database metadata
            Map<String, Object> metadata = new HashMap<>();
            jdbcTemplate.query("SELECT version() AS version", rs -> {
                metadata.put("version", rs.getString("version"));
            });
            
            result.put("metadata", metadata);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getName());
        }
        
        return ResponseEntity.ok(result);
    }
} 