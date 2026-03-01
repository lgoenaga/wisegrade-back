package com.wisegrade.exam.repository;

import com.wisegrade.exam.model.Examen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamenRepository extends JpaRepository<Examen, Long> {

    Optional<Examen> findByPeriodoIdAndMateriaIdAndMomentoIdAndDocenteResponsableId(
            long periodoId,
            long materiaId,
            long momentoId,
            long docenteResponsableId);
}
