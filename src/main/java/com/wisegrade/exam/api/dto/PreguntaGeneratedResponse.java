package com.wisegrade.exam.api.dto;

import java.util.List;

public record PreguntaGeneratedResponse(
        long id,
        String enunciado,
        List<String> opciones) {
}
