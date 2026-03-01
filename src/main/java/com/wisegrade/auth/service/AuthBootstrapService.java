package com.wisegrade.auth.service;

import com.wisegrade.auth.model.UserRole;
import com.wisegrade.auth.model.Usuario;
import com.wisegrade.auth.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthBootstrapService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private final String adminDocumento;
    private final String adminPassword;

    public AuthBootstrapService(
            UsuarioRepository usuarioRepository,
            @Value("${wisegrade.auth.admin.documento:ADMIN}") String adminDocumento,
            @Value("${wisegrade.auth.admin.password:Wisegrade2026}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.adminDocumento = adminDocumento;
        this.adminPassword = adminPassword;
    }

    @Transactional
    public void ensureAdminUser() {
        usuarioRepository.findByDocumento(adminDocumento)
                .orElseGet(() -> usuarioRepository.save(
                        new Usuario(
                                adminDocumento,
                                passwordEncoder.encode(adminPassword),
                                UserRole.ADMIN,
                                null,
                                null,
                                true)));
    }
}
