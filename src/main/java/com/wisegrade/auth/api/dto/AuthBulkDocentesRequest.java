package com.wisegrade.auth.api.dto;

public record AuthBulkDocentesRequest(
        Boolean soloActivos,
        Boolean activoUsuario,
        Boolean skipExisting) {
}
