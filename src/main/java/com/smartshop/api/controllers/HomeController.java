package com.smartshop.api.controllers;

import org.springframework.http.ResponseEntity;
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
        health.put("database", "connected");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }
} 