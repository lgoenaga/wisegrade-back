package com.wisegrade.auth.api.dto;

public record AuthBulkDocentesResponse(
        int totalDocentes,
        int considerados,
        int creados,
        int omitidosPorExistente,
        int omitidosPorDocumentoInvalido) {
}
