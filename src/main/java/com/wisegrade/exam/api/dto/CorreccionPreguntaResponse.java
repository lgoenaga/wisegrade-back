package com.wisegrade.exam.api.dto;

import com.wisegrade.exam.model.RespuestaCorrecta;

public record CorreccionPreguntaResponse(
        long preguntaId,
        RespuestaCorrecta respuestaEstudiante,
        RespuestaCorrecta respuestaCorrecta,
        boolean esCorrecta,
        String explicacion) {
}
