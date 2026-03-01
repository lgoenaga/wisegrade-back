package com.wisegrade.academic.api;

import com.wisegrade.academic.api.dto.NivelCreateRequest;
import com.wisegrade.academic.api.dto.NivelResponse;
import com.wisegrade.academic.api.dto.NivelUpdateRequest;
import com.wisegrade.academic.service.NivelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/niveles")
public class NivelController {

    private final NivelService nivelService;

    public NivelController(NivelService nivelService) {
        this.nivelService = nivelService;
    }

    @GetMapping
    public List<NivelResponse> list() {
        return nivelService.list();
    }

    @GetMapping("/{id}")
    public NivelResponse get(@PathVariable long id) {
        return nivelService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NivelResponse> create(@Valid @RequestBody NivelCreateRequest request,
            UriComponentsBuilder ucb) {
        NivelResponse created = nivelService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(ucb.path("/api/niveles/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public NivelResponse update(@PathVariable long id, @Valid @RequestBody NivelUpdateRequest request) {
        return nivelService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        nivelService.delete(id);
    }
}
