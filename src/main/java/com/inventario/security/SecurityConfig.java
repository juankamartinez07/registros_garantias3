package com.inventario.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;

    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authenticationProvider(authenticationProvider)

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/login",
                        "/css/**",
                        "/js/**",
                        "/img/**")
                .permitAll()

                .requestMatchers(
                        "/usuarios/**",
                        "/sedes/**")
                .hasAnyRole("SUPER_ADMIN", "SUPERUSER")

                .requestMatchers(
                        HttpMethod.PUT,
                        "/proveedores/**",
                        "/tipos/**")
                .hasAnyRole("SUPER_ADMIN", "SUPERUSER")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/garantias/**")
                .hasAnyRole("SUPER_ADMIN", "SUPERUSER")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/equipos/**")
                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/proveedores/**",
                        "/tipos/**")
                .hasAnyRole("SUPER_ADMIN", "SUPERUSER")

                .requestMatchers(
                        "/garantias/**")
                .hasAnyRole("SUPER_ADMIN", "SUPERUSER", "ADMIN")

                .requestMatchers(
                        "/configuracion/**",
                        "/proveedores/**",
                        "/tipos/**")
                .hasAnyRole("SUPER_ADMIN", "ADMIN")

                .anyRequest()
                .authenticated())

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll())

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll());

        return http.build();

    }

}
