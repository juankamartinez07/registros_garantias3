package com.inventario.service;

import com.inventario.config.TiempoColombiaConfig;
import com.inventario.dto.ServicioTecnicoActualizacionDTO;
import com.inventario.dto.ServicioTecnicoDTO;
import com.inventario.dto.ServicioTecnicoHistorialDTO;
import com.inventario.model.ServicioTecnico;
import com.inventario.model.ServicioTecnicoHistorial;
import com.inventario.repository.ServicioTecnicoHistorialRepository;
import com.inventario.repository.ServicioTecnicoRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ServicioTecnicoService {

    public static final String ESTADO_RECIBIDO = "Recibido";
    public static final String ESTADO_EN_REVISION = "En revision";
    public static final String ESTADO_LISTO_ENTREGAR = "Listo para entregar";
    public static final String ESTADO_ENTREGADO = "Entregado";
    public static final String ESTADO_NO_REPARADO = "No reparado";

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            ESTADO_RECIBIDO,
            ESTADO_EN_REVISION,
            ESTADO_LISTO_ENTREGAR,
            ESTADO_ENTREGADO,
            ESTADO_NO_REPARADO
    );

    private final ServicioTecnicoRepository servicioTecnicoRepository;
    private final ServicioTecnicoHistorialRepository servicioTecnicoHistorialRepository;
    private final UsuarioContextService usuarioContextService;

    public ServicioTecnicoService(
            ServicioTecnicoRepository servicioTecnicoRepository,
            ServicioTecnicoHistorialRepository servicioTecnicoHistorialRepository,
            UsuarioContextService usuarioContextService) {
        this.servicioTecnicoRepository = servicioTecnicoRepository;
        this.servicioTecnicoHistorialRepository = servicioTecnicoHistorialRepository;
        this.usuarioContextService = usuarioContextService;
    }

    public Page<ServicioTecnico> listar(
            String busqueda,
            String estadoServicio,
            Pageable pageable) {

        validarPuedeUsarModulo();
        String sedeNombre = sedeVisible();
        if (!usuarioContextService.esSuperUsuario() && sedeNombre == null) {
            return Page.empty(pageable);
        }

        return servicioTecnicoRepository.buscar(
                limpiar(busqueda),
                normalizarEstado(estadoServicio),
                sedeNombre,
                pageable);
    }

    public ServicioTecnico obtener(Long id) {
        validarPuedeUsarModulo();
        ServicioTecnico servicio = servicioTecnicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingreso de servicio tecnico no encontrado."));
        validarSedeServicio(servicio);
        return servicio;
    }

    public String sedeComprobante(ServicioTecnico servicio) {
        return sedeEfectiva(servicio);
    }

    @Transactional
    public ServicioTecnico crear(ServicioTecnicoDTO dto) {
        validarPuedeUsarModulo();
        ServicioTecnico servicio = new ServicioTecnico();
        servicio.setTicket(generarTicket());
        servicio.setSede(sedeActualParaRegistro());
        servicio.setUsuarioRecibe(usuarioContextService.usernameActual());
        aplicarDatos(servicio, dto);
        ServicioTecnico guardado = servicioTecnicoRepository.save(servicio);
        registrarHistorial(
                guardado,
                null,
                guardado.getEstadoServicio(),
                "Ingreso de servicio tecnico creado.",
                "CREACION");
        return guardado;
    }

    @Transactional
    public ServicioTecnico actualizar(Long id, ServicioTecnicoDTO dto) {
        validarPuedeEditarServicioTecnico();
        validarPuedeUsarModulo();
        ServicioTecnico servicio = obtener(id);
        aplicarDatos(servicio, dto);
        return servicioTecnicoRepository.save(servicio);
    }

    @Transactional
    public ServicioTecnico actualizarProceso(Long id, ServicioTecnicoActualizacionDTO dto) {
        validarPuedeUsarModulo();
        ServicioTecnico servicio = obtener(id);
        String estadoAnterior = servicio.getEstadoServicio();
        String estadoNuevo = normalizarEstado(dto.getEstadoServicio());

        if (estadoNuevo == null) {
            throw new RuntimeException("Debe seleccionar un estado de servicio valido.");
        }

        servicio.setEstadoServicio(estadoNuevo);
        ServicioTecnico guardado = servicioTecnicoRepository.save(servicio);
        registrarHistorial(
                guardado,
                estadoAnterior,
                estadoNuevo,
                mayuscula(dto.getObservacion()),
                tipoEvento(estadoNuevo));
        return guardado;
    }

    public List<ServicioTecnicoHistorialDTO> historial(Long id) {
        obtener(id);
        return servicioTecnicoHistorialRepository.findByServicioTecnicoIdOrderByFechaHoraDesc(id)
                .stream()
                .map(this::aDto)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        validarPuedeEliminar();
        ServicioTecnico servicio = servicioTecnicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingreso de servicio tecnico no encontrado."));
        validarSedeServicio(servicio);
        servicioTecnicoRepository.deleteById(id);
    }

    private void aplicarDatos(ServicioTecnico servicio, ServicioTecnicoDTO dto) {
        String cliente = mayuscula(dto.getCliente());
        String telefono = limpiar(dto.getTelefono());
        String serial = mayuscula(dto.getSerial());
        String producto = mayuscula(dto.getProductoReferencia());
        String motivo = mayuscula(dto.getMotivoRevision());
        String estadoFisico = mayuscula(dto.getEstadoFisico());
        String estadoServicio = normalizarEstado(dto.getEstadoServicio());

        if (cliente == null) {
            throw new RuntimeException("El cliente es obligatorio.");
        }
        if (telefono == null) {
            throw new RuntimeException("El telefono del cliente es obligatorio.");
        }
        if (serial == null) {
            throw new RuntimeException("El serial es obligatorio.");
        }
        if (producto == null) {
            throw new RuntimeException("El equipo o referencia es obligatorio.");
        }
        if (motivo == null) {
            throw new RuntimeException("El motivo de revision es obligatorio.");
        }
        if (estadoFisico == null) {
            throw new RuntimeException("El estado fisico del equipo es obligatorio.");
        }
        if (estadoServicio == null) {
            throw new RuntimeException("Debe seleccionar un estado del servicio.");
        }

        servicio.setFechaIngreso(dto.getFechaIngreso() == null ? TiempoColombiaConfig.hoy() : dto.getFechaIngreso());
        servicio.setCliente(cliente);
        servicio.setTelefono(telefono);
        servicio.setSerial(serial);
        servicio.setProductoReferencia(producto);
        servicio.setMarca(mayuscula(dto.getMarca()));
        servicio.setMotivoRevision(motivo);
        servicio.setEstadoFisico(estadoFisico);
        servicio.setObservaciones(mayuscula(dto.getObservaciones()));
        servicio.setEstadoServicio(estadoServicio);
    }

    private String generarTicket() {
        int siguiente = 1;
        String maximo = servicioTecnicoRepository.maxTicketCorto();

        if (maximo != null) {
            siguiente = Integer.parseInt(maximo) + 1;
        }

        while (siguiente <= 99999) {
            String ticket = String.format("%05d", siguiente);
            if (!servicioTecnicoRepository.existsByTicket(ticket)) {
                return ticket;
            }
            siguiente++;
        }

        throw new RuntimeException("No hay numeros de ticket disponibles para servicio tecnico.");
    }

    private String sedeActualParaRegistro() {
        String sede = limpiar(usuarioContextService.sedeNombreActual());
        if (!usuarioContextService.esSuperUsuario() && sede == null) {
            throw new RuntimeException("El usuario actual no tiene sede asignada.");
        }
        return sede;
    }

    private String sedeVisible() {
        return usuarioContextService.esSuperUsuario() ? null : limpiar(usuarioContextService.sedeNombreActual());
    }

    private void validarSedeServicio(ServicioTecnico servicio) {
        if (usuarioContextService.esSuperUsuario()) {
            return;
        }
        String sedeUsuario = limpiar(usuarioContextService.sedeNombreActual());
        String sedeServicio = limpiar(sedeEfectiva(servicio));
        if (sedeUsuario == null || sedeServicio == null || !sedeUsuario.equalsIgnoreCase(sedeServicio)) {
            throw new RuntimeException("No tiene permisos para modificar registros de otra sede.");
        }
    }

    private String sedeEfectiva(ServicioTecnico servicio) {
        String sedeRegistro = limpiar(servicio == null ? null : servicio.getSede());
        if (sedeRegistro != null) {
            return sedeRegistro;
        }

        String sedeUsuarioRecibe = usuarioContextService.sedeNombreDeUsuario(
                servicio == null ? null : servicio.getUsuarioRecibe());
        if (limpiar(sedeUsuarioRecibe) != null) {
            return limpiar(sedeUsuarioRecibe);
        }

        return limpiar(usuarioContextService.sedeNombreActual());
    }

    private String normalizarEstado(String estado) {
        String limpio = limpiar(estado);
        if (limpio == null) {
            return null;
        }

        String normalizado = switch (limpio.toUpperCase(Locale.ROOT)) {
            case "RECIBIDO" -> ESTADO_RECIBIDO;
            case "EN_REVISION", "EN REVISIÓN", "EN REVISION" -> ESTADO_EN_REVISION;
            case "LISTO_PARA_ENTREGAR", "LISTO PARA ENTREGAR" -> ESTADO_LISTO_ENTREGAR;
            case "ENTREGADO" -> ESTADO_ENTREGADO;
            case "NO_REPARADO", "NO REPARADO" -> ESTADO_NO_REPARADO;
            default -> limpio;
        };

        if (!ESTADOS_VALIDOS.contains(normalizado)) {
            throw new RuntimeException("Debe seleccionar un estado de servicio valido.");
        }

        return normalizado;
    }

    private void validarPuedeUsarModulo() {
        if (!tieneRol("SUPER_ADMIN") && !tieneRol("TECNICO")) {
            throw new RuntimeException("Permisos insuficientes. Solo los usuarios TECNICO o SUPER_ADMIN pueden usar servicio tecnico.");
        }
    }

    private void validarPuedeEliminar() {
        if (!tieneRol("SUPER_ADMIN")) {
            throw new RuntimeException("Permisos insuficientes. Solo los usuarios SUPER_ADMIN pueden eliminar ingresos de servicio tecnico.");
        }
    }

    private void validarPuedeEditarServicioTecnico() {
        if (!tieneRol("SUPER_ADMIN")) {
            throw new RuntimeException("Permisos insuficientes.");
        }
    }

    private String tipoEvento(String estado) {
        if (ESTADO_ENTREGADO.equals(estado)) {
            return "ENTREGA";
        }
        if (ESTADO_NO_REPARADO.equals(estado)) {
            return "CIERRE";
        }
        return "ACTUALIZACION";
    }

    private void registrarHistorial(
            ServicioTecnico servicio,
            String estadoAnterior,
            String estadoNuevo,
            String observacion,
            String tipoEvento) {

        ServicioTecnicoHistorial historial = new ServicioTecnicoHistorial();
        historial.setServicioTecnico(servicio);
        historial.setUsuario(usuarioContextService.usernameActual());
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(estadoNuevo);
        historial.setObservacion(observacion);
        historial.setTipoEvento(tipoEvento);
        servicioTecnicoHistorialRepository.save(historial);
    }

    private ServicioTecnicoHistorialDTO aDto(ServicioTecnicoHistorial historial) {
        ServicioTecnicoHistorialDTO dto = new ServicioTecnicoHistorialDTO();
        dto.setId(historial.getId());
        dto.setFechaHora(historial.getFechaHora());
        dto.setUsuario(historial.getUsuario());
        dto.setEstadoAnterior(historial.getEstadoAnterior());
        dto.setEstadoNuevo(historial.getEstadoNuevo());
        dto.setObservacion(historial.getObservacion());
        dto.setTipoEvento(historial.getTipoEvento());
        return dto;
    }

    private boolean tieneRol(String rol) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + rol));
    }

    private String mayuscula(String valor) {
        String limpio = limpiar(valor);
        return limpio == null ? null : limpio.toUpperCase(Locale.ROOT);
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
