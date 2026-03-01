package com.wisegrade.auth.security;

import com.wisegrade.auth.model.Usuario;
import com.wisegrade.auth.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByDocumento(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return new AuthPrincipal(u);
    }
}
