package com.smartshop.api.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DebugController {

    @Value("${app.url:}")
    private String appUrl;
    
    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getDebugInfo() {
        Map<String, String> debugInfo = new HashMap<>();
        debugInfo.put("appUrl", appUrl);
        debugInfo.put("contextPath", contextPath);
        debugInfo.put("currentRequestUrl", ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString());
        
        return ResponseEntity.ok(debugInfo);
    }
} 