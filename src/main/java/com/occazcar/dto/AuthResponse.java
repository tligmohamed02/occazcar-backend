package com.occazcar.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private String role;
}