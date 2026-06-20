package com.inventario.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class CompatibleBcryptPasswordEncoderTests {

    private final CompatibleBcryptPasswordEncoder encoder =
            new CompatibleBcryptPasswordEncoder();

    @Test
    void aceptaBcryptConYSinPrefijo() {

        String hashSinPrefijo =
                new BCryptPasswordEncoder().encode("1111");

        assertTrue(encoder.matches("1111", hashSinPrefijo));
        assertTrue(encoder.matches(
                "1111",
                "{bcrypt}" + hashSinPrefijo));

    }

    @Test
    void aceptaBcryptDosYConPrefijo() {

        String hashDosA =
                new BCryptPasswordEncoder().encode("1111");
        String hashDosY =
                hashDosA.replaceFirst("\\$2a\\$", "\\$2y\\$");

        assertTrue(encoder.matches(
                "1111",
                "{bcrypt}" + hashDosY));

    }

    @Test
    void generaHashConPrefijoYRechazaTextoPlano() {

        String hash = encoder.encode("1111");

        assertTrue(hash.startsWith("{bcrypt}$2"));
        assertTrue(encoder.matches("1111", hash));
        assertFalse(encoder.matches("1111", "1111"));

    }

}

