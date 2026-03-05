package com.wisegrade.exam.api.dto;

import jakarta.validation.Valid;

import java.util.List;

public record IntentoGuardarRequest(
        @Valid List<RespuestaEnviarRequest> respuestas) {
}
