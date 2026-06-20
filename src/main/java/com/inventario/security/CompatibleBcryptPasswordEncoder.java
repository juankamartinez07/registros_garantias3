package com.inventario.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CompatibleBcryptPasswordEncoder
        implements PasswordEncoder {

    private static final String PREFIX = "{bcrypt}";

    private final BCryptPasswordEncoder delegate =
            new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {

        return PREFIX + delegate.encode(rawPassword);

    }

    @Override
    public boolean matches(
            CharSequence rawPassword,
            String encodedPassword) {

        if (rawPassword == null || encodedPassword == null) {

            return false;

        }

        String hash = encodedPassword.trim();

        if (hash.startsWith(PREFIX)) {

            hash = hash.substring(PREFIX.length());

        }

        if (!hash.startsWith("$2a$") &&
                !hash.startsWith("$2b$") &&
                !hash.startsWith("$2y$")) {

            return false;

        }

        try {

            return delegate.matches(rawPassword, hash);

        } catch (IllegalArgumentException ex) {

            return false;

        }

    }

}

