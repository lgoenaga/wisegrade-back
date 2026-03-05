package com.wisegrade.exam.api.dto;

import com.wisegrade.exam.model.IntentoEstado;

import java.time.LocalDateTime;
import java.util.List;

public record IntentoIniciarResponse(
                Long intentoId,
                Long examenId,
                Long estudianteId,
                IntentoEstado estado,
                LocalDateTime startedAt,
                LocalDateTime deadlineAt,
                LocalDateTime blockedAt,
                Integer reopenCount,
                Integer extraMinutesTotal,
                Integer cantidad,
                List<PreguntaGeneratedResponse> preguntas) {
}
