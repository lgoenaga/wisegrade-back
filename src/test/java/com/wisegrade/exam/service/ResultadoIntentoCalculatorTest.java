package com.wisegrade.exam.service;

import com.wisegrade.academic.model.Docente;
import com.wisegrade.academic.model.Materia;
import com.wisegrade.academic.model.Momento;
import com.wisegrade.academic.model.Nivel;
import com.wisegrade.academic.model.Periodo;
import com.wisegrade.exam.api.dto.ResultadoIntentoResponse;
import com.wisegrade.exam.model.Examen;
import com.wisegrade.exam.model.IntentoPregunta;
import com.wisegrade.exam.model.Pregunta;
import com.wisegrade.exam.model.RespuestaCorrecta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultadoIntentoCalculatorTest {

    @Test
    void calculaNotaSinBeneficioSobreTotalOriginal() {
        ResultadoIntentoResponse resultado = ResultadoIntentoCalculator.calcular(
                intentoPreguntas(List.of(true, true, false)),
                false);

        assertThat(resultado.correctas()).isEqualTo(2);
        assertThat(resultado.total()).isEqualTo(3);
        assertThat(resultado.notaSobre5()).isEqualByComparingTo(new BigDecimal("3.33"));
    }

    @Test
    void calculaNotaConBeneficioSobreTotalAjustado() {
        ResultadoIntentoResponse resultado = ResultadoIntentoCalculator.calcular(
                intentoPreguntas(List.of(true, true, false)),
                true);

        assertThat(resultado.correctas()).isEqualTo(2);
        assertThat(resultado.total()).isEqualTo(3);
        assertThat(resultado.notaSobre5()).isEqualByComparingTo(new BigDecimal("5.00"));
    }

    @Test
    void limitaDescuentoDelBeneficioAHastaCincoIncorrectas() {
        ResultadoIntentoResponse resultado = ResultadoIntentoCalculator.calcular(
                intentoPreguntas(List.of(true, true, true, true, false, false, false, false, false, false)),
                true);

        assertThat(resultado.correctas()).isEqualTo(4);
        assertThat(resultado.total()).isEqualTo(10);
        assertThat(resultado.notaSobre5()).isEqualByComparingTo(new BigDecimal("4.00"));
    }

    @Test
    void devuelveCeroSiElTotalAjustadoNoEsPositivo() {
        ResultadoIntentoResponse resultado = ResultadoIntentoCalculator.calcular(
                intentoPreguntas(List.of(false)),
                true);

        assertThat(resultado.correctas()).isZero();
        assertThat(resultado.total()).isEqualTo(1);
        assertThat(resultado.notaSobre5()).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    private static List<IntentoPregunta> intentoPreguntas(List<Boolean> respuestasCorrectas) {
        Examen examen = new Examen(
                new Periodo(2026, "P1"),
                new Materia("Backend", new Nivel("Tecnico")),
                new Momento("Parcial"),
                new Docente("Doc", "Test", "123", true));

        List<IntentoPregunta> intentoPreguntas = new ArrayList<>();
        for (int index = 0; index < respuestasCorrectas.size(); index++) {
            boolean correcta = respuestasCorrectas.get(index);
            Pregunta pregunta = new Pregunta(
                    "Pregunta " + index,
                    "A",
                    "B",
                    "C",
                    "D",
                    RespuestaCorrecta.A);
            examen.addPregunta(pregunta);

            IntentoPregunta intentoPregunta = new IntentoPregunta(pregunta, index + 1);
            intentoPregunta.responder(correcta ? RespuestaCorrecta.A : RespuestaCorrecta.B, LocalDateTime.now());
            intentoPreguntas.add(intentoPregunta);
        }
        return intentoPreguntas;
    }
}