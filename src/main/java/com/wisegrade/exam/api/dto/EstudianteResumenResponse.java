package com.wisegrade.exam.api.dto;

public record EstudianteResumenResponse(
        Long id,
        String nombres,
        String apellidos,
        String documento) {
}
