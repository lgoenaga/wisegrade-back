package com.wisegrade.academic.api.dto;

import java.util.Set;

public record MateriaResponse(
        Long id,
        String nombre,
        Long nivelId,
        Set<Long> docenteIds) {
}
