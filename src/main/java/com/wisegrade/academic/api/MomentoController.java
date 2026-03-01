package com.wisegrade.academic.api;

import com.wisegrade.academic.api.dto.MomentoCreateRequest;
import com.wisegrade.academic.api.dto.MomentoResponse;
import com.wisegrade.academic.api.dto.MomentoUpdateRequest;
import com.wisegrade.academic.service.MomentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/momentos")
public class MomentoController {

    private final MomentoService momentoService;

    public MomentoController(MomentoService momentoService) {
        this.momentoService = momentoService;
    }

    @GetMapping
    public List<MomentoResponse> list() {
        return momentoService.list();
    }

    @GetMapping("/{id}")
    public MomentoResponse get(@PathVariable long id) {
        return momentoService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MomentoResponse> create(@Valid @RequestBody MomentoCreateRequest request,
            UriComponentsBuilder ucb) {
        MomentoResponse created = momentoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(ucb.path("/api/momentos/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MomentoResponse update(@PathVariable long id, @Valid @RequestBody MomentoUpdateRequest request) {
        return momentoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        momentoService.delete(id);
    }
}
