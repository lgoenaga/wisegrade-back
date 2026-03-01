package com.wisegrade.exam.service;

import com.wisegrade.academic.model.Docente;
import com.wisegrade.academic.model.Estudiante;
import com.wisegrade.academic.model.Materia;
import com.wisegrade.academic.repository.DocenteRepository;
import com.wisegrade.academic.repository.EstudianteRepository;
import com.wisegrade.academic.repository.MateriaRepository;
import com.wisegrade.academic.repository.MomentoRepository;
import com.wisegrade.academic.repository.PeriodoRepository;
import com.wisegrade.common.BadRequestException;
import com.wisegrade.common.NotFoundException;
import com.wisegrade.exam.api.dto.IntentoDetalleResponse;
import com.wisegrade.exam.api.dto.IntentoEnviarRequest;
import com.wisegrade.exam.api.dto.IntentoEnviarResponse;
import com.wisegrade.exam.api.dto.IntentoIniciarRequest;
import com.wisegrade.exam.api.dto.IntentoIniciarResponse;
import com.wisegrade.exam.api.dto.PreguntaGeneratedResponse;
import com.wisegrade.exam.api.dto.CorreccionPreguntaResponse;
import com.wisegrade.exam.api.dto.ResultadoIntentoResponse;
import com.wisegrade.exam.api.dto.RespuestaGuardadaResponse;
import com.wisegrade.exam.api.dto.RespuestaEnviarRequest;
import com.wisegrade.exam.model.Examen;
import com.wisegrade.exam.model.IntentoEstado;
import com.wisegrade.exam.model.IntentoExamen;
import com.wisegrade.exam.model.IntentoPregunta;
import com.wisegrade.exam.model.Pregunta;
import com.wisegrade.exam.model.RespuestaCorrecta;
import com.wisegrade.exam.repository.ExamenRepository;
import com.wisegrade.exam.repository.IntentoExamenRepository;
import com.wisegrade.exam.repository.IntentoPreguntaRepository;
import com.wisegrade.exam.repository.PreguntaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class IntentoExamenService {

        private final ExamenRepository examenRepository;
        private final PreguntaRepository preguntaRepository;
        private final IntentoExamenRepository intentoExamenRepository;
        private final IntentoPreguntaRepository intentoPreguntaRepository;
        private final PeriodoRepository periodoRepository;
        private final MateriaRepository materiaRepository;
        private final MomentoRepository momentoRepository;
        private final DocenteRepository docenteRepository;
        private final EstudianteRepository estudianteRepository;

        public IntentoExamenService(
                        ExamenRepository examenRepository,
                        PreguntaRepository preguntaRepository,
                        IntentoExamenRepository intentoExamenRepository,
                        IntentoPreguntaRepository intentoPreguntaRepository,
                        PeriodoRepository periodoRepository,
                        MateriaRepository materiaRepository,
                        MomentoRepository momentoRepository,
                        DocenteRepository docenteRepository,
                        EstudianteRepository estudianteRepository) {
                this.examenRepository = examenRepository;
                this.preguntaRepository = preguntaRepository;
                this.intentoExamenRepository = intentoExamenRepository;
                this.intentoPreguntaRepository = intentoPreguntaRepository;
                this.periodoRepository = periodoRepository;
                this.materiaRepository = materiaRepository;
                this.momentoRepository = momentoRepository;
                this.docenteRepository = docenteRepository;
                this.estudianteRepository = estudianteRepository;
        }

        @Transactional
        public IntentoIniciarResponse iniciar(IntentoIniciarRequest request) {
                int cantidad = request.cantidad() == null ? 10 : request.cantidad();
                if (cantidad <= 0) {
                        throw new BadRequestException("cantidad must be > 0");
                }

                periodoRepository.findById(request.periodoId())
                                .orElseThrow(() -> new NotFoundException("Periodo not found: " + request.periodoId()));

                Materia materia = materiaRepository.findById(request.materiaId())
                                .orElseThrow(() -> new NotFoundException("Materia not found: " + request.materiaId()));

                momentoRepository.findById(request.momentoId())
                                .orElseThrow(() -> new NotFoundException("Momento not found: " + request.momentoId()));

                Docente docente = docenteRepository.findById(request.docenteResponsableId())
                                .orElseThrow(() -> new NotFoundException(
                                                "Docente not found: " + request.docenteResponsableId()));

                validateDocenteAsociadoAMateria(materia, docente.getId());

                Examen examen = examenRepository
                                .findByPeriodoIdAndMateriaIdAndMomentoIdAndDocenteResponsableId(
                                                request.periodoId(),
                                                request.materiaId(),
                                                request.momentoId(),
                                                request.docenteResponsableId())
                                .orElseThrow(() -> new NotFoundException(
                                                "Examen not found for (periodoId, materiaId, momentoId, docenteResponsableId)"));

                Estudiante estudiante = estudianteRepository.findById(request.estudianteId())
                                .orElseThrow(() -> new NotFoundException(
                                                "Estudiante not found: " + request.estudianteId()));

                // "iniciar" should be idempotent: if an attempt already exists for (examen,
                // estudiante),
                // return it so the student can resume instead of throwing an error.
                var existingOpt = intentoExamenRepository.findByExamenIdAndEstudianteId(examen.getId(),
                                estudiante.getId());
                if (existingOpt.isPresent()) {
                        IntentoExamen existing = existingOpt.get();

                        List<IntentoPregunta> intentoPreguntas = intentoPreguntaRepository
                                        .findAllByIntento_IdOrderByOrdenAsc(existing.getId());

                        List<PreguntaGeneratedResponse> preguntas = intentoPreguntas.stream()
                                        .map(ip -> {
                                                Pregunta p = ip.getPregunta();
                                                return new PreguntaGeneratedResponse(
                                                                p.getId(),
                                                                p.getEnunciado(),
                                                                List.of(p.getOpcionA(), p.getOpcionB(), p.getOpcionC(),
                                                                                p.getOpcionD()));
                                        })
                                        .toList();

                        return new IntentoIniciarResponse(
                                        existing.getId(),
                                        examen.getId(),
                                        estudiante.getId(),
                                        existing.getEstado(),
                                        existing.getStartedAt(),
                                        preguntas.size(),
                                        preguntas);
                }

                List<Pregunta> banco = new ArrayList<>(preguntaRepository.findAllByExamenId(examen.getId()));
                if (banco.size() < cantidad) {
                        throw new BadRequestException(
                                        "Not enough questions in bank: have " + banco.size() + ", need " + cantidad);
                }

                banco.sort((a, b) -> Long.compare(a.getId(), b.getId()));
                java.util.Collections.shuffle(banco, ThreadLocalRandom.current());
                List<Pregunta> seleccion = banco.subList(0, cantidad);

                LocalDateTime now = LocalDateTime.now();
                IntentoExamen intento = new IntentoExamen(examen, estudiante, now);
                for (int i = 0; i < seleccion.size(); i++) {
                        intento.addPregunta(new IntentoPregunta(seleccion.get(i), i + 1));
                }

                IntentoExamen saved = intentoExamenRepository.save(intento);

                List<PreguntaGeneratedResponse> preguntas = seleccion.stream()
                                .map(p -> new PreguntaGeneratedResponse(
                                                p.getId(),
                                                p.getEnunciado(),
                                                List.of(p.getOpcionA(), p.getOpcionB(), p.getOpcionC(),
                                                                p.getOpcionD())))
                                .toList();

                return new IntentoIniciarResponse(
                                saved.getId(),
                                examen.getId(),
                                estudiante.getId(),
                                saved.getEstado(),
                                saved.getStartedAt(),
                                cantidad,
                                preguntas);
        }

        @Transactional(readOnly = true)
        public IntentoDetalleResponse getDetalle(long intentoId) {
                IntentoExamen intento = intentoExamenRepository.findById(intentoId)
                                .orElseThrow(() -> new NotFoundException("Intento not found: " + intentoId));

                List<IntentoPregunta> intentoPreguntas = intentoPreguntaRepository
                                .findAllByIntento_IdOrderByOrdenAsc(intento.getId());

                List<PreguntaGeneratedResponse> preguntas = intentoPreguntas.stream()
                                .map(ip -> {
                                        Pregunta p = ip.getPregunta();
                                        return new PreguntaGeneratedResponse(
                                                        p.getId(),
                                                        p.getEnunciado(),
                                                        List.of(p.getOpcionA(), p.getOpcionB(), p.getOpcionC(),
                                                                        p.getOpcionD()));
                                })
                                .toList();

                List<RespuestaGuardadaResponse> respuestas = intentoPreguntas.stream()
                                .filter(ip -> ip.getRespuesta() != null)
                                .map(ip -> new RespuestaGuardadaResponse(
                                                ip.getPregunta().getId(),
                                                ip.getRespuesta(),
                                                ip.getRespondedAt()))
                                .toList();

                ResultadoIntentoResponse resultado = null;
                List<CorreccionPreguntaResponse> correccion = List.of();
                if (intento.getEstado() == IntentoEstado.SUBMITTED) {
                        resultado = ResultadoIntentoCalculator.calcular(intentoPreguntas);
                        correccion = intentoPreguntas.stream()
                                        .map(ip -> {
                                                RespuestaCorrecta respuestaEstudiante = ip.getRespuesta();
                                                RespuestaCorrecta respuestaCorrecta = ip.getPregunta().getCorrecta();
                                                boolean esCorrecta = respuestaEstudiante != null
                                                                && respuestaEstudiante == respuestaCorrecta;
                                                return new CorreccionPreguntaResponse(
                                                                ip.getPregunta().getId(),
                                                                respuestaEstudiante,
                                                                respuestaCorrecta,
                                                                esCorrecta,
                                                                ip.getPregunta().getExplicacion());
                                        })
                                        .toList();
                }

                return new IntentoDetalleResponse(
                                intento.getId(),
                                intento.getExamen().getId(),
                                intento.getEstudiante().getId(),
                                intento.getEstado(),
                                intento.getStartedAt(),
                                intento.getFirstSubmitAttemptAt(),
                                intento.getSubmittedAt(),
                                preguntas.size(),
                                preguntas,
                                respuestas,
                                resultado,
                                correccion);
        }

        @Transactional
        public IntentoEnviarResponse enviar(IntentoEnviarRequest request) {
                IntentoExamen intento = intentoExamenRepository.findById(request.intentoId())
                                .orElseThrow(() -> new NotFoundException("Intento not found: " + request.intentoId()));

                LocalDateTime now = LocalDateTime.now();
                intento.markFirstSubmitAttempt(now);

                if (intento.getEstado() == IntentoEstado.SUBMITTED) {
                        return new IntentoEnviarResponse(
                                        intento.getId(),
                                        intento.getEstado(),
                                        intento.getFirstSubmitAttemptAt(),
                                        intento.getSubmittedAt(),
                                        0);
                }

                List<IntentoPregunta> intentoPreguntas = intentoPreguntaRepository
                                .findAllByIntento_IdOrderByOrdenAsc(intento.getId());

                Map<Long, IntentoPregunta> byPreguntaId = new HashMap<>();
                for (IntentoPregunta ip : intentoPreguntas) {
                        byPreguntaId.put(ip.getPregunta().getId(), ip);
                }

                int savedAnswers = 0;
                List<RespuestaEnviarRequest> respuestas = request.respuestas() == null ? List.of()
                                : request.respuestas();
                for (RespuestaEnviarRequest r : respuestas) {
                        IntentoPregunta ip = byPreguntaId.get(r.preguntaId());
                        if (ip == null) {
                                throw new BadRequestException(
                                                "Pregunta does not belong to this attempt: preguntaId="
                                                                + r.preguntaId());
                        }
                        ip.responder(r.respuesta(), now);
                        savedAnswers++;
                }

                intento.submit(now);

                return new IntentoEnviarResponse(
                                intento.getId(),
                                intento.getEstado(),
                                intento.getFirstSubmitAttemptAt(),
                                intento.getSubmittedAt(),
                                savedAnswers);
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
