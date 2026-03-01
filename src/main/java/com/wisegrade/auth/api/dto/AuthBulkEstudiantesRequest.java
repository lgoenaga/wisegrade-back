package com.wisegrade.auth.api.dto;

public record AuthBulkEstudiantesRequest(
        Boolean soloActivos,
        Boolean activoUsuario,
        Boolean skipExisting) {
}
