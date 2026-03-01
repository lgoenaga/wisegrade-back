package com.wisegrade.exam.api;

import com.wisegrade.exam.api.dto.ExamenBankLoadRequest;
import com.wisegrade.exam.api.dto.ExamenBankLoadResponse;
import com.wisegrade.exam.api.dto.ExamenGenerateRequest;
import com.wisegrade.exam.api.dto.ExamenGeneratedResponse;
import com.wisegrade.exam.service.ExamenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/examenes")
public class ExamenController {

    private final ExamenService examenService;

    public ExamenController(ExamenService examenService) {
        this.examenService = examenService;
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
}
