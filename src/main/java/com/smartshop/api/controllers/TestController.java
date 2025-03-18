package com.smartshop.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * TestController provides endpoints for testing the application
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * Simple echo endpoint
     * @param message The message to echo
     * @return The echoed message
     */
    @GetMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestParam(defaultValue = "Hello, world!") String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("echo", message);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Environment test endpoint
     * @return Environment information
     */
    @GetMapping("/environment")
    public ResponseEntity<Map<String, Object>> environment() {
        Map<String, Object> env = new HashMap<>();
        env.put("java.version", System.getProperty("java.version"));
        env.put("os.name", System.getProperty("os.name"));
        env.put("user.timezone", System.getProperty("user.timezone"));
        env.put("server.port", System.getProperty("server.port", "8080"));
        return ResponseEntity.ok(env);
    }
} 