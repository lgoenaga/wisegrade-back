package com.wisegrade.academic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "periodos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_periodos_anio_nombre", columnNames = { "anio", "nombre" })
})
public class Periodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anio", nullable = false)
    private int anio;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    protected Periodo() {
    }

    public Periodo(int anio, String nombre) {
        this.anio = anio;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
