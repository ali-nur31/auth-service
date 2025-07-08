package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmCodeRequest {
    @NotBlank
    private String code;
}
