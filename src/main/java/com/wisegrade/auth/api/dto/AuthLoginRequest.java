package com.wisegrade.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
        @NotBlank String documento,
        @NotBlank String clave) {
}
