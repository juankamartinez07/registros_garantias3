package com.inventario.service;

import com.inventario.config.TiempoColombiaConfig;
import com.inventario.dto.SerialInternoConsultaDTO;
import com.inventario.model.EstadoSerialInterno;
import com.inventario.repository.SerialInternoRepository;
import com.inventario.repository.LoteSerialInternoRepository;
import com.inventario.repository.SerialInternoHistorialRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaSerialInternoService {
    private final SerialInternoRepository seriales;
    private final UsuarioContextService contexto;
    private final LoteSerialInternoRepository lotes;
    private final SerialInternoHistorialRepository historiales;

    public ConsultaSerialInternoService(SerialInternoRepository seriales, UsuarioContextService contexto,
            LoteSerialInternoRepository lotes, SerialInternoHistorialRepository historiales) {
        this.seriales = seriales;
        this.contexto = contexto;
        this.lotes = lotes;
        this.historiales = historiales;
    }

    @Transactional(readOnly = true)
    public Page<SerialInternoConsultaDTO> consultar(EstadoSerialInterno estado, String lote,
            String referencia, boolean vencidos, Pageable pageable) {
        Long sede = sedeAutorizada();
        var hoy = TiempoColombiaConfig.hoy();
        return seriales.consultar(sede, estado, limpiar(lote), limpiar(referencia), vencidos, hoy, pageable)
                .map(s -> new SerialInternoConsultaDTO(s.getId(), s.getEquipo().getSerial(), s.getLote().getCodigo(),
                        s.getLote().getSedeNombre(), s.getEquipo().getProducto() == null ? null : s.getEquipo().getProducto().getNombre(),
                        s.getEstado(), s.getFechaGeneracion(), s.getFechaLimite(),
                        s.getEstado() == EstadoSerialInterno.PENDIENTE_ACTIVACION && s.getFechaLimite().isBefore(hoy)));
    }

    @Transactional(readOnly = true)
    public Contadores contadores() {
        Long sede = sedeAutorizada();
        return new Contadores(seriales.contarPorSedeYEstado(sede, null),
                seriales.contarPorSedeYEstado(sede, EstadoSerialInterno.PENDIENTE_ACTIVACION),
                seriales.contarPorSedeYEstado(sede, EstadoSerialInterno.ACTIVO),
                seriales.contarPorSedeYEstado(sede, EstadoSerialInterno.ANULADO),
                seriales.contarVencidos(sede, TiempoColombiaConfig.hoy()));
    }

    @Transactional(readOnly = true)
    public Page<LoteDTO> lotes(Pageable pageable) {
        return lotes.consultarPorSede(sedeAutorizada(), pageable).map(l -> new LoteDTO(
                l.getId(), l.getCodigo(), l.getSedeNombre(), l.getUsuarioCreador(), l.getCantidad(),
                l.getFechaCreacion(), l.getFechaLimite()));
    }

    @Transactional(readOnly = true)
    public Page<HistorialDTO> historial(Long id, Pageable pageable) {
        Long sede = sedeAutorizada();
        var serial = seriales.findById(id).orElseThrow(() -> new IllegalArgumentException("Serial interno no encontrado."));
        if (sede != null && !sede.equals(serial.getLote().getSede().getId()))
            throw new AccessDeniedException("No tiene permisos para consultar este serial.");
        return historiales.findBySerialInternoId(id, pageable).map(h -> new HistorialDTO(h.getId(),
                h.getTipoEvento().name(), h.getUsuario(), h.getFechaHora(), h.getObservacion(),
                h.getValoresAnteriores(), h.getValoresNuevos()));
    }

    private Long sedeAutorizada() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_TECNICO"))) {
            throw new AccessDeniedException("No tiene permisos para consultar seriales internos.");
        }
        boolean global = contexto.esSuperUsuario();
        Long sede = global ? null : contexto.sedeIdActual();
        if (!global && sede == null) throw new AccessDeniedException("El usuario no tiene sede asignada.");
        return sede;
    }

    private String limpiar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    public record Contadores(long total, long pendientes, long activos, long anulados, long vencidos) { }
    public record LoteDTO(Long id, String codigo, String sede, String usuario, int cantidad, LocalDateTime fecha, LocalDate fechaLimite) { }
    public record HistorialDTO(Long id, String evento, String usuario, LocalDateTime fecha, String observacion, String anterior, String nuevo) { }
}
