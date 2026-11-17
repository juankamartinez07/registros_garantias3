package com.inventario.service;

import com.inventario.config.TiempoColombiaConfig;
import com.inventario.dto.ActivacionSerialRespuesta;
import com.inventario.model.*;
import com.inventario.repository.EquipoRepository;
import com.inventario.repository.SerialInternoHistorialRepository;
import com.inventario.repository.SerialInternoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class ActivacionSerialInternoService {
    private final EquipoRepository equipos;
    private final SerialInternoRepository seriales;
    private final SerialInternoHistorialRepository historiales;
    private final UsuarioContextService contexto;

    public ActivacionSerialInternoService(EquipoRepository equipos, SerialInternoRepository seriales,
            SerialInternoHistorialRepository historiales, UsuarioContextService contexto) {
        this.equipos = equipos;
        this.seriales = seriales;
        this.historiales = historiales;
        this.contexto = contexto;
    }

    @Transactional
    public ActivacionSerialRespuesta activar(String valor) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_TECNICO"))) {
            throw new AccessDeniedException("No tiene permisos para activar este serial.");
        }
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Ingrese un serial.");
        Equipo equipo = equipos.findBySerialIgnoreCase(valor.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException("El serial ingresado no existe en el sistema."));
        SerialInterno interno = seriales.bloquearPorEquipo(equipo.getId_equipo())
                .orElseThrow(() -> new IllegalArgumentException("Este serial no corresponde a un serial interno generado por SMD."));
        Long sedeRegistro = interno.getLote().getSede().getId();
        if (!contexto.esSuperUsuario() && (contexto.sedeIdActual() == null
                || !contexto.sedeIdActual().equals(sedeRegistro))) {
            throw new AccessDeniedException("No tiene permisos para activar este serial.");
        }
        if (interno.getEstado() == EstadoSerialInterno.ACTIVO)
            throw new IllegalStateException("Este serial ya se encuentra activo.");
        if (interno.getEstado() == EstadoSerialInterno.ANULADO)
            throw new IllegalStateException("Este serial fue anulado y no puede activarse.");
        if (interno.getEstado() != EstadoSerialInterno.PENDIENTE_ACTIVACION)
            throw new IllegalStateException("El estado del serial no permite su activacion.");

        LocalDateTime ahora = TiempoColombiaConfig.ahora();
        interno.setEstado(EstadoSerialInterno.ACTIVO);
        interno.setFechaActivacion(ahora);
        interno.setUsuarioActivacion(auth.getName());
        seriales.save(interno);
        SerialInternoHistorial historial = new SerialInternoHistorial();
        historial.setSerialInterno(interno);
        historial.setTipoEvento(EventoSerialInterno.SERIAL_ACTIVADO);
        historial.setUsuario(auth.getName());
        historial.setFechaHora(ahora);
        historial.setObservacion("Serial interno activado mediante el proceso de identificacion fisica del equipo.");
        historial.setValoresAnteriores(EstadoSerialInterno.PENDIENTE_ACTIVACION.name());
        historial.setValoresNuevos(EstadoSerialInterno.ACTIVO.name());
        historiales.save(historial);
        return new ActivacionSerialRespuesta(interno.getId(), equipo.getSerial(),
                EstadoSerialInterno.PENDIENTE_ACTIVACION, EstadoSerialInterno.ACTIVO, ahora,
                "Serial activado correctamente.");
    }
}
