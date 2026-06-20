package com.inventario.security;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.http.HttpMethod;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final boolean LOGIN_TEMPORALMENTE_DESACTIVADO =
            true;

    @Autowired
    private CustomUserDetailsService userDetailsService;


    // =====================================
    // PASSWORD ENCODER
    // =====================================

    @Bean
    public PasswordEncoder passwordEncoder(){

        return new CompatibleBcryptPasswordEncoder();

    }


    // =====================================
    // SECURITY FILTER
    // =====================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        if (LOGIN_TEMPORALMENTE_DESACTIVADO) {

            http

                // TEMPORAL: login desactivado para probar el sistema.
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                    .anyRequest()

                    .permitAll()

                );

            return http.build();

        }

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

