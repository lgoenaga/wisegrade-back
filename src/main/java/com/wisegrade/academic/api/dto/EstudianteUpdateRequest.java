package com.wisegrade.academic.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EstudianteUpdateRequest(
        @NotBlank @Size(max = 150) String nombres,
        @NotBlank @Size(max = 150) String apellidos,
        @NotBlank @Size(max = 50) String documento,
        @NotNull Boolean activo) {
}
