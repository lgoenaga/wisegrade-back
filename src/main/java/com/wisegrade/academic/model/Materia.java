package com.wisegrade.academic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "materias", uniqueConstraints = {
        @UniqueConstraint(name = "uk_materias_nombre", columnNames = { "nombre" })
})
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nivel_id", nullable = false)
    private Nivel nivel;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "materia_docente", joinColumns = @JoinColumn(name = "materia_id"), inverseJoinColumns = @JoinColumn(name = "docente_id"))
    private Set<Docente> docentes = new LinkedHashSet<>();

    protected Materia() {
    }

    public Materia(String nombre, Nivel nivel) {
        this.nombre = nombre;
        this.nivel = nivel;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Nivel getNivel() {
        return nivel;
    }

    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
    }

    public Set<Docente> getDocentes() {
        return docentes;
    }

    public void addDocente(Docente docente) {
        Objects.requireNonNull(docente, "docente");
        this.docentes.add(docente);
    }

    public void removeDocente(Docente docente) {
        Objects.requireNonNull(docente, "docente");
        this.docentes.remove(docente);
    }
}
