package com.wisegrade.exam.repository;

import com.wisegrade.exam.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    List<Pregunta> findAllByExamenId(long examenId);

    @Query("select p.enunciado from Pregunta p where p.examen.id = :examenId")
    List<String> findEnunciadosByExamenId(@Param("examenId") long examenId);

    long countByExamenId(long examenId);
}
