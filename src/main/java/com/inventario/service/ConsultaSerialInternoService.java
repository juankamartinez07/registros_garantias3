package com.inventario.service;

import com.inventario.config.TiempoColombiaConfig;
import com.inventario.dto.SerialInternoConsultaDTO;
import com.inventario.model.EstadoSerialInterno;
import com.inventario.repository.SerialInternoRepository;
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

    public ConsultaSerialInternoService(SerialInternoRepository seriales, UsuarioContextService contexto) {
        this.seriales = seriales;
        this.contexto = contexto;
    }

    @Transactional(readOnly = true)
    public Page<SerialInternoConsultaDTO> consultar(EstadoSerialInterno estado, String lote,
            String referencia, boolean vencidos, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_TECNICO"))) {
            throw new AccessDeniedException("No tiene permisos para consultar seriales internos.");
        }
        boolean global = contexto.esSuperUsuario();
        Long sede = global ? null : contexto.sedeIdActual();
        if (!global && sede == null) throw new AccessDeniedException("El usuario no tiene sede asignada.");
        var hoy = TiempoColombiaConfig.hoy();
        return seriales.consultar(sede, estado, limpiar(lote), limpiar(referencia), vencidos, hoy, pageable)
                .map(s -> new SerialInternoConsultaDTO(s.getId(), s.getEquipo().getSerial(), s.getLote().getCodigo(),
                        s.getLote().getSedeNombre(), s.getEquipo().getProducto() == null ? null : s.getEquipo().getProducto().getNombre(),
                        s.getEstado(), s.getFechaGeneracion(), s.getFechaLimite(),
                        s.getEstado() == EstadoSerialInterno.PENDIENTE_ACTIVACION && s.getFechaLimite().isBefore(hoy)));
    }

    private String limpiar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
}
