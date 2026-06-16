package com.inventario.security;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.http.HttpMethod;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;


    // =====================================
    // PASSWORD ENCODER
    // =====================================

    @Bean
    public PasswordEncoder passwordEncoder(){

        BCryptPasswordEncoder bcrypt =
                new BCryptPasswordEncoder();

        return new PasswordEncoder() {

            @Override
            public String encode(CharSequence rawPassword) {

                return bcrypt.encode(rawPassword);

            }

            @Override
            public boolean matches(
                    CharSequence rawPassword,
                    String encodedPassword) {

                if (rawPassword == null ||
                        encodedPassword == null) {

                    return false;

                }

                String passwordGuardado =
                        encodedPassword.trim();

                if (passwordGuardado.startsWith("{bcrypt}")) {

                    passwordGuardado =
                            passwordGuardado.substring(
                                    "{bcrypt}".length());

                }

                if (passwordGuardado.startsWith("{noop}")) {

                    passwordGuardado =
                            passwordGuardado.substring(
                                    "{noop}".length());

                }

                if (passwordGuardado.startsWith("$2a$") ||
                        passwordGuardado.startsWith("$2b$") ||
                        passwordGuardado.startsWith("$2y$")) {

                    try {

                        return bcrypt.matches(
                                rawPassword,
                                passwordGuardado);

                    } catch (IllegalArgumentException ex) {

                        return false;

                    }

                }

                String passwordIngresado =
                        rawPassword.toString();

                return passwordGuardado.equals(passwordIngresado) ||
                        passwordGuardado.equals(
                                passwordIngresado.trim());

            }

        };

    }


    // =====================================
    // SECURITY FILTER
    // =====================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

            // CSRF
            .csrf(csrf -> csrf.disable())

            // RUTAS
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(

                        "/login",

                        "/css/**",

                        "/js/**",

                        "/img/**"

                ).permitAll()

                .requestMatchers(
                        "/usuarios/**",
                        "/sedes/**"
                )
                .hasRole("SUPER_ADMIN")

                .requestMatchers(
                        HttpMethod.PUT,
                        "/proveedores/**",
                        "/tipos/**"
                )
                .hasRole("SUPER_ADMIN")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/proveedores/**",
                        "/tipos/**"
                )
                .hasRole("SUPER_ADMIN")

                .requestMatchers(
                        "/configuracion/**",
                        "/proveedores/**",
                        "/tipos/**"
                )
                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                .anyRequest()

                .authenticated()

            )

            // LOGIN
            .formLogin(form -> form

                .loginPage("/login")

                .loginProcessingUrl("/login")

                .usernameParameter("username")

                .passwordParameter("password")

                .defaultSuccessUrl("/", true)

                .failureUrl("/login?error")

                .permitAll()

            )

            // LOGOUT
            .logout(logout -> logout

                .logoutUrl("/logout")

                .logoutSuccessUrl("/login?logout")

                .invalidateHttpSession(true)

                .deleteCookies("JSESSIONID")

                .permitAll()

            )

            // USER DETAILS SERVICE
            .userDetailsService(userDetailsService);

        return http.build();

    }

}
