package com.wisegrade.exam.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;

public record ExamenEnsureRequest(
                @Min(1) @JsonAlias({
                                "periodo_id" }) long periodoId,
                @Min(1) @JsonAlias({ "materia_id" }) long materiaId,
                @Min(1) @JsonAlias({ "momento_id" }) long momentoId,
                @Min(1) @JsonAlias({ "docente_responsable_id" }) long docenteResponsableId,
                Boolean beneficio) {
}
