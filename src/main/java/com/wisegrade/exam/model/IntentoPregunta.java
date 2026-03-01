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
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "intento_preguntas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_intento_preguntas", columnNames = { "intento_id", "pregunta_id" })
})
public class IntentoPregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intento_id", nullable = false)
    private IntentoExamen intento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Convert(converter = RespuestaCorrectaConverter.class)
    @Column(name = "respuesta", length = 1)
    private RespuestaCorrecta respuesta;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    protected IntentoPregunta() {
    }

    public IntentoPregunta(Pregunta pregunta, int orden) {
        this.pregunta = Objects.requireNonNull(pregunta, "pregunta");
        this.orden = orden;
    }

    Long getId() {
        return id;
    }

    IntentoExamen getIntento() {
        return intento;
    }

    public Long getIntentoId() {
        return intento == null ? null : intento.getId();
    }

    void setIntento(IntentoExamen intento) {
        this.intento = intento;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public Integer getOrden() {
        return orden;
    }

    public RespuestaCorrecta getRespuesta() {
        return respuesta;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void responder(RespuestaCorrecta respuesta, LocalDateTime respondedAt) {
        this.respuesta = Objects.requireNonNull(respuesta, "respuesta");
        this.respondedAt = Objects.requireNonNull(respondedAt, "respondedAt");
    }
}
