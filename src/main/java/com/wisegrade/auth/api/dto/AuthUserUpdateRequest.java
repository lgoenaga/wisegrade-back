package com.wisegrade.auth.api.dto;

import com.wisegrade.auth.model.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Full update (PUT) semantics.
 * - "clave" is optional: if null/blank, password is not changed.
 */
public record AuthUserUpdateRequest(
        @NotBlank String documento,
        String clave,
        @NotNull UserRole rol,
        Long docenteId,
        Long estudianteId,
        @NotNull Boolean activo) {
}
