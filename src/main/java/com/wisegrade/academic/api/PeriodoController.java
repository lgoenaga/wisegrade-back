package com.wisegrade.academic.api;

import com.wisegrade.academic.api.dto.PeriodoCreateRequest;
import com.wisegrade.academic.api.dto.PeriodoResponse;
import com.wisegrade.academic.api.dto.PeriodoUpdateRequest;
import com.wisegrade.academic.service.PeriodoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/periodos")
public class PeriodoController {

    private final PeriodoService periodoService;

    public PeriodoController(PeriodoService periodoService) {
        this.periodoService = periodoService;
    }

    @GetMapping
    public List<PeriodoResponse> list() {
        return periodoService.list();
    }

    @GetMapping("/{id}")
    public PeriodoResponse get(@PathVariable long id) {
        return periodoService.get(id);
    }

    @PostMapping
    public ResponseEntity<PeriodoResponse> create(@Valid @RequestBody PeriodoCreateRequest request,
            UriComponentsBuilder ucb) {
        PeriodoResponse created = periodoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(ucb.path("/api/periodos/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    public PeriodoResponse update(@PathVariable long id, @Valid @RequestBody PeriodoUpdateRequest request) {
        return periodoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        periodoService.delete(id);
    }
}
