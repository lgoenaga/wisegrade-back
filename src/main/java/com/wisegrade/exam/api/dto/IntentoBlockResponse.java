package com.wisegrade.exam.api.dto;

import com.wisegrade.exam.model.IntentoEstado;

import java.time.LocalDateTime;

public record IntentoBlockResponse(
        long intentoId,
        IntentoEstado estado,
        LocalDateTime blockedAt) {
}
