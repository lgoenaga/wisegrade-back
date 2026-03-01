package com.wisegrade.auth.api.dto;

public record AuthBulkEstudiantesResponse(
        int totalEstudiantes,
        int considerados,
        int creados,
        int omitidosPorExistente,
        int omitidosPorDocumentoInvalido) {
}
