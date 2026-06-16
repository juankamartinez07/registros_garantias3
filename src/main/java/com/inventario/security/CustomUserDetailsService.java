package com.inventario.security;
import java.util.List;

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

        Usuario usuario =
                repository
                .findByUsernameForLogin(usernameLimpio)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        logger.info(
                "Login: usuario encontrado '{}', rol '{}'",
                usuario.getUsername(),
                usuario.getRol());

        String rol =
                usuario.getRol() == null
                        ? ""
                        : usuario.getRol().trim();

        String autoridad =
                rol.startsWith("ROLE_")
                        ? rol
                        : "ROLE_" + rol;

        return new User(

                usuario.getUsername(),

                usuario.getPassword(),

                List.of(

    new SimpleGrantedAuthority(
            autoridad
    )

)

        );

    }

}
