package com.wisegrade.exam.api.dto;

import java.util.List;

public record ExamenGeneratedResponse(
        long examenId,
        long periodoId,
        long materiaId,
        long momentoId,
        long docenteResponsableId,
        int cantidad,
        List<PreguntaGeneratedResponse> preguntas) {
}
