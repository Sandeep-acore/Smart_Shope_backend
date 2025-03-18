package com.smartshop.api.exception;

import com.smartshop.api.payload.response.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        logger.error("Entity not found exception: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse("Error: " + ex.getMessage()));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        logger.error("Access denied exception: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse("Error: You don't have permission to access this resource."));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        response.put("status", "error");
        response.put("message", "Validation failed. Please check your input.");
        response.put("errors", errors);
        
        logger.error("Validation error: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        // Parse the constraint violations from the exception message
        String message = ex.getMessage();
        Pattern pattern = Pattern.compile("propertyPath=([^,]+).*interpolatedMessage='([^']+)'");
        Matcher matcher = pattern.matcher(message);
        
        while (matcher.find()) {
            String field = matcher.group(1);
            String errorMessage = matcher.group(2);
            errors.put(field, errorMessage);
        }
        
        response.put("status", "error");
        response.put("message", "Validation failed. Please check your input.");
        response.put("errors", errors);
        
        logger.error("Constraint violation: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    
    @ExceptionHandler(org.hibernate.exception.ConstraintViolationException.class)
    public ResponseEntity<?> handleHibernateConstraintViolationException(org.hibernate.exception.ConstraintViolationException ex) {
        logger.error("Hibernate constraint violation: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        
        String message = ex.getMessage();
        if (message.contains("Duplicate entry") && message.contains("email")) {
            response.put("message", "This email address is already registered.");
        } else if (message.contains("Duplicate entry") && message.contains("phone")) {
            response.put("message", "This phone number is already registered.");
        } else {
            response.put("message", "Database constraint violation. Please check your input.");
        }
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        String message = ex.getMessage();
        String userFriendlyMessage = "Database constraint violation. Please check your input.";
        
        if (message.contains("Data truncation")) {
            userFriendlyMessage = "Data too long for one or more fields. Please check your input.";
        } else if (message.contains("Duplicate entry") && message.contains("email")) {
            userFriendlyMessage = "This email address is already registered.";
        } else if (message.contains("Duplicate entry") && message.contains("phone")) {
            userFriendlyMessage = "This phone number is already registered.";
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", userFriendlyMessage);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unhandled exception: ", ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        
        // Check if this is a validation exception with a specific format
        if (ex.getMessage() != null && ex.getMessage().contains("Validation failed for classes")) {
            Map<String, String> errors = new HashMap<>();
            
            // Extract field-specific error messages using regex
            Pattern pattern = Pattern.compile("propertyPath=([^,]+).*interpolatedMessage='([^']+)'");
            Matcher matcher = pattern.matcher(ex.getMessage());
            
            while (matcher.find()) {
                String field = matcher.group(1);
                String message = matcher.group(2);
                errors.put(field, message);
            }
            
            response.put("message", "Validation failed. Please check your input.");
            response.put("errors", errors);
        } else {
            response.put("message", "An unexpected error occurred. Please try again later.");
        }
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
} 