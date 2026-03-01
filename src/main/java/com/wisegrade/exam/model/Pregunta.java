package com.wisegrade.exam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "preguntas")
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "examen_id", nullable = false)
    private Examen examen;

    @Column(name = "enunciado", nullable = false, length = 1000)
    private String enunciado;

    @Column(name = "opcion_a", nullable = false, length = 500)
    private String opcionA;

    @Column(name = "opcion_b", nullable = false, length = 500)
    private String opcionB;

    @Column(name = "opcion_c", nullable = false, length = 500)
    private String opcionC;

    @Column(name = "opcion_d", nullable = false, length = 500)
    private String opcionD;

    @Convert(converter = RespuestaCorrectaConverter.class)
    @Column(name = "correcta", nullable = false, length = 1)
    private RespuestaCorrecta correcta;

    @Column(name = "explicacion", columnDefinition = "TEXT")
    private String explicacion;

    protected Pregunta() {
    }

    public Pregunta(String enunciado, String opcionA, String opcionB, String opcionC, String opcionD,
            RespuestaCorrecta correcta) {
        this.enunciado = Objects.requireNonNull(enunciado, "enunciado");
        this.opcionA = Objects.requireNonNull(opcionA, "opcionA");
        this.opcionB = Objects.requireNonNull(opcionB, "opcionB");
        this.opcionC = Objects.requireNonNull(opcionC, "opcionC");
        this.opcionD = Objects.requireNonNull(opcionD, "opcionD");
        this.correcta = Objects.requireNonNull(correcta, "correcta");
    }

    public void setExplicacion(String explicacion) {
        this.explicacion = explicacion;
    }

    public Long getId() {
        return id;
    }

    public Examen getExamen() {
        return examen;
    }

    void setExamen(Examen examen) {
        this.examen = examen;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String getOpcionA() {
        return opcionA;
    }

    public String getOpcionB() {
        return opcionB;
    }

    public String getOpcionC() {
        return opcionC;
    }

    public String getOpcionD() {
        return opcionD;
    }

    public RespuestaCorrecta getCorrecta() {
        return correcta;
    }

    public String getExplicacion() {
        return explicacion;
    }
}
