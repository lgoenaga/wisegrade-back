package com.wisegrade.exam.api.dto;

import com.wisegrade.exam.model.IntentoEstado;

import java.time.LocalDateTime;

public record IntentoGuardarResponse(
        long intentoId,
        IntentoEstado estado,
        int savedAnswers,
        LocalDateTime deadlineAt,
        LocalDateTime blockedAt) {
}
