package com.wisegrade.academic.api.dto;

public record DocenteResponse(
        Long id,
        String nombres,
        String apellidos,
        String documento,
        boolean activo) {
}
