package com.wisegrade.auth.api.dto;

public record AuthMeResponse(
        String documento,
        String rol,
        AuthPersonaResponse estudiante,
        AuthPersonaResponse docente) {
}
