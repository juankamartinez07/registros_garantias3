package com.inventario.service;

import com.inventario.config.TiempoColombiaConfig;
import com.inventario.dto.EquipoDTO;
import com.inventario.dto.GenerarLoteSerialesRequest;
import com.inventario.dto.LoteSerialInternoRespuesta;
import com.inventario.model.*;
import com.inventario.repository.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LoteSerialInternoService {
    private final GeneradorSerialInterno generador;
    private final EquipoService equipos;
    private final EquipoRepository equipoRepository;
    private final LoteSerialInternoRepository lotes;
    private final SerialInternoRepository internos;
    private final SerialInternoHistorialRepository historiales;
    private final UsuarioContextService contexto;
    private final JdbcTemplate jdbc;

    public LoteSerialInternoService(GeneradorSerialInterno generador, EquipoService equipos,
            EquipoRepository equipoRepository, LoteSerialInternoRepository lotes,
            SerialInternoRepository internos, SerialInternoHistorialRepository historiales,
            UsuarioContextService contexto, JdbcTemplate jdbc) {
        this.generador = generador; this.equipos = equipos; this.equipoRepository = equipoRepository;
        this.lotes = lotes; this.internos = internos; this.historiales = historiales;
        this.contexto = contexto; this.jdbc = jdbc;
    }

    @Transactional
    public LoteSerialInternoRespuesta generar(GenerarLoteSerialesRequest solicitud) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Permisos insuficientes para generar seriales.");
        }
        if (solicitud == null || solicitud.cantidad() < 1 || solicitud.cantidad() > 100
                || solicitud.fecha() == null || vacio(solicitud.producto()) || vacio(solicitud.tipo())
                || vacio(solicitud.proveedor()) || vacio(solicitud.factura())) {
            throw new IllegalArgumentException("Complete los datos requeridos y seleccione entre 1 y 100 seriales.");
        }
        Usuario usuario = contexto.usuarioActual();
        if (usuario == null || usuario.getSede() == null || usuario.getSede().getId() == null) {
            throw new IllegalArgumentException("El usuario debe tener una sede asignada.");
        }
        verificarIndiceUnico();
        LocalDateTime ahora = TiempoColombiaConfig.ahora();
        LoteSerialInterno lote = new LoteSerialInterno();
        lote.setCodigo("GS-" + UUID.randomUUID());
        lote.setSede(usuario.getSede());
        lote.setSedeNombre(usuario.getSede().getNombre());
        lote.setUsuarioCreador(usuario.getUsername());
        lote.setCantidad(solicitud.cantidad());
        lote.setFechaCreacion(ahora);
        lote.setFechaLimite(ahora.toLocalDate().plusDays(7));
        lotes.save(lote);
        List<String> resultado = new ArrayList<>();
        Set<String> generados = new HashSet<>();
        for (int i = 0; i < solicitud.cantidad(); i++) {
            String serial = nuevoSerial(generados);
            EquipoDTO dto = new EquipoDTO();
            dto.setSerial(serial); dto.setProducto(solicitud.producto()); dto.setTipo(solicitud.tipo());
            dto.setProveedor(solicitud.proveedor()); dto.setFactura(solicitud.factura());
            dto.setFecha(solicitud.fecha().toString()); dto.setObservaciones(solicitud.observaciones());
            Equipo equipo = equipos.guardarCompleto(dto);
            equipoRepository.flush();
            SerialInterno interno = new SerialInterno();
            interno.setEquipo(equipo); interno.setLote(lote); interno.setFechaGeneracion(ahora);
            interno.setFechaLimite(lote.getFechaLimite()); interno.setEstado(EstadoSerialInterno.PENDIENTE_ACTIVACION);
            internos.save(interno);
            SerialInternoHistorial historial = new SerialInternoHistorial();
            historial.setSerialInterno(interno); historial.setTipoEvento(EventoSerialInterno.SERIAL_GENERADO);
            historial.setUsuario(usuario.getUsername()); historial.setFechaHora(ahora);
            historial.setObservacion("Serial interno generado y pendiente de activacion.");
            historiales.save(historial);
            resultado.add(serial);
        }
        return new LoteSerialInternoRespuesta(lote.getId(), lote.getCodigo(), ahora,
                lote.getFechaLimite(), resultado.size(), resultado, EstadoSerialInterno.PENDIENTE_ACTIVACION);
    }

    private String nuevoSerial(Set<String> generados) {
        for (int intento = 0; intento < 100; intento++) {
            String serial = generador.generar();
            if (generados.add(serial) && !equipoRepository.existsBySerialIgnoreCase(serial)) return serial;
        }
        throw new IllegalStateException("No fue posible generar seriales unicos; intente nuevamente.");
    }

    private void verificarIndiceUnico() {
        Integer indices = jdbc.queryForObject("""
                select count(*) from (
                    select index_name from information_schema.statistics
                    where table_schema = database() and table_name = 'equipos' and non_unique = 0
                    group by index_name
                    having count(*) = 1 and max(column_name) = 'serial' and max(sub_part) is null
                ) indices_unicos
                """, Integer.class);
        if (indices == null || indices == 0) {
            throw new IllegalStateException("La tabla equipos requiere un indice unico completo sobre serial antes de generar lotes.");
        }
    }

    private boolean vacio(String valor) { return valor == null || valor.isBlank(); }
}
