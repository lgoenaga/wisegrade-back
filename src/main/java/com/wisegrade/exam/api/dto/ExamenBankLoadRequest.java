package com.wisegrade.exam.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ExamenBankLoadRequest(
        @Min(1) long periodoId,
        @Min(1) long materiaId,
        @Min(1) long momentoId,
        @Min(1) long docenteResponsableId,
        @NotEmpty @Valid List<PreguntaCreateRequest> preguntas) {
}
