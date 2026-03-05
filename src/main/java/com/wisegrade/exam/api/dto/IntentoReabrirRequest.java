package com.wisegrade.exam.api.dto;

import jakarta.validation.constraints.Min;

public record IntentoReabrirRequest(
        @Min(1) int extraMinutes) {
}
