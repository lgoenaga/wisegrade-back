package com.wisegrade.academic.api;

import com.wisegrade.academic.api.dto.EstudianteCreateRequest;
import com.wisegrade.academic.api.dto.EstudianteResponse;
import com.wisegrade.academic.api.dto.EstudianteUpdateRequest;
import com.wisegrade.academic.service.EstudianteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public List<EstudianteResponse> list() {
        return estudianteService.list();
    }

    @GetMapping("/{id}")
    public EstudianteResponse get(@PathVariable long id) {
        return estudianteService.get(id);
    }

    @PostMapping
    public ResponseEntity<EstudianteResponse> create(@Valid @RequestBody EstudianteCreateRequest request,
            UriComponentsBuilder ucb) {
        EstudianteResponse created = estudianteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(ucb.path("/api/estudiantes/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    public EstudianteResponse update(@PathVariable long id, @Valid @RequestBody EstudianteUpdateRequest request) {
        return estudianteService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        estudianteService.delete(id);
    }
}
