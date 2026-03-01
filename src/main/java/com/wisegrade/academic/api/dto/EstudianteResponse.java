package com.wisegrade.academic.api.dto;

public record EstudianteResponse(
        Long id,
        String nombres,
        String apellidos,
        String documento,
        boolean activo) {
}
