package com.inventario.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class GeneradorSerialInterno {

    private static final String ALFABETO = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int LONGITUD = 15;
    private final SecureRandom aleatorio = new SecureRandom();

    public String generar() {
        StringBuilder serial = new StringBuilder(LONGITUD);
        for (int i = 0; i < LONGITUD; i++) {
            serial.append(ALFABETO.charAt(aleatorio.nextInt(ALFABETO.length())));
        }
        return serial.toString();
    }
}
