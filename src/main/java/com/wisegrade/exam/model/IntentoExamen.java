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

    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    @Column(name = "first_submit_attempt_at")
    private LocalDateTime firstSubmitAttemptAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "block_reason", length = 255)
    private String blockReason;

    @Column(name = "reopened_at")
    private LocalDateTime reopenedAt;

    @Column(name = "reopen_count", nullable = false)
    private int reopenCount;

    @Column(name = "extra_minutes_total", nullable = false)
    private int extraMinutesTotal;

    @OneToMany(mappedBy = "intento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<IntentoPregunta> preguntas = new LinkedHashSet<>();

    protected IntentoExamen() {
    }

    public IntentoExamen(Examen examen, Estudiante estudiante, LocalDateTime startedAt, LocalDateTime deadlineAt) {
        this.examen = Objects.requireNonNull(examen, "examen");
        this.estudiante = Objects.requireNonNull(estudiante, "estudiante");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.deadlineAt = Objects.requireNonNull(deadlineAt, "deadlineAt");
        this.estado = IntentoEstado.IN_PROGRESS;
        this.reopenCount = 0;
        this.extraMinutesTotal = 0;
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

    public LocalDateTime getDeadlineAt() {
        return deadlineAt;
    }

    public LocalDateTime getFirstSubmitAttemptAt() {
        return firstSubmitAttemptAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public LocalDateTime getReopenedAt() {
        return reopenedAt;
    }

    public int getReopenCount() {
        return reopenCount;
    }

    public int getExtraMinutesTotal() {
        return extraMinutesTotal;
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

    public void ensureDeadline(LocalDateTime deadlineAt) {
        if (this.deadlineAt == null) {
            this.deadlineAt = Objects.requireNonNull(deadlineAt, "deadlineAt");
        }
    }

    public void block(LocalDateTime now, String reason) {
        Objects.requireNonNull(now, "now");
        if (this.estado == IntentoEstado.SUBMITTED) {
            return;
        }
        if (this.estado != IntentoEstado.BLOCKED) {
            this.estado = IntentoEstado.BLOCKED;
        }
        if (this.blockedAt == null) {
            this.blockedAt = now;
        }
        if (this.blockReason == null || this.blockReason.isBlank()) {
            this.blockReason = reason;
        }
    }

    public void reopen(LocalDateTime now, int extraMinutes) {
        Objects.requireNonNull(now, "now");
        if (extraMinutes <= 0) {
            throw new IllegalArgumentException("extraMinutes must be > 0");
        }
        if (this.estado != IntentoEstado.BLOCKED) {
            throw new IllegalStateException("Attempt is not BLOCKED");
        }
        if (this.reopenCount >= 1) {
            throw new IllegalStateException("Attempt was already reopened");
        }

        this.estado = IntentoEstado.IN_PROGRESS;
        this.reopenedAt = now;
        this.reopenCount = this.reopenCount + 1;
        this.extraMinutesTotal = this.extraMinutesTotal + extraMinutes;
        if (this.deadlineAt != null) {
            this.deadlineAt = this.deadlineAt.plusMinutes(extraMinutes);
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
