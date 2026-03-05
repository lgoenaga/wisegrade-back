package com.wisegrade.auth.api.dto;

import com.wisegrade.auth.model.UserRole;

public record AuthUserResponse(
        Long id,
        String documento,
        UserRole rol,
        Long docenteId,
        Long estudianteId,
        boolean activo) {
}
