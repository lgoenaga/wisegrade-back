package com.wisegrade.exam.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record IntentoEnviarRequest(
        long intentoId,
        @NotEmpty List<@Valid RespuestaEnviarRequest> respuestas) {
}
