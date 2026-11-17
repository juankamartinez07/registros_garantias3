package com.inventario.service;

import com.inventario.config.TiempoColombiaConfig;
import com.inventario.dto.AnulacionSerialRequest;
import com.inventario.dto.ProrrogaSerialRequest;
import com.inventario.dto.EdicionSerialInternoRequest;
import com.inventario.model.*;
import com.inventario.repository.SerialInternoHistorialRepository;
import com.inventario.repository.SerialInternoRepository;
import com.inventario.repository.ProductoRepository;
import com.inventario.repository.TipoProductoRepository;
import com.inventario.repository.ProveedorRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;

@Service
public class AdministracionSerialInternoService {
    private static final Set<String> MOTIVOS = Set.of("ETIQUETA_DANADA", "ERROR_IMPRESION", "ETIQUETA_EXTRAVIADA", "GENERADO_POR_ERROR", "OTRO");
    private final SerialInternoRepository seriales;
    private final SerialInternoHistorialRepository historiales;
    private final UsuarioContextService contexto;
    private final ProductoRepository productos;
    private final TipoProductoRepository tipos;
    private final ProveedorRepository proveedores;

    public AdministracionSerialInternoService(SerialInternoRepository seriales,
            SerialInternoHistorialRepository historiales, UsuarioContextService contexto,
            ProductoRepository productos, TipoProductoRepository tipos, ProveedorRepository proveedores) {
        this.seriales = seriales; this.historiales = historiales; this.contexto = contexto;
        this.productos = productos; this.tipos = tipos; this.proveedores = proveedores;
    }

    @Transactional
    public int editar(EdicionSerialInternoRequest solicitud) {
        String usuario = administrador();
        if (solicitud == null || solicitud.ids() == null || solicitud.ids().isEmpty()
                || solicitud.ids().stream().anyMatch(id -> id == null || id <= 0))
            throw new IllegalArgumentException("Seleccione seriales internos validos.");
        if (solicitud.producto() == null && solicitud.tipo() == null && solicitud.proveedor() == null
                && solicitud.factura() == null && solicitud.fecha() == null)
            throw new IllegalArgumentException("Seleccione al menos un campo para modificar.");
        List<SerialInterno> seleccion = solicitud.ids().stream().distinct().sorted()
                .map(id -> seriales.bloquearPorId(id).orElseThrow(() ->
                        new IllegalArgumentException("Serial interno no encontrado: " + id))).toList();
        seleccion.forEach(this::validarSede);

        TipoProducto tipo = solicitud.tipo() == null ? null : tipos.findByNombreIgnoreCase(normalizar(solicitud.tipo()))
                .orElseThrow(() -> new IllegalArgumentException("La marca o tipo no existe."));
        Proveedor proveedor = solicitud.proveedor() == null ? null : proveedores.findByNombreIgnoreCase(normalizar(solicitud.proveedor()))
                .orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));
        Producto producto = solicitud.producto() == null ? null : productos.findByNombreIgnoreCase(normalizar(solicitud.producto()))
                .orElseThrow(() -> new IllegalArgumentException("La referencia no existe en el catalogo."));
        String factura = solicitud.factura() == null ? null : normalizar(solicitud.factura());

        int modificados = 0;
        for (SerialInterno serial : seleccion) {
            Equipo equipo = serial.getEquipo();
            Map<String, String> antes = new LinkedHashMap<>();
            Map<String, String> despues = new LinkedHashMap<>();
            if (producto != null && !Objects.equals(idProducto(equipo.getProducto()), idProducto(producto))) {
                cambio(antes, despues, "producto", equipo.getProducto() == null ? null : equipo.getProducto().getNombre(), producto.getNombre());
                equipo.setProducto(producto);
            }
            if (tipo != null && !Objects.equals(equipo.getTipoProducto() == null ? null : equipo.getTipoProducto().getNombre(), tipo.getNombre())) {
                cambio(antes, despues, "tipo", equipo.getTipoProducto() == null ? null : equipo.getTipoProducto().getNombre(), tipo.getNombre());
                equipo.setTipoProducto(tipo);
            }
            if (proveedor != null && !Objects.equals(equipo.getProveedor() == null ? null : equipo.getProveedor().getNombre(), proveedor.getNombre())) {
                cambio(antes, despues, "proveedor", equipo.getProveedor() == null ? null : equipo.getProveedor().getNombre(), proveedor.getNombre());
                equipo.setProveedor(proveedor);
            }
            if (factura != null && !Objects.equals(equipo.getFactura(), factura)) {
                cambio(antes, despues, "factura", equipo.getFactura(), factura); equipo.setFactura(factura);
            }
            if (solicitud.fecha() != null && !Objects.equals(equipo.getFecha(), solicitud.fecha().toString())) {
                cambio(antes, despues, "fecha", equipo.getFecha(), solicitud.fecha().toString()); equipo.setFecha(solicitud.fecha().toString());
            }
            if (!antes.isEmpty()) {
                registrar(serial, EventoSerialInterno.DATOS_MODIFICADOS, usuario, "Correccion de datos administrativos.", antes.toString(), despues.toString());
                modificados++;
            }
        }
        return modificados;
    }

    private String normalizar(String valor) {
        if (valor.isBlank()) throw new IllegalArgumentException("Los campos seleccionados no pueden estar vacios.");
        return valor.trim().toUpperCase(Locale.ROOT);
    }

    private Long idProducto(Producto producto) { return producto == null ? null : producto.getId_producto(); }
    private void cambio(Map<String, String> antes, Map<String, String> despues, String campo, String anterior, String nuevo) {
        antes.put(campo, anterior); despues.put(campo, nuevo);
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
