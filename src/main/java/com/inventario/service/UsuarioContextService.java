package com.inventario.service;

import com.inventario.model.Sede;
import com.inventario.model.Usuario;
import com.inventario.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UsuarioContextService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioContextService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario usuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return usuarioRepository.findByUsername(authentication.getName()).orElse(null);
    }

    public boolean esSuperUsuario() {
        Usuario usuario = usuarioActual();
        return usuario != null && esRolSuperUsuario(usuario.getRol());
    }

    public boolean esRolSuperUsuario(String rol) {
        String limpio = normalizarRol(rol);
        return "SUPER_ADMIN".equals(limpio);
    }

    public Long sedeIdActual() {
        Usuario usuario = usuarioActual();
        Sede sede = usuario == null ? null : usuario.getSede();
        return sede == null ? null : sede.getId();
    }

    public String sedeNombreActual() {
        Usuario usuario = usuarioActual();
        Sede sede = usuario == null ? null : usuario.getSede();
        return sede == null ? null : sede.getNombre();
    }

    public void validarMismaSede(Long sedeIdRegistro) {
        if (esSuperUsuario()) {
            return;
        }
        Long sedeId = sedeIdActual();
        if (sedeId == null || sedeIdRegistro == null || !sedeId.equals(sedeIdRegistro)) {
            throw new RuntimeException("No tiene permisos para modificar registros de otra sede.");
        }
    }

    public String usernameActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    private String normalizarRol(String rol) {
        String limpio = rol == null ? "" : rol.trim().toUpperCase(Locale.ROOT);
        if (limpio.startsWith("ROLE_")) {
            limpio = limpio.substring("ROLE_".length());
        }
        if ("SUPERUSUARIO".equals(limpio) || "SUPERUSER".equals(limpio) || "SUPERADMIN".equals(limpio)) {
            return "SUPER_ADMIN";
        }
        if ("USUARIO".equals(limpio)) {
            return "USER";
        }
        return limpio;
    }
}
