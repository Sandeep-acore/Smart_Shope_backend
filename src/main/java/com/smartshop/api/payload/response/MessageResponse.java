package com.smartshop.api.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String status;
    private String message;
    
    public MessageResponse(String message) {
        this.status = "success";
        this.message = message;
    }
    
    public static MessageResponse success(String message) {
        return new MessageResponse("success", message);
    }
    
    public static MessageResponse error(String message) {
        return new MessageResponse("error", message);
    }
} 