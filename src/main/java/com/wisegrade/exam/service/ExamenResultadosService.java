package com.wisegrade.exam.service;

import com.wisegrade.academic.model.Docente;
import com.wisegrade.academic.model.Estudiante;
import com.wisegrade.academic.model.Materia;
import com.wisegrade.academic.repository.DocenteRepository;
import com.wisegrade.academic.repository.MateriaRepository;
import com.wisegrade.academic.repository.MomentoRepository;
import com.wisegrade.academic.repository.PeriodoRepository;
import com.wisegrade.common.BadRequestException;
import com.wisegrade.common.NotFoundException;
import com.wisegrade.exam.api.dto.EstudianteResumenResponse;
import com.wisegrade.exam.api.dto.ExamenResultadoFilaResponse;
import com.wisegrade.exam.api.dto.ExamenResultadosResponse;
import com.wisegrade.exam.api.dto.ResultadoIntentoResponse;
import com.wisegrade.exam.model.Examen;
import com.wisegrade.exam.model.IntentoEstado;
import com.wisegrade.exam.model.IntentoExamen;
import com.wisegrade.exam.model.IntentoPregunta;
import com.wisegrade.exam.repository.ExamenRepository;
import com.wisegrade.exam.repository.IntentoExamenRepository;
import com.wisegrade.exam.repository.IntentoPreguntaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExamenResultadosService {

        private final PeriodoRepository periodoRepository;
        private final MateriaRepository materiaRepository;
        private final MomentoRepository momentoRepository;
        private final DocenteRepository docenteRepository;
        private final ExamenRepository examenRepository;
        private final IntentoExamenRepository intentoExamenRepository;
        private final IntentoPreguntaRepository intentoPreguntaRepository;

        public ExamenResultadosService(
                        PeriodoRepository periodoRepository,
                        MateriaRepository materiaRepository,
                        MomentoRepository momentoRepository,
                        DocenteRepository docenteRepository,
                        ExamenRepository examenRepository,
                        IntentoExamenRepository intentoExamenRepository,
                        IntentoPreguntaRepository intentoPreguntaRepository) {
                this.periodoRepository = periodoRepository;
                this.materiaRepository = materiaRepository;
                this.momentoRepository = momentoRepository;
                this.docenteRepository = docenteRepository;
                this.examenRepository = examenRepository;
                this.intentoExamenRepository = intentoExamenRepository;
                this.intentoPreguntaRepository = intentoPreguntaRepository;
        }

        @Transactional(readOnly = true)
        public ExamenResultadosResponse getResultados(long periodoId, long materiaId, long momentoId,
                        long docenteResponsableId, boolean includeInProgress) {

                periodoRepository.findById(periodoId)
                                .orElseThrow(() -> new NotFoundException("Periodo not found: " + periodoId));

                Materia materia = materiaRepository.findById(materiaId)
                                .orElseThrow(() -> new NotFoundException("Materia not found: " + materiaId));

                momentoRepository.findById(momentoId)
                                .orElseThrow(() -> new NotFoundException("Momento not found: " + momentoId));

                Docente docente = docenteRepository.findById(docenteResponsableId)
                                .orElseThrow(() -> new NotFoundException("Docente not found: " + docenteResponsableId));

                validateDocenteAsociadoAMateria(materia, docente.getId());

                Examen examen = examenRepository
                                .findByPeriodoIdAndMateriaIdAndMomentoIdAndDocenteResponsableId(periodoId, materiaId,
                                                momentoId,
                                                docenteResponsableId)
                                .orElseThrow(() -> new NotFoundException(
                                                "Examen not found for (periodoId, materiaId, momentoId, docenteResponsableId)"));

                List<IntentoExamen> intentos;
                if (includeInProgress) {
                        intentos = intentoExamenRepository.findAllByExamenIdAndEstadoInFetchEstudiante(
                                        examen.getId(),
                                        List.of(IntentoEstado.SUBMITTED, IntentoEstado.IN_PROGRESS,
                                                        IntentoEstado.BLOCKED));
                } else {
                        intentos = intentoExamenRepository.findAllByExamenIdAndEstadoFetchEstudiante(
                                        examen.getId(),
                                        IntentoEstado.SUBMITTED);
                }

                if (intentos.isEmpty()) {
                        return new ExamenResultadosResponse(
                                        examen.getId(),
                                        periodoId,
                                        materiaId,
                                        momentoId,
                                        docenteResponsableId,
                                        List.of());
                }

                Map<Long, List<IntentoPregunta>> preguntasByIntentoId = new HashMap<>();
                List<Long> submittedIntentoIds = intentos.stream()
                                .filter(i -> i.getEstado() == IntentoEstado.SUBMITTED)
                                .map(IntentoExamen::getId)
                                .toList();
                if (!submittedIntentoIds.isEmpty()) {
                        List<IntentoPregunta> allPreguntas = intentoPreguntaRepository
                                        .findAllByIntentoIdInFetchPreguntaOrderByIntentoIdAscOrdenAsc(
                                                        submittedIntentoIds);

                        for (IntentoPregunta ip : allPreguntas) {
                                Long intentoId = ip.getIntentoId();
                                if (intentoId == null) {
                                        continue;
                                }
                                preguntasByIntentoId.computeIfAbsent(intentoId, k -> new ArrayList<>()).add(ip);
                        }
                }

                List<ExamenResultadoFilaResponse> filas = intentos.stream()
                                .map(i -> {
                                        Estudiante e = i.getEstudiante();
                                        EstudianteResumenResponse estudiante = new EstudianteResumenResponse(
                                                        e.getId(),
                                                        e.getNombres(),
                                                        e.getApellidos(),
                                                        e.getDocumento());

                                        List<IntentoPregunta> ips = preguntasByIntentoId.getOrDefault(i.getId(),
                                                        List.of());
                                        ResultadoIntentoResponse resultado = null;
                                        if (i.getEstado() == IntentoEstado.SUBMITTED) {
                                                resultado = ResultadoIntentoCalculator.calcular(ips);
                                        }

                                        return new ExamenResultadoFilaResponse(
                                                        i.getId(),
                                                        i.getEstado(),
                                                        estudiante,
                                                        i.getStartedAt(),
                                                        i.getDeadlineAt(),
                                                        i.getBlockedAt(),
                                                        i.getReopenCount(),
                                                        i.getExtraMinutesTotal(),
                                                        i.getSubmittedAt(),
                                                        resultado);
                                })
                                .toList();

                return new ExamenResultadosResponse(
                                examen.getId(),
                                periodoId,
                                materiaId,
                                momentoId,
                                docenteResponsableId,
                                filas);
        }

        private void validateDocenteAsociadoAMateria(Materia materia, Long docenteId) {
                boolean asociado = materia.getDocentes().stream().anyMatch(d -> d.getId().equals(docenteId));
                if (!asociado) {
                        throw new BadRequestException(
                                        "DocenteResponsable must be associated to Materia (materiaId=" + materia.getId()
                                                        + ", docenteId=" + docenteId + ")");
                }
        }
}
