package com.inventario.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TiempoColombiaConfig {

    public static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");
    private static final Clock RELOJ_COLOMBIA = Clock.system(ZONA_COLOMBIA);

    @Bean
    public Clock relojColombia() {
        return RELOJ_COLOMBIA;
    }

    @PostConstruct
    void configurarZonaPredeterminadaJvm() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZONA_COLOMBIA));
    }

    public static LocalDate hoy() {
        return LocalDate.now(RELOJ_COLOMBIA);
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now(RELOJ_COLOMBIA);
    }
}
