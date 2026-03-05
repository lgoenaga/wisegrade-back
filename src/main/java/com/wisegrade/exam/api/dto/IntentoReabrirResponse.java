package com.wisegrade.exam.api.dto;

import com.wisegrade.exam.model.IntentoEstado;

import java.time.LocalDateTime;

public record IntentoReabrirResponse(
        long intentoId,
        IntentoEstado estado,
        LocalDateTime deadlineAt,
        int reopenCount,
        int extraMinutesTotal) {
}
