package com.wisegrade.academic.api;

import com.wisegrade.academic.api.dto.DocenteCreateRequest;
import com.wisegrade.academic.api.dto.DocenteResponse;
import com.wisegrade.academic.api.dto.DocenteUpdateRequest;
import com.wisegrade.academic.service.DocenteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    private final DocenteService docenteService;

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    @GetMapping
    public List<DocenteResponse> list() {
        return docenteService.list();
    }

    @GetMapping("/{id}")
    public DocenteResponse get(@PathVariable long id) {
        return docenteService.get(id);
    }

    @PostMapping
    public ResponseEntity<DocenteResponse> create(@Valid @RequestBody DocenteCreateRequest request,
            UriComponentsBuilder ucb) {
        DocenteResponse created = docenteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(ucb.path("/api/docentes/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    public DocenteResponse update(@PathVariable long id, @Valid @RequestBody DocenteUpdateRequest request) {
        return docenteService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        docenteService.delete(id);
    }
}
