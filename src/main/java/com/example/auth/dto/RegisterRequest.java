package com.example.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Size(min = 3, max = 50)
    private String name;

    @NotBlank @Size(min = 3, max = 50)
    private String username;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(max = 15)
    private String phoneNumber;

    @NotBlank @Size(min = 8, max = 100)
    private String password;

    @NotBlank @Size(min = 8, max = 100)
    private String confirmPassword;
}
