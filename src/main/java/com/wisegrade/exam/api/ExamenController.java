package com.wisegrade.exam.api;

import com.wisegrade.auth.model.UserRole;
import com.wisegrade.auth.security.AuthPrincipal;
import com.wisegrade.exam.api.dto.ExamenBankLoadRequest;
import com.wisegrade.exam.api.dto.ExamenBankLoadResponse;
import com.wisegrade.exam.api.dto.ExamenEnsureRequest;
import com.wisegrade.exam.api.dto.ExamenEnsureResponse;
import com.wisegrade.exam.api.dto.ExamenGenerateRequest;
import com.wisegrade.exam.api.dto.ExamenGeneratedResponse;
import com.wisegrade.exam.api.dto.ExamenResultadosResponse;
import com.wisegrade.exam.service.ExamenService;
import com.wisegrade.exam.service.ExamenResultadosService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    @ResponseStatus(HttpStatus.OK)
    public ExamenBankLoadResponse loadBank(
            @Valid @RequestBody ExamenBankLoadRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {

        if (principal != null && principal.getUsuario().getRol() == UserRole.DOCENTE) {
            Long principalDocenteId = principal.getDocenteId();
            if (principalDocenteId == null) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Usuario docente sin docenteId asociado");
            }
            request = new ExamenBankLoadRequest(
                    request.periodoId(),
                    request.materiaId(),
                    request.momentoId(),
                    principalDocenteId,
                    request.preguntas());
        }

        return examenService.loadBank(request);
    }

    @PostMapping("/asegurar")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    @ResponseStatus(HttpStatus.OK)
    public ExamenEnsureResponse asegurar(
            @Valid @RequestBody ExamenEnsureRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {

        if (principal != null && principal.getUsuario().getRol() == UserRole.DOCENTE) {
            Long principalDocenteId = principal.getDocenteId();
            if (principalDocenteId == null) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Usuario docente sin docenteId asociado");
            }
            request = new ExamenEnsureRequest(
                    request.periodoId(),
                    request.materiaId(),
                    request.momentoId(),
                    principalDocenteId);
        }

        return examenService.ensureExamen(request);
    }

    @PostMapping("/generar")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public ExamenGeneratedResponse generate(@Valid @RequestBody ExamenGenerateRequest request) {
        return examenService.generate(request);
    }

    @GetMapping("/resultados")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    @ResponseStatus(HttpStatus.OK)
    public ExamenResultadosResponse resultados(
            @RequestParam long periodoId,
            @RequestParam long materiaId,
            @RequestParam long momentoId,
            @RequestParam long docenteResponsableId,
            @RequestParam(required = false, defaultValue = "false") boolean includeInProgress,
            @AuthenticationPrincipal AuthPrincipal principal) {

        if (principal != null && principal.getUsuario().getRol() == UserRole.DOCENTE) {
            Long principalDocenteId = principal.getDocenteId();
            if (principalDocenteId == null) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Usuario docente sin docenteId asociado");
            }
            docenteResponsableId = principalDocenteId;
        }

        return examenResultadosService.getResultados(periodoId, materiaId, momentoId, docenteResponsableId,
                includeInProgress);
    }
}
