package com.wisegrade.exam.model;

import com.wisegrade.academic.model.Docente;
import com.wisegrade.academic.model.Materia;
import com.wisegrade.academic.model.Momento;
import com.wisegrade.academic.model.Periodo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "examenes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_examen_config", columnNames = { "periodo_id", "materia_id", "momento_id",
                "docente_responsable_id" })
})
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_id", nullable = false)
    private Periodo periodo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "momento_id", nullable = false)
    private Momento momento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_responsable_id", nullable = false)
    private Docente docenteResponsable;

    @OneToMany(mappedBy = "examen", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Pregunta> preguntas = new LinkedHashSet<>();

    protected Examen() {
    }

    public Examen(Periodo periodo, Materia materia, Momento momento, Docente docenteResponsable) {
        this.periodo = Objects.requireNonNull(periodo, "periodo");
        this.materia = Objects.requireNonNull(materia, "materia");
        this.momento = Objects.requireNonNull(momento, "momento");
        this.docenteResponsable = Objects.requireNonNull(docenteResponsable, "docenteResponsable");
    }

    public Long getId() {
        return id;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public Materia getMateria() {
        return materia;
    }

    public Momento getMomento() {
        return momento;
    }

    public Docente getDocenteResponsable() {
        return docenteResponsable;
    }

    public Set<Pregunta> getPreguntas() {
        return preguntas;
    }

    public void addPregunta(Pregunta pregunta) {
        Objects.requireNonNull(pregunta, "pregunta");
        pregunta.setExamen(this);
        this.preguntas.add(pregunta);
    }
}
