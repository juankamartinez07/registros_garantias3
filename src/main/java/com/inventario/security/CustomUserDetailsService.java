package com.inventario.security;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.inventario.model.Usuario;
import com.inventario.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.*;

import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    @Autowired
    private UsuarioRepository repository;

    @Override
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        Usuario usuario =
                repository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        return new User(

                usuario.getUsername(),

                usuario.getPassword(),

                List.of(

    new SimpleGrantedAuthority(
            "ROLE_" + usuario.getRol()
    )

)

        );

    }

}