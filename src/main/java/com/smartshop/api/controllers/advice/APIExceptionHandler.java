package com.smartshop.api.controllers.advice;

import com.smartshop.api.payload.response.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import java.sql.SQLException;

@ControllerAdvice
public class APIExceptionHandler extends ResponseEntityExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(APIExceptionHandler.class);
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<MessageResponse> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        logger.error("Entity not found: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(ex.getMessage()));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        logger.error("Access denied: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse("You don't have permission to access this resource"));
    }
    
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<MessageResponse> handleSQLException(SQLException ex, WebRequest request) {
        logger.error("Database error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("A database error occurred. Please try again later."));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("An unexpected error occurred. Please try again later."));
    }
} 