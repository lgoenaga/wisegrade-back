package com.wisegrade.exam.api.dto;

public record ExamenBankLoadResponse(
        long examenId,
        int preguntasRecibidas,
        int preguntasAgregadas,
        int preguntasOmitidas,
        long totalBanco) {
}
