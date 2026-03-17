package com.wisegrade.exam.service;

import com.wisegrade.academic.model.Docente;
import com.wisegrade.academic.model.Materia;
import com.wisegrade.academic.model.Momento;
import com.wisegrade.academic.model.Periodo;
import com.wisegrade.academic.repository.DocenteRepository;
import com.wisegrade.academic.repository.MateriaRepository;
import com.wisegrade.academic.repository.MomentoRepository;
import com.wisegrade.academic.repository.PeriodoRepository;
import com.wisegrade.common.BadRequestException;
import com.wisegrade.common.NotFoundException;
import com.wisegrade.exam.api.dto.ExamenBankLoadRequest;
import com.wisegrade.exam.api.dto.ExamenBankLoadResponse;
import com.wisegrade.exam.api.dto.ExamenEnsureRequest;
import com.wisegrade.exam.api.dto.ExamenEnsureResponse;
import com.wisegrade.exam.api.dto.ExamenGenerateRequest;
import com.wisegrade.exam.api.dto.ExamenGeneratedResponse;
import com.wisegrade.exam.api.dto.PreguntaCreateRequest;
import com.wisegrade.exam.api.dto.PreguntaGeneratedResponse;
import com.wisegrade.exam.model.Examen;
import com.wisegrade.exam.model.Pregunta;
import com.wisegrade.exam.repository.ExamenRepository;
import com.wisegrade.exam.repository.PreguntaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ExamenService {

        private final ExamenRepository examenRepository;
        private final PreguntaRepository preguntaRepository;
        private final PeriodoRepository periodoRepository;
        private final MateriaRepository materiaRepository;
        private final MomentoRepository momentoRepository;
        private final DocenteRepository docenteRepository;

        public ExamenService(
                        ExamenRepository examenRepository,
                        PreguntaRepository preguntaRepository,
                        PeriodoRepository periodoRepository,
                        MateriaRepository materiaRepository,
                        MomentoRepository momentoRepository,
                        DocenteRepository docenteRepository) {
                this.examenRepository = examenRepository;
                this.preguntaRepository = preguntaRepository;
                this.periodoRepository = periodoRepository;
                this.materiaRepository = materiaRepository;
                this.momentoRepository = momentoRepository;
                this.docenteRepository = docenteRepository;
        }

        @Transactional
        public ExamenBankLoadResponse loadBank(ExamenBankLoadRequest request) {
                ExamenFindOrCreateResult result = findOrCreateExamen(
                                request.periodoId(),
                                request.materiaId(),
                                request.momentoId(),
                                request.docenteResponsableId());
                Examen examen = result.examen();

                Set<String> existingEnunciados = new HashSet<>();
                for (String enunciado : preguntaRepository.findEnunciadosByExamenId(examen.getId())) {
                        if (enunciado != null) {
                                existingEnunciados.add(normalize(enunciado));
                        }
                }

                int added = 0;
                int skipped = 0;

                List<Pregunta> nuevas = new ArrayList<>();
                for (PreguntaCreateRequest p : request.preguntas()) {
                        String enunciado = normalize(p.enunciado());
                        if (existingEnunciados.contains(enunciado)) {
                                skipped++;
                                continue;
                        }

                        Pregunta pregunta = new Pregunta(
                                        enunciado,
                                        normalize(p.opcionA()),
                                        normalize(p.opcionB()),
                                        normalize(p.opcionC()),
                                        normalize(p.opcionD()),
                                        p.correcta());

                        pregunta.setExplicacion(normalize(p.explicacion()));

                        nuevas.add(pregunta);
                        existingEnunciados.add(enunciado);
                        added++;
                }

                for (Pregunta p : nuevas) {
                        examen.addPregunta(p);
                }

                examenRepository.save(examen);

                long total = preguntaRepository.countByExamenId(examen.getId());
                return new ExamenBankLoadResponse(examen.getId(), request.preguntas().size(), added, skipped, total);
        }

        @Transactional
        public ExamenEnsureResponse ensureExamen(ExamenEnsureRequest request) {
                ExamenFindOrCreateResult result = findOrCreateExamen(
                                request.periodoId(),
                                request.materiaId(),
                                request.momentoId(),
                                request.docenteResponsableId());

                if (request.beneficio() != null) {
                        result.examen().setBeneficio(request.beneficio());
                }

                long total = preguntaRepository.countByExamenId(result.examen().getId());
                return new ExamenEnsureResponse(
                                result.examen().getId(),
                                result.created(),
                                total,
                                result.examen().isBeneficio());
        }

        @Transactional(readOnly = true)
        public ExamenGeneratedResponse generate(ExamenGenerateRequest request) {
                int cantidad = request.cantidad() == null ? 10 : request.cantidad();
                if (cantidad <= 0) {
                        throw new BadRequestException("cantidad must be > 0");
                }

                Examen examen = examenRepository
                                .findByPeriodoIdAndMateriaIdAndMomentoIdAndDocenteResponsableId(
                                                request.periodoId(),
                                                request.materiaId(),
                                                request.momentoId(),
                                                request.docenteResponsableId())
                                .orElseThrow(() -> new NotFoundException(
                                                "Examen not found for (periodoId, materiaId, momentoId, docenteResponsableId)"));

                validateDocenteAsociadoAMateria(examen.getMateria(), examen.getDocenteResponsable().getId());

                List<Pregunta> preguntas = new ArrayList<>(preguntaRepository.findAllByExamenId(examen.getId()));
                if (preguntas.size() < cantidad) {
                        throw new BadRequestException(
                                        "Not enough questions in bank: have " + preguntas.size() + ", need "
                                                        + cantidad);
                }

                preguntas.sort((a, b) -> Long.compare(a.getId(), b.getId()));
                java.util.Collections.shuffle(preguntas, ThreadLocalRandom.current());

                List<PreguntaGeneratedResponse> seleccion = preguntas.subList(0, cantidad).stream()
                                .map(p -> new PreguntaGeneratedResponse(
                                                p.getId(),
                                                p.getEnunciado(),
                                                List.of(p.getOpcionA(), p.getOpcionB(), p.getOpcionC(),
                                                                p.getOpcionD())))
                                .toList();

                return new ExamenGeneratedResponse(
                                examen.getId(),
                                examen.getPeriodo().getId(),
                                examen.getMateria().getId(),
                                examen.getMomento().getId(),
                                examen.getDocenteResponsable().getId(),
                                cantidad,
                                seleccion);
        }

        private void validateDocenteAsociadoAMateria(Materia materia, Long docenteId) {
                boolean asociado = materia.getDocentes().stream().anyMatch(d -> d.getId().equals(docenteId));
                if (!asociado) {
                        throw new BadRequestException(
                                        "DocenteResponsable must be associated to Materia (materiaId=" + materia.getId()
                                                        + ", docenteId=" + docenteId + ")");
                }
        }

        private static String normalize(String s) {
                return s == null ? null : s.trim();
        }

        private record ExamenFindOrCreateResult(Examen examen, boolean created) {
        }

        private ExamenFindOrCreateResult findOrCreateExamen(
                        long periodoId,
                        long materiaId,
                        long momentoId,
                        long docenteResponsableId) {

                Periodo periodo = periodoRepository.findById(periodoId)
                                .orElseThrow(() -> new NotFoundException("Periodo not found: " + periodoId));

                Materia materia = materiaRepository.findById(materiaId)
                                .orElseThrow(() -> new NotFoundException("Materia not found: " + materiaId));

                Momento momento = momentoRepository.findById(momentoId)
                                .orElseThrow(() -> new NotFoundException("Momento not found: " + momentoId));

                Docente docente = docenteRepository.findById(docenteResponsableId)
                                .orElseThrow(() -> new NotFoundException("Docente not found: " + docenteResponsableId));

                validateDocenteAsociadoAMateria(materia, docente.getId());

                return examenRepository
                                .findByPeriodoIdAndMateriaIdAndMomentoIdAndDocenteResponsableId(
                                                periodoId,
                                                materiaId,
                                                momentoId,
                                                docenteResponsableId)
                                .map(ex -> new ExamenFindOrCreateResult(ex, false))
                                .orElseGet(() -> {
                                        try {
                                                Examen created = examenRepository.saveAndFlush(
                                                                new Examen(periodo, materia, momento, docente));
                                                return new ExamenFindOrCreateResult(created, true);
                                        } catch (DataIntegrityViolationException e) {
                                                return examenRepository
                                                                .findByPeriodoIdAndMateriaIdAndMomentoIdAndDocenteResponsableId(
                                                                                periodoId,
                                                                                materiaId,
                                                                                momentoId,
                                                                                docenteResponsableId)
                                                                .map(ex -> new ExamenFindOrCreateResult(ex, false))
                                                                .orElseThrow(() -> e);
                                        }
                                });
        }
}
