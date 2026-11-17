package com.inventario.service;

import com.inventario.dto.EdicionSerialInternoRequest;
import com.inventario.model.*;
import com.inventario.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EdicionSerialInternoTest {
    @Mock SerialInternoRepository seriales;
    @Mock SerialInternoHistorialRepository historiales;
    @Mock UsuarioContextService contexto;
    @Mock ProductoRepository productos;
    @Mock TipoProductoRepository tipos;
    @Mock ProveedorRepository proveedores;

    @AfterEach void limpiar() { SecurityContextHolder.clearContext(); }

    @Test void editaSeleccionSinAlterarSerialNiCamposNoSeleccionados() {
        autenticar("ADMIN");
        SerialInterno primero = serial("ABC"); SerialInterno segundo = serial("XYZ");
        when(seriales.bloquearPorId(1L)).thenReturn(Optional.of(primero));
        when(seriales.bloquearPorId(2L)).thenReturn(Optional.of(segundo));
        when(contexto.sedeIdActual()).thenReturn(10L);
        assertEquals(2, servicio().editar(new EdicionSerialInternoRequest(List.of(1L, 2L), null, null, null, "nueva", null)));
        assertEquals("ABC", primero.getEquipo().getSerial());
        assertEquals("XYZ", segundo.getEquipo().getSerial());
        assertEquals("NUEVA", primero.getEquipo().getFactura());
        assertEquals("2026-01-01", segundo.getEquipo().getFecha());
        verify(historiales, times(2)).save(any(SerialInternoHistorial.class));
    }

    @Test void impideModificacionesDeOtraSede() {
        autenticar("ADMIN"); SerialInterno serial = serial("ABC");
        when(seriales.bloquearPorId(1L)).thenReturn(Optional.of(serial));
        when(contexto.sedeIdActual()).thenReturn(99L);
        assertThrows(AccessDeniedException.class, () -> servicio().editar(new EdicionSerialInternoRequest(List.of(1L), null, null, null, "nueva", null)));
        assertEquals("ANTERIOR", serial.getEquipo().getFactura());
        verifyNoInteractions(historiales);
    }

    @Test void rechazaTecnicoYSerialInexistente() {
        autenticar("TECNICO");
        assertThrows(AccessDeniedException.class, () -> servicio().editar(new EdicionSerialInternoRequest(List.of(1L), null, null, null, "nueva", null)));
        autenticar("ADMIN");
        when(seriales.bloquearPorId(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> servicio().editar(new EdicionSerialInternoRequest(List.of(1L), null, null, null, "nueva", null)));
    }

    private SerialInterno serial(String valor) {
        Sede sede = new Sede(); sede.setId(10L); LoteSerialInterno lote = new LoteSerialInterno(); lote.setSede(sede);
        Equipo equipo = new Equipo(); equipo.setSerial(valor); equipo.setFactura("ANTERIOR"); equipo.setFecha("2026-01-01");
        SerialInterno serial = new SerialInterno(); serial.setEquipo(equipo); serial.setLote(lote); return serial;
    }
    private void autenticar(String rol) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "", List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
    }
    private AdministracionSerialInternoService servicio() {
        return new AdministracionSerialInternoService(seriales, historiales, contexto, productos, tipos, proveedores);
    }
}
