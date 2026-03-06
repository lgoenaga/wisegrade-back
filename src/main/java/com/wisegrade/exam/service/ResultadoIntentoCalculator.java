package com.wisegrade.exam.service;

import com.wisegrade.exam.api.dto.ResultadoIntentoResponse;
import com.wisegrade.exam.model.IntentoPregunta;
import com.wisegrade.exam.model.RespuestaCorrecta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

final class ResultadoIntentoCalculator {

    private ResultadoIntentoCalculator() {
    }

    static ResultadoIntentoResponse calcular(List<IntentoPregunta> intentoPreguntas, boolean beneficio) {
        int total = intentoPreguntas.size();
        int correctas = 0;
        for (IntentoPregunta ip : intentoPreguntas) {
            RespuestaCorrecta r = ip.getRespuesta();
            if (r != null && r == ip.getPregunta().getCorrecta()) {
                correctas++;
            }
        }

        int totalEfectivo = total;
        if (beneficio && total > 0) {
            int incorrectas = total - correctas;
            int descuento = Math.min(5, Math.max(0, incorrectas));
            totalEfectivo = total - descuento;
        }

        BigDecimal notaSobre5;
        if (totalEfectivo <= 0) {
            notaSobre5 = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            notaSobre5 = BigDecimal.valueOf(correctas)
                    .multiply(BigDecimal.valueOf(5))
                    .divide(BigDecimal.valueOf(totalEfectivo), 2, RoundingMode.HALF_UP);
        }

        return new ResultadoIntentoResponse(correctas, total, notaSobre5);
    }
}
