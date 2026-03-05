package com.wisegrade.exam.api.dto;

import com.wisegrade.exam.model.IntentoEstado;

import java.time.LocalDateTime;

public record ExamenResultadoFilaResponse(
        Long intentoId,
        IntentoEstado estado,
        EstudianteResumenResponse estudiante,
        LocalDateTime startedAt,
        LocalDateTime deadlineAt,
        LocalDateTime blockedAt,
        Integer reopenCount,
        Integer extraMinutesTotal,
        LocalDateTime submittedAt,
        ResultadoIntentoResponse resultado) {
}
