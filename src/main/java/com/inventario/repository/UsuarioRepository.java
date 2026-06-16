package com.inventario.repository;

import com.inventario.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(
            String username);

    @Query("""
            select u
            from Usuario u
            where lower(trim(u.username)) =
                  lower(trim(:username))
            """)
    Optional<Usuario> findByUsernameForLogin(
            @Param("username") String username);

}
