package com.inventario.repository;

import java.util.Optional;
import java.util.Collection;

import com.inventario.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    long countByRolInAndActivoTrue(Collection<String> roles);

}

