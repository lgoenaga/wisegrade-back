package com.wisegrade.exam.repository;

import com.wisegrade.exam.model.IntentoExamen;
import com.wisegrade.exam.model.IntentoEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IntentoExamenRepository extends JpaRepository<IntentoExamen, Long> {
    Optional<IntentoExamen> findByExamenIdAndEstudianteId(Long examenId, Long estudianteId);

    @Query("""
            select i
            from IntentoExamen i
            join fetch i.estudiante e
            where i.examen.id = :examenId
                and i.estado = :estado
            order by i.submittedAt asc, i.id asc
            """)
    List<IntentoExamen> findAllByExamenIdAndEstadoFetchEstudiante(
            @Param("examenId") Long examenId,
            @Param("estado") IntentoEstado estado);

    @Query("""
            select i
            from IntentoExamen i
            join fetch i.estudiante e
            where i.examen.id = :examenId
            and i.estado in :estados
            order by i.submittedAt asc, i.id asc
            """)
    List<IntentoExamen> findAllByExamenIdAndEstadoInFetchEstudiante(
            @Param("examenId") Long examenId,
            @Param("estados") List<IntentoEstado> estados);
}
