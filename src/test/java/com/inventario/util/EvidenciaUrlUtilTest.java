package com.inventario.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvidenciaUrlUtilTest {

    @Test
    void aceptaEnlacesHttpHttpsYConvierteVacioEnNulo() {
        assertEquals("https://ejemplo.com/evidencia", EvidenciaUrlUtil.normalizarYValidar(" https://ejemplo.com/evidencia "));
        assertEquals("http://ejemplo.com/evidencia", EvidenciaUrlUtil.normalizarYValidar("http://ejemplo.com/evidencia"));
        assertNull(EvidenciaUrlUtil.normalizarYValidar("   "));
    }

    @Test
    void rechazaEsquemasNoSeguros() {
        assertThrows(IllegalArgumentException.class,
                () -> EvidenciaUrlUtil.normalizarYValidar("javascript:alert(1)"));
        assertThrows(IllegalArgumentException.class,
                () -> EvidenciaUrlUtil.normalizarYValidar("file:///C:/evidencia.jpg"));
    }

    @Test
    void extraeArchivoDriveYNoGeneraMiniaturaParaCarpeta() {
        String archivo = "https://drive.google.com/file/d/ARCHIVO_123/view?usp=sharing";
        String carpeta = "https://drive.google.com/drive/folders/CARPETA_123";

        assertEquals("ARCHIVO_123", EvidenciaUrlUtil.extraerIdArchivoDrive(archivo).orElseThrow());
        assertTrue(EvidenciaUrlUtil.construirUrlMiniatura(archivo).orElseThrow().contains("ARCHIVO_123"));
        assertTrue(EvidenciaUrlUtil.esCarpetaDrive(carpeta));
        assertFalse(EvidenciaUrlUtil.extraerIdArchivoDrive(carpeta).isPresent());
        assertFalse(EvidenciaUrlUtil.construirUrlMiniatura(carpeta).isPresent());
    }
}
