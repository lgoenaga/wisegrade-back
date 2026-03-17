package com.wisegrade.exam.service;

import com.wisegrade.exam.api.dto.ResultadoIntentoResponse;
import com.wisegrade.exam.model.IntentoPregunta;
import com.wisegrade.exam.model.RespuestaCorrecta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

final class ResultadoIntentoCalculator {

    private static final BigDecimal MAX_NOTA = BigDecimal.valueOf(5);

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

        BigDecimal notaSobre5;
        if (total <= 0) {
            notaSobre5 = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            int totalAjustado = total;
            if (beneficio) {
                int incorrectas = total - correctas;
                int descuento = Math.min(5, incorrectas);
                totalAjustado = total - descuento;
            }

            if (totalAjustado <= 0) {
                notaSobre5 = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            } else {
                notaSobre5 = BigDecimal.valueOf(correctas)
                        .multiply(MAX_NOTA)
                        .divide(BigDecimal.valueOf(totalAjustado), 2, RoundingMode.HALF_UP);
            }
        }

        return new ResultadoIntentoResponse(correctas, total, notaSobre5);
    }
}
