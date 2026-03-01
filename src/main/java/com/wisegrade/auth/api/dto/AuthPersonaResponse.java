package com.wisegrade.auth.api.dto;

public record AuthPersonaResponse(
        Long id,
        String documento,
        String nombres,
        String apellidos
) {
}
