package com.wisegrade.auth.api.dto;

import com.wisegrade.auth.model.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthUserCreateRequest(
        @NotBlank String documento,
        @NotBlank String clave,
        @NotNull UserRole rol,
        Long docenteId,
        Long estudianteId,
        Boolean activo) {
}
