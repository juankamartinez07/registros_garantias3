package com.inventario.security;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.inventario.model.Usuario;
import com.inventario.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private static final Set<String> ROLES_VALIDOS =
            Set.of("SUPER_ADMIN", "ADMIN", "USER");

    private static final Logger logger =
            LoggerFactory.getLogger(
                    CustomUserDetailsService.class);

    @Autowired
    private UsuarioRepository repository;

    @Override
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        String usernameLimpio =
                username == null
                        ? ""
                        : username.trim();

        Usuario usuario = repository
                .findByUsernameForLogin(usernameLimpio)
                .orElseThrow(() -> {

                    logger.warn(
                            "Login: usuario no encontrado '{}'",
                            usernameLimpio);

                    return new UsernameNotFoundException(
                            "Usuario no encontrado");

                });

        String rol =
                usuario.getRol() == null
                        ? ""
                        : usuario.getRol()
                                .trim()
                                .toUpperCase(Locale.ROOT);

        if (rol.startsWith("ROLE_")) {

            rol = rol.substring("ROLE_".length());

        }

        if (!ROLES_VALIDOS.contains(rol)) {

            logger.warn(
                    "Login: usuario '{}' tiene rol no reconocido '{}'",
                    usuario.getUsername(),
                    rol);

        }

        String autoridad = "ROLE_" + rol;
        boolean activo = true;

        logger.info(
                "Login: usuario encontrado='{}', activo={}, rol='{}', authority='{}'",
                usuario.getUsername(),
                activo,
                rol,
                autoridad);

        return User
                .withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(List.of(
                        new SimpleGrantedAuthority(
                                autoridad)))
                .disabled(!activo)
                .build();

    }

}

