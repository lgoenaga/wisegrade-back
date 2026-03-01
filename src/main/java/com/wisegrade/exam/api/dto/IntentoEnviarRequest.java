package com.wisegrade.exam.api.dto;

import jakarta.validation.Valid;

import java.util.List;

public record IntentoEnviarRequest(
                long intentoId,
                List<@Valid RespuestaEnviarRequest> respuestas) {
}
