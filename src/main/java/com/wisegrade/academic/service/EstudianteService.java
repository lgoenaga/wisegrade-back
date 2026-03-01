package com.wisegrade.academic.service;

import com.wisegrade.academic.api.dto.EstudianteCreateRequest;
import com.wisegrade.academic.api.dto.EstudianteResponse;
import com.wisegrade.academic.api.dto.EstudianteUpdateRequest;
import com.wisegrade.academic.model.Estudiante;
import com.wisegrade.academic.repository.EstudianteRepository;
import com.wisegrade.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    @Transactional(readOnly = true)
    public List<EstudianteResponse> list() {
        return estudianteRepository.findAll().stream().map(EstudianteService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EstudianteResponse get(long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public EstudianteResponse create(EstudianteCreateRequest request) {
        boolean activo = request.activo() == null || request.activo();
        Estudiante estudiante = new Estudiante(request.nombres(), request.apellidos(), request.documento(), activo);
        Estudiante saved = estudianteRepository.save(estudiante);
        return toResponse(saved);
    }

    @Transactional
    public EstudianteResponse update(long id, EstudianteUpdateRequest request) {
        Estudiante estudiante = getEntity(id);
        estudiante.setNombres(request.nombres());
        estudiante.setApellidos(request.apellidos());
        estudiante.setDocumento(request.documento());
        estudiante.setActivo(request.activo());
        return toResponse(estudiante);
    }

    @Transactional
    public void delete(long id) {
        if (!estudianteRepository.existsById(id)) {
            throw new NotFoundException("Estudiante not found: " + id);
        }
        estudianteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Estudiante getEntity(long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Estudiante not found: " + id));
    }

    private static EstudianteResponse toResponse(Estudiante estudiante) {
        return new EstudianteResponse(estudiante.getId(), estudiante.getNombres(), estudiante.getApellidos(),
                estudiante.getDocumento(), estudiante.isActivo());
    }
}
