package com.inventario.service;

import com.inventario.dto.GenerarLoteSerialesRequest;
import com.inventario.model.*;
import com.inventario.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoteSerialInternoServiceTest {
    @Mock GeneradorSerialInterno generador;
    @Mock EquipoService equipos;
    @Mock EquipoRepository equipoRepository;
    @Mock LoteSerialInternoRepository lotes;
    @Mock SerialInternoRepository internos;
    @Mock SerialInternoHistorialRepository historiales;
    @Mock UsuarioContextService contexto;
    @Mock JdbcTemplate jdbc;

    @AfterEach void limpiar() { SecurityContextHolder.clearContext(); }

    @Test void generaTreintaEquiposConHistorial() {
        autenticar("ADMIN");
        Usuario usuario = new Usuario(); usuario.setUsername("admin");
        Sede sede = new Sede(); sede.setId(1L); sede.setNombre("Armenia"); usuario.setSede(sede);
        when(contexto.usuarioActual()).thenReturn(usuario);
        when(jdbc.queryForObject(anyString(), eq(Integer.class))).thenReturn(1);
        AtomicInteger secuencia = new AtomicInteger();
        when(generador.generar()).thenAnswer(i -> String.format("%015d", secuencia.incrementAndGet()));
        when(equipos.guardarCompleto(any())).thenAnswer(i -> {
            Equipo e = new Equipo(); e.setSerial(((com.inventario.dto.EquipoDTO) i.getArgument(0)).getSerial()); return e;
        });
        var resultado = servicio().generar(solicitud(30));
        assertEquals(30, resultado.seriales().stream().distinct().count());
        ArgumentCaptor<SerialInterno> captor = ArgumentCaptor.forClass(SerialInterno.class);
        verify(internos, times(30)).save(captor.capture());
        assertTrue(captor.getAllValues().stream().allMatch(s -> s.getEquipo() != null && s.getLote() != null
                && s.getEstado() == EstadoSerialInterno.PENDIENTE_ACTIVACION));
        verify(historiales, times(30)).save(any(SerialInternoHistorial.class));
    }

    @Test void rechazaTecnico() {
        autenticar("TECNICO");
        assertThrows(AccessDeniedException.class, () -> servicio().generar(solicitud(1)));
        verifyNoInteractions(lotes);
    }

    @Test void rechazaCantidadInvalida() {
        autenticar("ADMIN");
        assertThrows(IllegalArgumentException.class, () -> servicio().generar(solicitud(0)));
        verifyNoInteractions(lotes);
    }

    private GenerarLoteSerialesRequest solicitud(int cantidad) {
        return new GenerarLoteSerialesRequest("CAMARA", "MARCA", "PROVEEDOR", "FACTURA", LocalDate.now(), cantidad, null);
    }
    private void autenticar(String rol) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "usuario", "", List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
    }
    private LoteSerialInternoService servicio() {
        return new LoteSerialInternoService(generador, equipos, equipoRepository, lotes, internos, historiales, contexto, jdbc);
    }
}
