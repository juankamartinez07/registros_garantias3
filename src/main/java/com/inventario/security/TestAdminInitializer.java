package com.inventario.security;

import com.inventario.model.Usuario;
import com.inventario.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.test-admin.enabled",
        havingValue = "true")
public class TestAdminInitializer
        implements ApplicationRunner {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    TestAdminInitializer.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String testPassword;

    public TestAdminInitializer(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${app.test-admin.password:}")
            String testPassword) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.testPassword = testPassword;

    }

    @Override
    public void run(ApplicationArguments args) {

        if (testPassword == null || testPassword.isBlank()) {

            throw new IllegalStateException(
                    "TEST_ADMIN_PASSWORD es obligatorio cuando " +
                    "TEST_ADMIN_ENABLED=true");

        }

        Usuario usuario = repository
                .findByUsernameForLogin("testadmin")
                .orElseGet(Usuario::new);

        boolean nuevo = usuario.getId() == null;

        usuario.setUsername("testadmin");
        usuario.setPassword(
                passwordEncoder.encode(testPassword));
        usuario.setRol("SUPER_ADMIN");

        repository.save(usuario);

        logger.info(
                "Usuario de prueba '{}' {} con rol '{}'",
                usuario.getUsername(),
                nuevo ? "creado" : "actualizado",
                usuario.getRol());

    }

}

