package com.wisegrade.academic.service;

import com.wisegrade.academic.api.dto.DocenteCreateRequest;
import com.wisegrade.academic.api.dto.DocenteResponse;
import com.wisegrade.academic.api.dto.DocenteUpdateRequest;
import com.wisegrade.academic.model.Docente;
import com.wisegrade.academic.repository.DocenteRepository;
import com.wisegrade.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocenteService {

    private final DocenteRepository docenteRepository;

    public DocenteService(DocenteRepository docenteRepository) {
        this.docenteRepository = docenteRepository;
    }

    @Transactional(readOnly = true)
    public List<DocenteResponse> list() {
        return docenteRepository.findAll().stream().map(DocenteService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DocenteResponse get(long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public DocenteResponse create(DocenteCreateRequest request) {
        boolean activo = request.activo() == null || request.activo();
        Docente docente = new Docente(request.nombres(), request.apellidos(), request.documento(), activo);
        Docente saved = docenteRepository.save(docente);
        return toResponse(saved);
    }

    @Transactional
    public DocenteResponse update(long id, DocenteUpdateRequest request) {
        Docente docente = getEntity(id);
        docente.setNombres(request.nombres());
        docente.setApellidos(request.apellidos());
        docente.setDocumento(request.documento());
        docente.setActivo(request.activo());
        return toResponse(docente);
    }

    @Transactional
    public void delete(long id) {
        if (!docenteRepository.existsById(id)) {
            throw new NotFoundException("Docente not found: " + id);
        }
        docenteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Docente getEntity(long id) {
        return docenteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Docente not found: " + id));
    }

    private static DocenteResponse toResponse(Docente docente) {
        return new DocenteResponse(docente.getId(), docente.getNombres(), docente.getApellidos(),
                docente.getDocumento(), docente.isActivo());
    }
}
