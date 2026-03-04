package com.wisegrade.exam.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.wisegrade.exam.model.RespuestaCorrecta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PreguntaCreateRequest(
                @NotBlank String enunciado,
                @NotBlank @JsonAlias({
                                "opcion_a" }) String opcionA,
                @NotBlank @JsonAlias({ "opcion_b" }) String opcionB,
                @NotBlank @JsonAlias({ "opcion_c" }) String opcionC,
                @NotBlank @JsonAlias({ "opcion_d" }) String opcionD,
                @NotNull RespuestaCorrecta correcta,
                String explicacion) {
}
