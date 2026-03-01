package com.wisegrade.academic.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MateriaCreateRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotNull Long nivelId) {
}
