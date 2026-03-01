package com.wisegrade.auth.service;

import com.wisegrade.academic.model.Docente;
import com.wisegrade.academic.model.Estudiante;
import com.wisegrade.academic.repository.DocenteRepository;
import com.wisegrade.academic.repository.EstudianteRepository;
import com.wisegrade.auth.api.dto.AuthMeResponse;
import com.wisegrade.auth.api.dto.AuthPersonaResponse;
import com.wisegrade.auth.security.AuthPrincipal;
import org.springframework.stereotype.Service;

@Service
public class AuthMeService {

    private final EstudianteRepository estudianteRepository;
    private final DocenteRepository docenteRepository;

    public AuthMeService(EstudianteRepository estudianteRepository, DocenteRepository docenteRepository) {
        this.estudianteRepository = estudianteRepository;
        this.docenteRepository = docenteRepository;
    }

    public AuthMeResponse toMe(AuthPrincipal principal) {
        AuthPersonaResponse estudiante = null;
        AuthPersonaResponse docente = null;

        if (principal.getEstudianteId() != null) {
            Estudiante e = estudianteRepository.findById(principal.getEstudianteId()).orElse(null);
            if (e != null) {
                estudiante = new AuthPersonaResponse(e.getId(), e.getDocumento(), e.getNombres(), e.getApellidos());
            }
        }

        if (principal.getDocenteId() != null) {
            Docente d = docenteRepository.findById(principal.getDocenteId()).orElse(null);
            if (d != null) {
                docente = new AuthPersonaResponse(d.getId(), d.getDocumento(), d.getNombres(), d.getApellidos());
            }
        }

        return new AuthMeResponse(principal.getDocumento(), principal.getRol(), estudiante, docente);
    }
}
