package com.inventario.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneradorSerialInternoTest {

    @Test
    void generaIdentificadoresConElFormatoPermitido() {
        GeneradorSerialInterno generador = new GeneradorSerialInterno();
        for (int i = 0; i < 100; i++) {
            assertTrue(generador.generar().matches("[2-9A-HJ-NP-Z]{15}"));
        }
    }
}
