package com.wisegrade.exam.api;

import com.wisegrade.exam.api.dto.ExamenBankLoadRequest;
import com.wisegrade.exam.api.dto.ExamenBankLoadResponse;
import com.wisegrade.exam.api.dto.ExamenGenerateRequest;
import com.wisegrade.exam.api.dto.ExamenGeneratedResponse;
import com.wisegrade.exam.api.dto.ExamenResultadosResponse;
import com.wisegrade.exam.service.ExamenService;
import com.wisegrade.exam.service.ExamenResultadosService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/examenes")
public class ExamenController {

    private final ExamenService examenService;
    private final ExamenResultadosService examenResultadosService;

    public ExamenController(ExamenService examenService, ExamenResultadosService examenResultadosService) {
        this.examenService = examenService;
        this.examenResultadosService = examenResultadosService;
    }

    @PostMapping("/banco")
    @ResponseStatus(HttpStatus.OK)
    public ExamenBankLoadResponse loadBank(@Valid @RequestBody ExamenBankLoadRequest request) {
        return examenService.loadBank(request);
    }

    @PostMapping("/generar")
    @ResponseStatus(HttpStatus.OK)
    public ExamenGeneratedResponse generate(@Valid @RequestBody ExamenGenerateRequest request) {
        return examenService.generate(request);
    }

    @GetMapping("/resultados")
    @ResponseStatus(HttpStatus.OK)
    public ExamenResultadosResponse resultados(
            @RequestParam long periodoId,
            @RequestParam long materiaId,
            @RequestParam long momentoId,
            @RequestParam long docenteResponsableId) {
        return examenResultadosService.getResultados(periodoId, materiaId, momentoId, docenteResponsableId);
    }
}
