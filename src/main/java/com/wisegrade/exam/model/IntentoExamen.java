package com.wisegrade.exam.model;

import com.wisegrade.academic.model.Estudiante;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "intentos_examen", uniqueConstraints = {
        @UniqueConstraint(name = "uk_intento_examen_estudiante", columnNames = { "examen_id", "estudiante_id" })
})
public class IntentoExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "examen_id", nullable = false)
    private Examen examen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private IntentoEstado estado;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "first_submit_attempt_at")
    private LocalDateTime firstSubmitAttemptAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "intento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<IntentoPregunta> preguntas = new LinkedHashSet<>();

    protected IntentoExamen() {
    }

    public IntentoExamen(Examen examen, Estudiante estudiante, LocalDateTime startedAt) {
        this.examen = Objects.requireNonNull(examen, "examen");
        this.estudiante = Objects.requireNonNull(estudiante, "estudiante");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.estado = IntentoEstado.IN_PROGRESS;
    }

    public Long getId() {
        return id;
    }

    public Examen getExamen() {
        return examen;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public IntentoEstado getEstado() {
        return estado;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFirstSubmitAttemptAt() {
        return firstSubmitAttemptAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public Set<IntentoPregunta> getPreguntas() {
        return preguntas;
    }

    public void addPregunta(IntentoPregunta intentoPregunta) {
        Objects.requireNonNull(intentoPregunta, "intentoPregunta");
        intentoPregunta.setIntento(this);
        this.preguntas.add(intentoPregunta);
    }

    public void markFirstSubmitAttempt(LocalDateTime now) {
        if (this.firstSubmitAttemptAt == null) {
            this.firstSubmitAttemptAt = Objects.requireNonNull(now, "now");
        }
    }

    public void submit(LocalDateTime now) {
        Objects.requireNonNull(now, "now");
        if (this.estado != IntentoEstado.SUBMITTED) {
            this.estado = IntentoEstado.SUBMITTED;
        }
        if (this.submittedAt == null) {
            this.submittedAt = now;
        }
    }
}
