package com.wisegrade.academic.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NivelUpdateRequest(
        @NotBlank @Size(max = 100) String nombre) {
}
