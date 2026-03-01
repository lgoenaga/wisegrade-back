package com.wisegrade.exam.api.dto;

import jakarta.validation.constraints.Min;

public record IntentoIniciarRequest(
        @Min(1) long periodoId,
        @Min(1) long materiaId,
        @Min(1) long momentoId,
        @Min(1) long docenteResponsableId,
        @Min(1) long estudianteId,
        Integer cantidad) {
}
