package com.wisegrade.exam.api.dto;

import com.wisegrade.exam.model.RespuestaCorrecta;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RespuestaEnviarRequest(
        @Min(1) long preguntaId,
        @NotNull RespuestaCorrecta respuesta) {
}
