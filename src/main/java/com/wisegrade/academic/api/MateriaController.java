package com.wisegrade.academic.api;

import com.wisegrade.academic.api.dto.DocenteResponse;
import com.wisegrade.academic.api.dto.MateriaCreateRequest;
import com.wisegrade.academic.api.dto.MateriaResponse;
import com.wisegrade.academic.api.dto.MateriaUpdateRequest;
import com.wisegrade.academic.service.MateriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/materias")
public class MateriaController {

    private final MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    public List<MateriaResponse> list() {
        return materiaService.list();
    }

    @GetMapping("/{id}")
    public MateriaResponse get(@PathVariable long id) {
        return materiaService.get(id);
    }

    @PostMapping
    public ResponseEntity<MateriaResponse> create(@Valid @RequestBody MateriaCreateRequest request,
            UriComponentsBuilder ucb) {
        MateriaResponse created = materiaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(ucb.path("/api/materias/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    public MateriaResponse update(@PathVariable long id, @Valid @RequestBody MateriaUpdateRequest request) {
        return materiaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        materiaService.delete(id);
    }

    @GetMapping("/{id}/docentes")
    public List<DocenteResponse> listDocentes(@PathVariable long id) {
        return materiaService.listDocentes(id);
    }

    @PutMapping("/{materiaId}/docentes/{docenteId}")
    public MateriaResponse addDocente(@PathVariable long materiaId, @PathVariable long docenteId) {
        return materiaService.addDocente(materiaId, docenteId);
    }

    @DeleteMapping("/{materiaId}/docentes/{docenteId}")
    public MateriaResponse removeDocente(@PathVariable long materiaId, @PathVariable long docenteId) {
        return materiaService.removeDocente(materiaId, docenteId);
    }
}
