package com.inventario.service;

import com.inventario.config.TiempoColombiaConfig;
import com.inventario.dto.AnulacionSerialRequest;
import com.inventario.dto.ProrrogaSerialRequest;
import com.inventario.model.*;
import com.inventario.repository.SerialInternoHistorialRepository;
import com.inventario.repository.SerialInternoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

@Service
public class AdministracionSerialInternoService {
    private static final Set<String> MOTIVOS = Set.of("ETIQUETA_DANADA", "ERROR_IMPRESION", "ETIQUETA_EXTRAVIADA", "GENERADO_POR_ERROR", "OTRO");
    private final SerialInternoRepository seriales;
    private final SerialInternoHistorialRepository historiales;
    private final UsuarioContextService contexto;

    public AdministracionSerialInternoService(SerialInternoRepository seriales,
            SerialInternoHistorialRepository historiales, UsuarioContextService contexto) {
        this.seriales = seriales; this.historiales = historiales; this.contexto = contexto;
    }

    @Transactional
    public int anular(AnulacionSerialRequest solicitud) {
        String usuario = administrador();
        if (solicitud == null || solicitud.ids() == null || solicitud.ids().isEmpty()
                || solicitud.ids().stream().anyMatch(id -> id == null || id <= 0)
                || solicitud.motivo() == null || !MOTIVOS.contains(solicitud.motivo())
                || solicitud.observacion() == null || solicitud.observacion().isBlank())
            throw new IllegalArgumentException("Seleccione seriales, motivo valido y observacion obligatoria.");
        List<Long> ids = solicitud.ids().stream().distinct().sorted().toList();
        List<SerialInterno> seleccion = ids.stream().map(id -> seriales.bloquearPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Serial interno no encontrado: " + id))).toList();
        seleccion.forEach(s -> { validarSede(s); validarPendiente(s); });
        for (SerialInterno serial : seleccion) {
            serial.setEstado(EstadoSerialInterno.ANULADO);
            serial.setFechaAnulacion(TiempoColombiaConfig.ahora());
            serial.setUsuarioAnulacion(usuario);
            serial.setMotivoAnulacion(solicitud.motivo());
            serial.setObservacionAnulacion(solicitud.observacion().trim());
            registrar(serial, EventoSerialInterno.SERIAL_ANULADO, usuario, solicitud.observacion().trim(),
                    EstadoSerialInterno.PENDIENTE_ACTIVACION.name(), EstadoSerialInterno.ANULADO.name());
        }
        return seleccion.size();
    }

    @Transactional
    public int prorrogar(Long loteId, ProrrogaSerialRequest solicitud) {
        String usuario = administrador();
        if (solicitud == null || solicitud.fechaLimite() == null || solicitud.motivo() == null || solicitud.motivo().isBlank())
            throw new IllegalArgumentException("Ingrese fecha limite y motivo de prorroga.");
        List<SerialInterno> pendientes = seriales.bloquearPendientesLote(loteId);
        if (pendientes.isEmpty()) throw new IllegalArgumentException("El lote no tiene seriales pendientes.");
        for (SerialInterno serial : pendientes) {
            validarSede(serial);
            if (!solicitud.fechaLimite().isAfter(serial.getFechaLimite()))
                throw new IllegalArgumentException("La nueva fecha debe ser posterior a todos los plazos vigentes.");
        }
        for (SerialInterno serial : pendientes) {
            String anterior = serial.getFechaLimite().toString();
            serial.setFechaLimite(solicitud.fechaLimite());
            registrar(serial, EventoSerialInterno.PLAZO_PRORROGADO, usuario, solicitud.motivo().trim(), anterior, solicitud.fechaLimite().toString());
        }
        return pendientes.size();
    }

    private String administrador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN")))
            throw new AccessDeniedException("No tiene permisos para administrar seriales internos.");
        return auth.getName();
    }
    private void validarSede(SerialInterno serial) {
        if (!contexto.esSuperUsuario() && (contexto.sedeIdActual() == null
                || !contexto.sedeIdActual().equals(serial.getLote().getSede().getId())))
            throw new AccessDeniedException("No tiene permisos para administrar seriales de otra sede.");
    }
    private void validarPendiente(SerialInterno serial) {
        if (serial.getEstado() != EstadoSerialInterno.PENDIENTE_ACTIVACION)
            throw new IllegalStateException("Solo se pueden anular seriales pendientes.");
    }
    private void registrar(SerialInterno serial, EventoSerialInterno tipo, String usuario, String observacion, String anterior, String nuevo) {
        SerialInternoHistorial historial = new SerialInternoHistorial();
        historial.setSerialInterno(serial); historial.setTipoEvento(tipo); historial.setUsuario(usuario);
        historial.setFechaHora(TiempoColombiaConfig.ahora()); historial.setObservacion(observacion);
        historial.setValoresAnteriores(anterior); historial.setValoresNuevos(nuevo);
        historiales.save(historial);
    }
}
