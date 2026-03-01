package com.wisegrade.auth.service;

import com.wisegrade.academic.model.Docente;
import com.wisegrade.academic.model.Estudiante;
import com.wisegrade.academic.repository.DocenteRepository;
import com.wisegrade.academic.repository.EstudianteRepository;
import com.wisegrade.auth.api.dto.AuthBulkEstudiantesRequest;
import com.wisegrade.auth.api.dto.AuthBulkEstudiantesResponse;
import com.wisegrade.auth.api.dto.AuthUserCreateRequest;
import com.wisegrade.auth.model.UserRole;
import com.wisegrade.auth.model.Usuario;
import com.wisegrade.auth.repository.UsuarioRepository;
import com.wisegrade.common.BadRequestException;
import com.wisegrade.common.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthUserService {

    private final UsuarioRepository usuarioRepository;
    private final DocenteRepository docenteRepository;
    private final EstudianteRepository estudianteRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthUserService(
            UsuarioRepository usuarioRepository,
            DocenteRepository docenteRepository,
            EstudianteRepository estudianteRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.docenteRepository = docenteRepository;
        this.estudianteRepository = estudianteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario createUser(AuthUserCreateRequest req) {
        String documento = req.documento().trim();

        if (usuarioRepository.findByDocumento(documento).isPresent()) {
            throw new BadRequestException("Ya existe un usuario con documento: " + documento);
        }

        boolean activo = req.activo() == null || req.activo();
        UserRole rol = req.rol();

        Long docenteId = req.docenteId();
        Long estudianteId = req.estudianteId();

        if (rol == UserRole.ADMIN) {
            if (docenteId != null || estudianteId != null) {
                throw new BadRequestException("ADMIN no debe tener docenteId/estudianteId");
            }
        } else if (rol == UserRole.DOCENTE) {
            if (docenteId == null || estudianteId != null) {
                throw new BadRequestException("DOCENTE requiere docenteId y no debe tener estudianteId");
            }
            Docente d = docenteRepository.findById(docenteId)
                    .orElseThrow(() -> new NotFoundException("Docente not found: " + docenteId));
            if (!documento.equals(d.getDocumento())) {
                throw new BadRequestException("Documento de usuario debe coincidir con el documento del docente");
            }
        } else if (rol == UserRole.ESTUDIANTE) {
            if (estudianteId == null || docenteId != null) {
                throw new BadRequestException("ESTUDIANTE requiere estudianteId y no debe tener docenteId");
            }
            Estudiante e = estudianteRepository.findById(estudianteId)
                    .orElseThrow(() -> new NotFoundException("Estudiante not found: " + estudianteId));
            if (!documento.equals(e.getDocumento())) {
                throw new BadRequestException("Documento de usuario debe coincidir con el documento del estudiante");
            }
        }

        String hash = passwordEncoder.encode(req.clave());
        return usuarioRepository.save(new Usuario(documento, hash, rol, docenteId, estudianteId, activo));
    }

    @Transactional
    public AuthBulkEstudiantesResponse bulkCreateEstudianteUsers(AuthBulkEstudiantesRequest req) {
        boolean soloActivos = req != null && Boolean.TRUE.equals(req.soloActivos());
        boolean activoUsuario = req == null || req.activoUsuario() == null || req.activoUsuario();
        boolean skipExisting = req == null || req.skipExisting() == null || req.skipExisting();

        var estudiantes = estudianteRepository.findAll();
        int total = estudiantes.size();

        int considerados = 0;
        int creados = 0;
        int omitidosPorExistente = 0;
        int omitidosPorDocumentoInvalido = 0;

        for (Estudiante e : estudiantes) {
            if (soloActivos && !e.isActivo()) {
                continue;
            }

            String documentoRaw = e.getDocumento();
            String documento = documentoRaw == null ? "" : documentoRaw.trim();
            if (documento.isBlank()) {
                omitidosPorDocumentoInvalido++;
                continue;
            }

            considerados++;

            if (usuarioRepository.findByDocumento(documento).isPresent()) {
                if (skipExisting) {
                    omitidosPorExistente++;
                    continue;
                }
                throw new BadRequestException("Ya existe un usuario con documento: " + documento);
            }

            // Requisito solicitado: clave = documento (encriptado).
            String hash = passwordEncoder.encode(documento);
            usuarioRepository.save(new Usuario(
                    documento,
                    hash,
                    UserRole.ESTUDIANTE,
                    null,
                    e.getId(),
                    activoUsuario));
            creados++;
        }

        return new AuthBulkEstudiantesResponse(
                total,
                considerados,
                creados,
                omitidosPorExistente,
                omitidosPorDocumentoInvalido);
    }
}
