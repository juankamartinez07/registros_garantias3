package com.inventario.service;

import com.inventario.dto.ServicioTecnicoActualizacionDTO;
import com.inventario.dto.ServicioTecnicoDTO;
import com.inventario.model.ServicioTecnico;
import com.inventario.model.ServicioTecnicoHistorial;
import com.inventario.repository.ServicioTecnicoHistorialRepository;
import com.inventario.repository.ServicioTecnicoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioTecnicoServiceTest {

    @Mock
    private ServicioTecnicoRepository servicioTecnicoRepository;

    @Mock
    private ServicioTecnicoHistorialRepository historialRepository;

    @Mock
    private UsuarioContextService usuarioContextService;

    @AfterEach
    void limpiarContextoSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void actualizarProcesoRegistraHistorialConCambioDeEstado() {
        autenticarTecnico();
        ServicioTecnico servicio = servicio("Armenia", "Recibido");
        ServicioTecnicoActualizacionDTO solicitud = new ServicioTecnicoActualizacionDTO();
        solicitud.setEstadoServicio("En revision");
        solicitud.setObservacion("Se inicia diagnostico.");

        when(servicioTecnicoRepository.findById(15L)).thenReturn(Optional.of(servicio));
        when(servicioTecnicoRepository.save(servicio)).thenReturn(servicio);
        when(usuarioContextService.esSuperUsuario()).thenReturn(false);
        when(usuarioContextService.sedeNombreActual()).thenReturn("Armenia");
        when(usuarioContextService.usernameActual()).thenReturn("tecnico.armenia");

        servicio().actualizarProceso(15L, solicitud);

        assertEquals("En revision", servicio.getEstadoServicio());
        ArgumentCaptor<ServicioTecnicoHistorial> captor = ArgumentCaptor.forClass(ServicioTecnicoHistorial.class);
        verify(historialRepository).save(captor.capture());
        assertEquals("Recibido", captor.getValue().getEstadoAnterior());
        assertEquals("En revision", captor.getValue().getEstadoNuevo());
        assertEquals("ACTUALIZACION", captor.getValue().getTipoEvento());
        assertEquals("tecnico.armenia", captor.getValue().getUsuario());
        assertEquals("SE INICIA DIAGNOSTICO.", captor.getValue().getObservacion());
    }

    @Test
    void crearRegistraLaPrimeraEntradaDeHistorial() {
        autenticarTecnico();
        ServicioTecnicoDTO solicitud = new ServicioTecnicoDTO();
        solicitud.setCliente("Cliente prueba");
        solicitud.setTelefono("3000000000");
        solicitud.setSerial("SERIAL-001");
        solicitud.setProductoReferencia("Camara prueba");
        solicitud.setMotivoRevision("Revision inicial");
        solicitud.setEstadoFisico("Sin novedades");
        solicitud.setEstadoServicio("Recibido");

        when(servicioTecnicoRepository.maxTicketCorto()).thenReturn(null);
        when(servicioTecnicoRepository.existsByTicket("00001")).thenReturn(false);
        when(servicioTecnicoRepository.save(any(ServicioTecnico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioContextService.esSuperUsuario()).thenReturn(false);
        when(usuarioContextService.sedeNombreActual()).thenReturn("Armenia");
        when(usuarioContextService.usernameActual()).thenReturn("tecnico.armenia");

        ServicioTecnico creado = servicio().crear(solicitud);

        assertEquals("00001", creado.getTicket());
        assertEquals("Armenia", creado.getSede());
        ArgumentCaptor<ServicioTecnicoHistorial> captor = ArgumentCaptor.forClass(ServicioTecnicoHistorial.class);
        verify(historialRepository).save(captor.capture());
        assertEquals("CREACION", captor.getValue().getTipoEvento());
        assertEquals("Recibido", captor.getValue().getEstadoNuevo());
        assertEquals("Ingreso de servicio tecnico creado.", captor.getValue().getObservacion());
    }

    @Test
    void actualizarProcesoRechazaServicioDeOtraSede() {
        autenticarTecnico();
        when(servicioTecnicoRepository.findById(15L)).thenReturn(Optional.of(servicio("Pereira", "Recibido")));
        when(usuarioContextService.esSuperUsuario()).thenReturn(false);
        when(usuarioContextService.sedeNombreActual()).thenReturn("Armenia");

        ServicioTecnicoActualizacionDTO solicitud = new ServicioTecnicoActualizacionDTO();
        solicitud.setEstadoServicio("En revision");

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> servicio().actualizarProceso(15L, solicitud));

        assertEquals("No tiene permisos para modificar registros de otra sede.", error.getMessage());
        verifyNoInteractions(historialRepository);
    }

    private ServicioTecnicoService servicio() {
        return new ServicioTecnicoService(servicioTecnicoRepository, historialRepository, usuarioContextService);
    }

    private ServicioTecnico servicio(String sede, String estado) {
        ServicioTecnico servicio = new ServicioTecnico();
        servicio.setSede(sede);
        servicio.setEstadoServicio(estado);
        return servicio;
    }

    private void autenticarTecnico() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "tecnico.armenia",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_TECNICO"))));
    }
}
