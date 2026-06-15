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

        return new BCryptPasswordEncoder();

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

                .defaultSuccessUrl("/", true)

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
