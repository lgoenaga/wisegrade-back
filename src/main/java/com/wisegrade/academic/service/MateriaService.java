package com.wisegrade.academic.service;

import com.wisegrade.academic.api.dto.DocenteResponse;
import com.wisegrade.academic.api.dto.MateriaCreateRequest;
import com.wisegrade.academic.api.dto.MateriaResponse;
import com.wisegrade.academic.api.dto.MateriaUpdateRequest;
import com.wisegrade.academic.model.Docente;
import com.wisegrade.academic.model.Materia;
import com.wisegrade.academic.model.Nivel;
import com.wisegrade.academic.repository.DocenteRepository;
import com.wisegrade.academic.repository.MateriaRepository;
import com.wisegrade.academic.repository.NivelRepository;
import com.wisegrade.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class MateriaService {

    private final MateriaRepository materiaRepository;
    private final NivelRepository nivelRepository;
    private final DocenteRepository docenteRepository;

    public MateriaService(MateriaRepository materiaRepository, NivelRepository nivelRepository,
            DocenteRepository docenteRepository) {
        this.materiaRepository = materiaRepository;
        this.nivelRepository = nivelRepository;
        this.docenteRepository = docenteRepository;
    }

    @Transactional(readOnly = true)
    public List<MateriaResponse> list() {
        return materiaRepository.findAll().stream().map(MateriaService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MateriaResponse get(long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public MateriaResponse create(MateriaCreateRequest request) {
        Nivel nivel = nivelRepository.findById(request.nivelId())
                .orElseThrow(() -> new NotFoundException("Nivel not found: " + request.nivelId()));
        Materia saved = materiaRepository.save(new Materia(request.nombre(), nivel));
        return toResponse(saved);
    }

    @Transactional
    public MateriaResponse update(long id, MateriaUpdateRequest request) {
        Materia materia = getEntity(id);
        Nivel nivel = nivelRepository.findById(request.nivelId())
                .orElseThrow(() -> new NotFoundException("Nivel not found: " + request.nivelId()));
        materia.setNombre(request.nombre());
        materia.setNivel(nivel);
        return toResponse(materia);
    }

    @Transactional
    public void delete(long id) {
        if (!materiaRepository.existsById(id)) {
            throw new NotFoundException("Materia not found: " + id);
        }
        materiaRepository.deleteById(id);
    }

    @Transactional
    public MateriaResponse addDocente(long materiaId, long docenteId) {
        Materia materia = getEntity(materiaId);
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new NotFoundException("Docente not found: " + docenteId));
        materia.addDocente(docente);
        return toResponse(materia);
    }

    @Transactional
    public MateriaResponse removeDocente(long materiaId, long docenteId) {
        Materia materia = getEntity(materiaId);
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new NotFoundException("Docente not found: " + docenteId));
        materia.removeDocente(docente);
        return toResponse(materia);
    }

    @Transactional(readOnly = true)
    public List<DocenteResponse> listDocentes(long materiaId) {
        Materia materia = getEntity(materiaId);
        return materia.getDocentes().stream()
                .map(d -> new DocenteResponse(d.getId(), d.getNombres(), d.getApellidos(), d.getDocumento(),
                        d.isActivo()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Materia getEntity(long id) {
        return materiaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Materia not found: " + id));
    }

    private static MateriaResponse toResponse(Materia materia) {
        Set<Long> docenteIds = materia.getDocentes().stream().map(Docente::getId)
                .collect(java.util.stream.Collectors.toSet());
        return new MateriaResponse(materia.getId(), materia.getNombre(), materia.getNivel().getId(), docenteIds);
    }
}
