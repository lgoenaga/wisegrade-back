package com.wisegrade.exam.api;

import com.wisegrade.exam.api.dto.IntentoEnviarRequest;
import com.wisegrade.exam.api.dto.IntentoEnviarResponse;
import com.wisegrade.exam.api.dto.IntentoIniciarRequest;
import com.wisegrade.exam.api.dto.IntentoIniciarResponse;
import com.wisegrade.exam.service.IntentoExamenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/intentos")
public class IntentoExamenController {

    private final IntentoExamenService intentoExamenService;

    public IntentoExamenController(IntentoExamenService intentoExamenService) {
        this.intentoExamenService = intentoExamenService;
    }

    @PostMapping("/iniciar")
    @ResponseStatus(HttpStatus.OK)
    public IntentoIniciarResponse iniciar(@Valid @RequestBody IntentoIniciarRequest request) {
        return intentoExamenService.iniciar(request);
    }

    @PostMapping("/enviar")
    @ResponseStatus(HttpStatus.OK)
    public IntentoEnviarResponse enviar(@Valid @RequestBody IntentoEnviarRequest request) {
        return intentoExamenService.enviar(request);
    }
}
