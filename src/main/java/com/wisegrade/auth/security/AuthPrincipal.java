package com.wisegrade.auth.security;

import com.wisegrade.auth.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthPrincipal implements UserDetails {

    private final Usuario usuario;

    public AuthPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Long getUsuarioId() {
        return usuario.getId();
    }

    public String getDocumento() {
        return usuario.getDocumento();
    }

    public String getRol() {
        return usuario.getRol().name();
    }

    public Long getDocenteId() {
        return usuario.getDocenteId();
    }

    public Long getEstudianteId() {
        return usuario.getEstudianteId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return usuario.getDocumento();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.isActivo();
    }
}
