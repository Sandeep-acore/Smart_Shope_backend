package com.smartshop.api.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String profileImage;
    private List<String> roles;

    public JwtResponse(String token, Long id, String name, String email, String phone, String profileImage, List<String> roles) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profileImage = profileImage;
        this.roles = roles;
    }
} 