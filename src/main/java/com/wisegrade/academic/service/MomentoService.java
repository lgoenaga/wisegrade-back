package com.wisegrade.academic.service;

import com.wisegrade.academic.api.dto.MomentoCreateRequest;
import com.wisegrade.academic.api.dto.MomentoResponse;
import com.wisegrade.academic.api.dto.MomentoUpdateRequest;
import com.wisegrade.academic.model.Momento;
import com.wisegrade.academic.repository.MomentoRepository;
import com.wisegrade.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MomentoService {

    private final MomentoRepository momentoRepository;

    public MomentoService(MomentoRepository momentoRepository) {
        this.momentoRepository = momentoRepository;
    }

    @Transactional(readOnly = true)
    public List<MomentoResponse> list() {
        return momentoRepository.findAll().stream().map(MomentoService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MomentoResponse get(long id) {
        Momento momento = momentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Momento not found: " + id));
        return toResponse(momento);
    }

    @Transactional
    public MomentoResponse create(MomentoCreateRequest request) {
        Momento saved = momentoRepository.save(new Momento(request.nombre()));
        return toResponse(saved);
    }

    @Transactional
    public MomentoResponse update(long id, MomentoUpdateRequest request) {
        Momento momento = momentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Momento not found: " + id));
        momento.setNombre(request.nombre());
        return toResponse(momento);
    }

    @Transactional
    public void delete(long id) {
        if (!momentoRepository.existsById(id)) {
            throw new NotFoundException("Momento not found: " + id);
        }
        momentoRepository.deleteById(id);
    }

    private static MomentoResponse toResponse(Momento momento) {
        return new MomentoResponse(momento.getId(), momento.getNombre());
    }
}
