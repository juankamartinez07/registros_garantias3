package com.inventario.security;

import java.util.Locale;
import java.util.Set;

import com.inventario.model.Usuario;
import com.inventario.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    CustomUserDetailsService.class);

    private static final Set<String> ROLES_VALIDOS =
            Set.of("SUPER_ADMIN", "ADMIN", "USER");

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
                .orElseThrow(() -> {

                    logger.warn(
                            "Login: usuario no encontrado '{}'",
                            usernameLimpio);

                    return new UsernameNotFoundException(
                            "Usuario no encontrado");

                });

        String rol = normalizarRol(usuario.getRol());

        if (!ROLES_VALIDOS.contains(rol)) {

            logger.warn(
                    "Login: usuario '{}' tiene rol invÃ¡lido '{}'",
                    usuario.getUsername(),
                    rol);

            throw new UsernameNotFoundException(
                    "Usuario con rol invÃ¡lido");

        }

        String authority = "ROLE_" + rol;

        logger.info(
                "Login: usuario encontrado='{}', activo=true, rol='{}', authority='{}'",
                usuario.getUsername(),
                rol,
                authority);

        return User
                .withUsername(usuario.getUsername())
                .password(normalizarPassword(usuario.getPassword()))
                .authorities(authority)
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

            return rolLimpio.substring("ROLE_".length());

        }

        return rolLimpio;

    }

}

