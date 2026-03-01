package com.wisegrade.academic.repository;

import com.wisegrade.academic.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocenteRepository extends JpaRepository<Docente, Long> {
}
