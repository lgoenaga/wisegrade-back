package com.wisegrade.exam.api.dto;

import com.wisegrade.exam.model.RespuestaCorrecta;

import java.time.LocalDateTime;

public record RespuestaGuardadaResponse(
        long preguntaId,
        RespuestaCorrecta respuesta,
        LocalDateTime respondedAt) {
}
