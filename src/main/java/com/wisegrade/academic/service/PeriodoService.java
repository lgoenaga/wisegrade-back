package com.wisegrade.academic.service;

import com.wisegrade.academic.api.dto.PeriodoCreateRequest;
import com.wisegrade.academic.api.dto.PeriodoResponse;
import com.wisegrade.academic.api.dto.PeriodoUpdateRequest;
import com.wisegrade.academic.model.Periodo;
import com.wisegrade.academic.repository.PeriodoRepository;
import com.wisegrade.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PeriodoService {

    private final PeriodoRepository periodoRepository;

    public PeriodoService(PeriodoRepository periodoRepository) {
        this.periodoRepository = periodoRepository;
    }

    @Transactional(readOnly = true)
    public List<PeriodoResponse> list() {
        return periodoRepository.findAll().stream().map(PeriodoService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PeriodoResponse get(long id) {
        Periodo periodo = periodoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Periodo not found: " + id));
        return toResponse(periodo);
    }

    @Transactional
    public PeriodoResponse create(PeriodoCreateRequest request) {
        Periodo saved = periodoRepository.save(new Periodo(request.anio(), request.nombre()));
        return toResponse(saved);
    }

    @Transactional
    public PeriodoResponse update(long id, PeriodoUpdateRequest request) {
        Periodo periodo = periodoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Periodo not found: " + id));
        periodo.setAnio(request.anio());
        periodo.setNombre(request.nombre());
        return toResponse(periodo);
    }

    @Transactional
    public void delete(long id) {
        if (!periodoRepository.existsById(id)) {
            throw new NotFoundException("Periodo not found: " + id);
        }
        periodoRepository.deleteById(id);
    }

    private static PeriodoResponse toResponse(Periodo periodo) {
        return new PeriodoResponse(periodo.getId(), periodo.getAnio(), periodo.getNombre());
    }
}
