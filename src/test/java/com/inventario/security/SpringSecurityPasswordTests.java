package com.inventario.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SpringSecurityPasswordTests {

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    @Test
    void aceptaHashDosASinPrefijo() {

        String hash = encoder.encode("1111");

        assertTrue(encoder.matches("1111", hash));

    }

    @Test
    void aceptaHashDosYDespuesDeQuitarPrefijo() {

        String hashDosA = encoder.encode("1111");
        String hashDosY = hashDosA.replaceFirst(
                "\\$2a\\$",
                "\\$2y\\$");

        String almacenado = "{bcrypt}" + hashDosY;
        String normalizado =
                CustomUserDetailsService
                        .normalizarPassword(almacenado);

        assertTrue(encoder.matches("1111", normalizado));

    }

}

