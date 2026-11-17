package com.inventario.service;

import com.inventario.model.*;
import com.inventario.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivacionSerialInternoServiceTest {
    @Mock EquipoRepository equipos;
    @Mock SerialInternoRepository seriales;
    @Mock SerialInternoHistorialRepository historiales;
    @Mock UsuarioContextService contexto;
    @AfterEach void limpiar() { SecurityContextHolder.clearContext(); }

    @Test void activaYRegistraHistorialSinCambiarEquipo() {
        autenticar("TECNICO");
        Equipo equipo = equipo(); SerialInterno interno = interno(EstadoSerialInterno.PENDIENTE_ACTIVACION);
        when(equipos.findBySerialIgnoreCase("ABC")).thenReturn(Optional.of(equipo));
        when(seriales.bloquearPorEquipo(1L)).thenReturn(Optional.of(interno));
        when(contexto.sedeIdActual()).thenReturn(2L);
        var resultado = servicio().activar(" abc ");
        assertEquals(EstadoSerialInterno.ACTIVO, resultado.estadoActual());
        assertEquals("ACTIVO ORIGINAL", equipo.getEstado());
        assertNotNull(interno.getFechaActivacion());
        assertEquals("tecnico", interno.getUsuarioActivacion());
        ArgumentCaptor<SerialInternoHistorial> captor = ArgumentCaptor.forClass(SerialInternoHistorial.class);
        verify(historiales).save(captor.capture());
        assertEquals(EventoSerialInterno.SERIAL_ACTIVADO, captor.getValue().getTipoEvento());
    }

    @Test void rechazaActivoYAnulado() {
        autenticar("ADMIN"); Equipo equipo = equipo();
        SerialInterno interno = interno(EstadoSerialInterno.ACTIVO);
        when(equipos.findBySerialIgnoreCase("ABC")).thenReturn(Optional.of(equipo));
        when(seriales.bloquearPorEquipo(1L)).thenReturn(Optional.of(interno));
        when(contexto.sedeIdActual()).thenReturn(2L);
        assertThrows(IllegalStateException.class, () -> servicio().activar("ABC"));
        interno.setEstado(EstadoSerialInterno.ANULADO);
        assertThrows(IllegalStateException.class, () -> servicio().activar("ABC"));
        verifyNoInteractions(historiales);
    }

    @Test void rechazaUsuarioSinRol() {
        autenticar("USER");
        assertThrows(AccessDeniedException.class, () -> servicio().activar("ABC"));
        verifyNoInteractions(equipos);
    }

    @Test void rechazaOtraSede() {
        autenticar("ADMIN");
        when(equipos.findBySerialIgnoreCase("ABC")).thenReturn(Optional.of(equipo()));
        when(seriales.bloquearPorEquipo(1L)).thenReturn(Optional.of(interno(EstadoSerialInterno.PENDIENTE_ACTIVACION)));
        when(contexto.sedeIdActual()).thenReturn(3L);
        assertThrows(AccessDeniedException.class, () -> servicio().activar("ABC"));
        verifyNoInteractions(historiales);
    }

    private Equipo equipo() { Equipo e = new Equipo(); e.setId_equipo(1L); e.setSerial("ABC"); e.setEstado("ACTIVO ORIGINAL"); return e; }
    private SerialInterno interno(EstadoSerialInterno estado) {
        Sede sede = new Sede(); sede.setId(2L); LoteSerialInterno lote = new LoteSerialInterno(); lote.setSede(sede);
        SerialInterno s = new SerialInterno(); s.setLote(lote); s.setEstado(estado); return s;
    }
    private void autenticar(String rol) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "tecnico", "", List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
    }
    private ActivacionSerialInternoService servicio() { return new ActivacionSerialInternoService(equipos, seriales, historiales, contexto); }
}
