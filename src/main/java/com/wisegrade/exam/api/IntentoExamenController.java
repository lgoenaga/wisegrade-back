package com.wisegrade.exam.api;

import com.wisegrade.auth.security.AuthPrincipal;
import com.wisegrade.exam.api.dto.IntentoDetalleResponse;
import com.wisegrade.exam.api.dto.IntentoEnviarRequest;
import com.wisegrade.exam.api.dto.IntentoEnviarResponse;
import com.wisegrade.exam.api.dto.IntentoIniciarRequest;
import com.wisegrade.exam.api.dto.IntentoIniciarResponse;
import com.wisegrade.exam.service.IntentoExamenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @PreAuthorize("hasAnyRole('ADMIN','ESTUDIANTE')")
    @ResponseStatus(HttpStatus.OK)
    public IntentoIniciarResponse iniciar(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody IntentoIniciarRequest request) {
        return intentoExamenService.iniciar(principal, request);
    }

    @GetMapping("/{intentoId}")
    @PreAuthorize("hasAnyRole('ADMIN','ESTUDIANTE')")
    @ResponseStatus(HttpStatus.OK)
    public IntentoDetalleResponse get(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable long intentoId) {
        return intentoExamenService.getDetalle(principal, intentoId);
    }

    @PostMapping("/enviar")
    @PreAuthorize("hasAnyRole('ADMIN','ESTUDIANTE')")
    @ResponseStatus(HttpStatus.OK)
    public IntentoEnviarResponse enviar(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody IntentoEnviarRequest request) {
        return intentoExamenService.enviar(principal, request);
    }
}
