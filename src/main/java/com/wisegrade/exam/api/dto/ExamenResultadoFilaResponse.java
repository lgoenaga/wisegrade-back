package com.wisegrade.exam.api.dto;

import java.time.LocalDateTime;

public record ExamenResultadoFilaResponse(
        Long intentoId,
        EstudianteResumenResponse estudiante,
        LocalDateTime startedAt,
        LocalDateTime submittedAt,
        ResultadoIntentoResponse resultado) {
}
