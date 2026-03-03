package com.wisegrade.exam.api;

import com.wisegrade.auth.security.AuthPrincipal;
import com.wisegrade.exam.api.dto.IntentoDetalleResponse;
import com.wisegrade.exam.api.dto.IntentoEnviarRequest;
import com.wisegrade.exam.api.dto.IntentoEnviarResponse;
import com.wisegrade.exam.api.dto.IntentoIniciarRequest;
import com.wisegrade.exam.api.dto.IntentoIniciarResponse;
import com.wisegrade.exam.service.IntentoExamenService;
import com.wisegrade.exam.service.IntentoPdfExportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final IntentoPdfExportService intentoPdfExportService;

    public IntentoExamenController(
            IntentoExamenService intentoExamenService,
            IntentoPdfExportService intentoPdfExportService) {
        this.intentoExamenService = intentoExamenService;
        this.intentoPdfExportService = intentoPdfExportService;
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

    @GetMapping(value = "/{intentoId}/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','ESTUDIANTE')")
    public ResponseEntity<byte[]> exportPdf(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable long intentoId) {
        byte[] pdf = intentoPdfExportService.exportSubmittedIntentoPdf(principal, intentoId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"examen-intento-" + intentoId + ".pdf\"");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @PostMapping("/enviar")
    @PreAuthorize("hasAnyRole('ADMIN','ESTUDIANTE')")
    @ResponseStatus(HttpStatus.OK)
    public IntentoEnviarResponse enviar(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody IntentoEnviarRequest request) {
        return intentoExamenService.enviar(principal, request);
    }

    @DeleteMapping("/{intentoId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable long intentoId) {
        intentoExamenService.deleteIntento(principal, intentoId);
    }
}
