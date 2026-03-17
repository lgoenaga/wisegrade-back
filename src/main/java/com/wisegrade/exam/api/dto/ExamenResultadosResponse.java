package com.wisegrade.exam.api.dto;

import java.util.List;

public record ExamenResultadosResponse(
                Long examenId,
                Long periodoId,
                Long materiaId,
                Long momentoId,
                Long docenteResponsableId,
                boolean beneficio,
                List<ExamenResultadoFilaResponse> filas) {
}
