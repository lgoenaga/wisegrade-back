package com.wisegrade.exam.repository;

import com.wisegrade.exam.model.IntentoPregunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntentoPreguntaRepository extends JpaRepository<IntentoPregunta, Long> {
    List<IntentoPregunta> findAllByIntentoIdOrderByOrdenAsc(Long intentoId);
}
