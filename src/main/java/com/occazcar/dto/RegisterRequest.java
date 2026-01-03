package com.occazcar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    private String fullName;

    private String phone;

    @NotBlank
    private String role; // VENDEUR ou ACHETEUR
}
