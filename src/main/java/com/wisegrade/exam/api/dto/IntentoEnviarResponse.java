package com.wisegrade.exam.api.dto;

import com.wisegrade.exam.model.IntentoEstado;

import java.time.LocalDateTime;

public record IntentoEnviarResponse(
        Long intentoId,
        IntentoEstado estado,
        LocalDateTime firstSubmitAttemptAt,
        LocalDateTime submittedAt,
        Integer respuestasGuardadas) {
}
