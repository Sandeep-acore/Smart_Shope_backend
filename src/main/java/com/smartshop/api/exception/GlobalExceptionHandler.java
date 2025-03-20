package com.smartshop.api.exception;

import com.smartshop.api.payload.response.MessageResponse;
import org.hibernate.LazyInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        logger.error("Entity not found: {}", ex.getMessage());
        return new ResponseEntity<>(
                MessageResponse.error(ex.getMessage()),
                HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        logger.error("Access denied: {}", ex.getMessage());
        return new ResponseEntity<>(
                MessageResponse.error("You don't have permission to access this resource"),
                HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.error("Validation error: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            String fieldName = path.substring(path.lastIndexOf('.') + 1);
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.error("Constraint violation: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
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
    
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<?> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        logger.error("Missing request part: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Required field missing: " + ex.getRequestPartName() + " is required");
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        logger.error("Missing parameter: {}", name);
        return new ResponseEntity<>(
                MessageResponse.error("Required parameter '" + name + "' is missing"),
                HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        logger.error("Bad credentials: {}", ex.getMessage());
        return new ResponseEntity<>(
                MessageResponse.error("Invalid username or password"),
                HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        logger.error("File size exceeded: {}", ex.getMessage());
        return new ResponseEntity<>(
                MessageResponse.error("File size exceeds maximum allowed upload size"),
                HttpStatus.PAYLOAD_TOO_LARGE);
    }
    
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Object> handleMultipartException(MultipartException ex) {
        logger.error("Multipart error: {}", ex.getMessage());
        return new ResponseEntity<>(
                MessageResponse.error("Error while processing multipart/form-data"),
                HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(IOException ex) {
        logger.error("IO error: {}", ex.getMessage());
        return new ResponseEntity<>(
                MessageResponse.error("Error processing file or I/O operation"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<Object> handleLazyInitializationException(LazyInitializationException ex) {
        logger.error("Lazy initialization error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                MessageResponse.error("Error accessing data. Please try again later."),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                MessageResponse.error("An unexpected error occurred. Please try again later."),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 