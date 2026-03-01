package com.wisegrade.academic.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MomentoCreateRequest(
        @NotBlank @Size(max = 100) String nombre) {
}
