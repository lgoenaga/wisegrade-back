package com.wisegrade.exam.repository;

import com.wisegrade.exam.model.IntentoPregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IntentoPreguntaRepository extends JpaRepository<IntentoPregunta, Long> {
        List<IntentoPregunta> findAllByIntento_IdOrderByOrdenAsc(Long intentoId);

        void deleteByIntento_Id(Long intentoId);

        @Query("""
                        select ip
                        from IntentoPregunta ip
                        join fetch ip.intento i
                        join fetch ip.pregunta p
                        where ip.intento.id in :intentoIds
                        order by ip.intento.id asc, ip.orden asc
                        """)
        List<IntentoPregunta> findAllByIntentoIdInFetchPreguntaOrderByIntentoIdAscOrdenAsc(
                        @Param("intentoIds") List<Long> intentoIds);
}
