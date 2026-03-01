package com.wisegrade.exam.api.dto;

import com.wisegrade.exam.model.RespuestaCorrecta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PreguntaCreateRequest(
        @NotBlank String enunciado,
        @NotBlank String opcionA,
        @NotBlank String opcionB,
        @NotBlank String opcionC,
        @NotBlank String opcionD,
        @NotNull RespuestaCorrecta correcta,
        String explicacion) {
}
