package com.wisegrade.exam.api.dto;

import com.wisegrade.exam.model.IntentoEstado;

import java.time.LocalDateTime;
import java.util.List;

public record IntentoDetalleResponse(
        long intentoId,
        long examenId,
        long estudianteId,
        IntentoEstado estado,
        LocalDateTime startedAt,
        LocalDateTime firstSubmitAttemptAt,
        LocalDateTime submittedAt,
        int cantidad,
        List<PreguntaGeneratedResponse> preguntas,
        List<RespuestaGuardadaResponse> respuestas) {
}
