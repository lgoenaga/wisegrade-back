package com.wisegrade.exam.api.dto;

import java.math.BigDecimal;

public record ResultadoIntentoResponse(
        int correctas,
        int total,
        BigDecimal notaSobre5) {
}
