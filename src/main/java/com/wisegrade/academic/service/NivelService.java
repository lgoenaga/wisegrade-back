package com.wisegrade.academic.service;

import com.wisegrade.academic.api.dto.NivelCreateRequest;
import com.wisegrade.academic.api.dto.NivelResponse;
import com.wisegrade.academic.api.dto.NivelUpdateRequest;
import com.wisegrade.academic.model.Nivel;
import com.wisegrade.academic.repository.NivelRepository;
import com.wisegrade.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NivelService {

    private final NivelRepository nivelRepository;

    public NivelService(NivelRepository nivelRepository) {
        this.nivelRepository = nivelRepository;
    }

    @Transactional(readOnly = true)
    public List<NivelResponse> list() {
        return nivelRepository.findAll().stream().map(NivelService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public NivelResponse get(long id) {
        Nivel nivel = nivelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nivel not found: " + id));
        return toResponse(nivel);
    }

    @Transactional
    public NivelResponse create(NivelCreateRequest request) {
        Nivel nivel = new Nivel(request.nombre());
        Nivel saved = nivelRepository.save(nivel);
        return toResponse(saved);
    }

    @Transactional
    public NivelResponse update(long id, NivelUpdateRequest request) {
        Nivel nivel = nivelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nivel not found: " + id));
        nivel.setNombre(request.nombre());
        return toResponse(nivel);
    }

    @Transactional
    public void delete(long id) {
        if (!nivelRepository.existsById(id)) {
            throw new NotFoundException("Nivel not found: " + id);
        }
        nivelRepository.deleteById(id);
    }

    private static NivelResponse toResponse(Nivel nivel) {
        return new NivelResponse(nivel.getId(), nivel.getNombre());
    }
}
