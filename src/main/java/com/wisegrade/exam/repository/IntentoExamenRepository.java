package com.wisegrade.exam.repository;

import com.wisegrade.exam.model.IntentoExamen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntentoExamenRepository extends JpaRepository<IntentoExamen, Long> {
    Optional<IntentoExamen> findByExamenIdAndEstudianteId(Long examenId, Long estudianteId);
}
