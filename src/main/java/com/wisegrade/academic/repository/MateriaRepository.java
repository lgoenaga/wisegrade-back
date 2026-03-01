package com.wisegrade.academic.repository;

import com.wisegrade.academic.model.Materia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MateriaRepository extends JpaRepository<Materia, Long> {
}
