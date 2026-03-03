package com.wisegrade.exam.api.dto;

import com.wisegrade.exam.model.IntentoEstado;

import java.time.LocalDateTime;

public record ExamenResultadoFilaResponse(
                Long intentoId,
                IntentoEstado estado,
                EstudianteResumenResponse estudiante,
                LocalDateTime startedAt,
                LocalDateTime submittedAt,
                ResultadoIntentoResponse resultado) {
}
