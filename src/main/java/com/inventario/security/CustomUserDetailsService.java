package com.inventario.security;

import java.util.Locale;
import java.util.Set;

import com.inventario.model.Usuario;
import com.inventario.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private static final Set<String> ROLES_VALIDOS =
            Set.of("SUPER_ADMIN", "ADMIN", "TECNICO", "USER");

    private static final String BCRYPT_PREFIX = "{bcrypt}";

    private final UsuarioRepository repository;

    public CustomUserDetailsService(
            UsuarioRepository repository) {

        this.repository = repository;

    }

    @Override
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        String usernameLimpio =
                username == null ? "" : username.trim();

        Usuario usuario = repository
                .findByUsername(usernameLimpio)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado"));

        String rol = normalizarRol(usuario.getRol());

        if (!ROLES_VALIDOS.contains(rol)) {

            throw new UsernameNotFoundException(
                    "Usuario con rol inválido");

        }

        String authority = "ROLE_" + rol;

        return User
                .withUsername(usuario.getUsername())
                .password(normalizarPassword(usuario.getPassword()))
                .authorities(authority)
                .disabled(Boolean.FALSE.equals(usuario.getActivo()))
                .build();

    }

    static String normalizarPassword(String password) {

        if (password == null) {

            return "";

        }

        String passwordLimpio = password.trim();

        if (passwordLimpio.startsWith(BCRYPT_PREFIX)) {

            return passwordLimpio.substring(
                    BCRYPT_PREFIX.length());

        }

        return passwordLimpio;

    }

    private static String normalizarRol(String rol) {

        String rolLimpio = rol == null
                ? ""
                : rol.trim().toUpperCase(Locale.ROOT);

        if (rolLimpio.startsWith("ROLE_")) {

            rolLimpio = rolLimpio.substring("ROLE_".length());

        }

        if ("USUARIO".equals(rolLimpio)) {

            return "USER";

        }

        if ("SUPERUSUARIO".equals(rolLimpio) || "SUPERUSER".equals(rolLimpio) || "SUPERADMIN".equals(rolLimpio)) {

            return "SUPER_ADMIN";

        }

        return rolLimpio;

    }

}
