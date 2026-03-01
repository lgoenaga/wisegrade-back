package com.wisegrade.academic.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PeriodoCreateRequest(
        @Min(2000) @Max(2100) int anio,
        @NotBlank @Size(max = 100) String nombre) {
}
