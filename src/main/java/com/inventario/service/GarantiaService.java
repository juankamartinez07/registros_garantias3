package com.inventario.service;

import com.inventario.dto.DashboardGarantias;
import com.inventario.dto.GarantiaDTO;
import com.inventario.model.Equipo;
import com.inventario.model.Garantia;
import com.inventario.repository.EquipoRepository;
import com.inventario.repository.GarantiaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
public class GarantiaService {

    public static final String ESTADO_GENERAL_ABIERTO = "Abierto";
    public static final String ESTADO_GENERAL_CERRADO = "Cerrado";
    public static final String ESTADO_EN_TRAMITE = "En tramite";
    public static final String ESTADO_REVISION_INTERNA = "En revision interna";
    public static final String ESTADO_ENVIADO_PROVEEDOR = "Enviado a proveedor";
    public static final String ESTADO_REPARADO = "Reparado";
    public static final String ESTADO_NO_APLICO = "No aplico garantia";
    public static final String ESTADO_CAMBIO = "Cambio por equipo nuevo";
    public static final String ESTADO_NOTA_CREDITO = "Nota credito";

    private static final int DIAS_GARANTIA = 365;
    private static final Set<String> ESTADOS_GENERALES_VALIDOS = Set.of(
            ESTADO_GENERAL_ABIERTO,
            ESTADO_GENERAL_CERRADO
    );
    private static final Set<String> ESTADOS_ABIERTOS_VALIDOS = Set.of(
            ESTADO_EN_TRAMITE,
            ESTADO_REVISION_INTERNA,
            ESTADO_ENVIADO_PROVEEDOR
    );
    private static final Set<String> ESTADOS_CERRADOS_VALIDOS = Set.of(
            ESTADO_REPARADO,
            ESTADO_NO_APLICO,
            ESTADO_CAMBIO,
            ESTADO_NOTA_CREDITO
    );

    private final GarantiaRepository garantiaRepository;
    private final EquipoRepository equipoRepository;
    private final UsuarioContextService usuarioContextService;

    public GarantiaService(
            GarantiaRepository garantiaRepository,
            EquipoRepository equipoRepository,
            UsuarioContextService usuarioContextService) {
        this.garantiaRepository = garantiaRepository;
        this.equipoRepository = equipoRepository;
        this.usuarioContextService = usuarioContextService;
    }

    public Page<Garantia> listar(
            String serial,
            String estado,
            String estadoGeneral,
            String estadoEspecifico,
            String filtro,
            Pageable pageable) {

        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
        LocalDate fechaLimite10Dias = hoy.minusDays(10);
        String filtroLimpio = limpiar(filtro);
        String sedeNombre = sedeVisible();

        if (!usuarioContextService.esSuperUsuario() && sedeNombre == null) {
            return Page.empty(pageable);
        }

        return garantiaRepository.buscar(
                limpiar(serial),
                normalizarEstadoLibre(estado),
                normalizarEstadoGeneral(estadoGeneral),
                normalizarEstadoEspecifico(estadoEspecifico),
                "ingresadasMesActual".equalsIgnoreCase(filtroLimpio),
                "sinCasoProveedor".equalsIgnoreCase(filtroLimpio),
                "abiertas10dias".equalsIgnoreCase(filtroLimpio),
                "tiempoAbiertas".equalsIgnoreCase(filtroLimpio),
                inicioMes,
                finMes,
                fechaLimite10Dias,
                sedeNombre,
                pageable);
    }

    public DashboardGarantias dashboard() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
        LocalDate fechaLimite10Dias = hoy.minusDays(10);
        String sedeNombre = sedeVisible();

        DashboardGarantias dashboard = new DashboardGarantias();
        if (!usuarioContextService.esSuperUsuario() && sedeNombre == null) {
            aplicarEstadoTiempoAbiertas(dashboard, hoy, null);
            return dashboard;
        }

        dashboard.setTotalGarantias(sedeNombre == null ? garantiaRepository.count() : garantiaRepository.countBySede(sedeNombre));
        dashboard.setAbiertas(contarPorEstadoGeneral(sedeNombre, ESTADO_GENERAL_ABIERTO));
        dashboard.setCerradas(contarPorEstadoGeneral(sedeNombre, ESTADO_GENERAL_CERRADO));
        dashboard.setIngresadasMesActual(sedeNombre == null
                ? garantiaRepository.countByFechaIngresoGarantiaBetween(inicioMes, finMes)
                : garantiaRepository.countBySedeAndFechaIngresoGarantiaBetween(sedeNombre, inicioMes, finMes));
        dashboard.setAbiertasSinCasoProveedor(sedeNombre == null
                ? garantiaRepository.contarAbiertasSinCasoProveedor()
                : garantiaRepository.contarAbiertasSinCasoProveedorPorSede(sedeNombre));
        dashboard.setAbiertasMas10Dias(sedeNombre == null
                ? garantiaRepository.contarAbiertasMas10Dias(fechaLimite10Dias)
                : garantiaRepository.contarAbiertasMas10DiasPorSede(sedeNombre, fechaLimite10Dias));
        dashboard.setEnTramite(contarPorEstadoEspecifico(sedeNombre, ESTADO_EN_TRAMITE));
        dashboard.setEnRevisionInterna(contarPorEstadoEspecifico(sedeNombre, ESTADO_REVISION_INTERNA));
        dashboard.setEnviadoAProveedor(contarPorEstadoEspecifico(sedeNombre, ESTADO_ENVIADO_PROVEEDOR));
        dashboard.setReparado(contarPorEstadoEspecifico(sedeNombre, ESTADO_REPARADO));
        dashboard.setNoAplicoGarantia(contarPorEstadoEspecifico(sedeNombre, ESTADO_NO_APLICO));
        dashboard.setCambioEquipoNuevo(contarPorEstadoEspecifico(sedeNombre, ESTADO_CAMBIO));
        dashboard.setNotaCredito(contarPorEstadoEspecifico(sedeNombre, ESTADO_NOTA_CREDITO));
        aplicarEstadoTiempoAbiertas(dashboard, hoy, sedeNombre);
        return dashboard;
    }

    public Garantia obtener(Long id) {
        Garantia garantia = garantiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Garantia no encontrada"));
        validarSedeGarantia(garantia);
        return garantia;
    }

    public GarantiaDTO preparar(String serial) {
        validarPuedeGestionarGarantias();
        Equipo equipo = obtenerEquipoApto(serial);
        validarGarantiaAbierta(equipo.getSerial());
        return crearDtoBase(equipo);
    }

    @Transactional
    public Garantia crear(GarantiaDTO dto) {
        validarPuedeGestionarGarantias();
        Equipo equipo = obtenerEquipoApto(dto.getSerial());
        validarGarantiaAbierta(equipo.getSerial());

        Garantia garantia = new Garantia();
        garantia.setEquipo(equipo);
        garantia.setNumeroTicket(generarNumeroTicket());
        garantia.setUsuarioCreacion(nombreUsuarioActual());
        aplicarDatos(garantia, dto, equipo);
        return garantiaRepository.save(garantia);
    }

    @Transactional
    public Garantia actualizar(Long id, GarantiaDTO dto) {
        validarPuedeGestionarGarantias();
        Garantia garantia = obtener(id);
        String serialDto = limpiar(dto.getSerial());
        if (serialDto != null && !serialDto.equalsIgnoreCase(garantia.getSerial())) {
            throw new RuntimeException("No se permite cambiar el serial de una garantia existente.");
        }

        Equipo equipo = garantia.getEquipo();
        aplicarDatos(garantia, dto, equipo);
        return garantiaRepository.save(garantia);
    }

    @Transactional
    public void eliminar(Long id) {
        validarPuedeEliminarGarantias();
        Garantia garantia = garantiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Garantia no encontrada."));
        validarSedeGarantia(garantia);
        garantiaRepository.deleteById(id);
    }

    public boolean estaEnGarantia(Equipo equipo) {
        LocalDate fecha = parseFecha(equipo == null ? null : equipo.getFecha());
        if (fecha == null || fecha.isAfter(LocalDate.now())) {
            return false;
        }
        long dias = ChronoUnit.DAYS.between(fecha, LocalDate.now());
        return dias <= DIAS_GARANTIA;
    }

    private Equipo obtenerEquipoApto(String serial) {
        String serialLimpio = limpiar(serial);
        if (serialLimpio == null) {
            throw new RuntimeException("El serial es obligatorio.");
        }

        Equipo equipo = equipoRepository.findBySerial(serialLimpio)
                .orElseThrow(() -> new RuntimeException("No se puede tramitar garantia de un serial inexistente."));

        if (!estaEnGarantia(equipo)) {
            throw new RuntimeException("No se puede tramitar garantia porque el serial esta fuera del periodo de garantia.");
        }

        usuarioContextService.validarMismaSede(equipo.getSede() == null ? null : equipo.getSede().getId());

        return equipo;
    }

    private void validarGarantiaAbierta(String serial) {
        if (garantiaRepository.existsBySerialIgnoreCaseAndEstadoGeneral(serial, ESTADO_GENERAL_ABIERTO)) {
            throw new RuntimeException("Este serial ya tiene una garantia abierta.");
        }
    }

    private void aplicarDatos(Garantia garantia, GarantiaDTO dto, Equipo equipo) {
        String estadoGeneral = limpiar(dto.getEstadoGeneral());
        String estadoEspecifico = limpiar(dto.getEstadoEspecifico());

        if (estadoGeneral == null && limpiar(dto.getEstado()) != null) {
            estadoGeneral = estadoGeneralDesdeEstadoAnterior(dto.getEstado());
            estadoEspecifico = limpiar(dto.getEstado());
        }

        if (estadoGeneral == null) {
            estadoGeneral = garantia.getEstadoGeneral() == null ? ESTADO_GENERAL_ABIERTO : garantia.getEstadoGeneral();
        }

        if (estadoEspecifico == null) {
            estadoEspecifico = garantia.getEstadoEspecifico() == null ? ESTADO_EN_TRAMITE : garantia.getEstadoEspecifico();
        }

        validarEstados(estadoGeneral, estadoEspecifico);

        String motivoNoAplica = limpiar(dto.getMotivoNoAplicaGarantia());
        if (ESTADO_GENERAL_CERRADO.equals(estadoGeneral)
                && ESTADO_NO_APLICO.equals(estadoEspecifico)
                && motivoNoAplica == null) {
            throw new RuntimeException("Debe ingresar el motivo por el cual no aplico la garantia.");
        }

        if (!ESTADO_NO_APLICO.equals(estadoEspecifico)) {
            motivoNoAplica = null;
        }

        garantia.setSede(valorBase(dto.getSede(), equipo == null || equipo.getSede() == null ? null : equipo.getSede().getNombre()));
        garantia.setReferenciaProducto(valorBase(dto.getReferenciaProducto(), equipo == null || equipo.getProducto() == null ? null : equipo.getProducto().getNombre()));
        garantia.setSerial(valorBase(dto.getSerial(), equipo == null ? null : equipo.getSerial()));
        garantia.setProveedor(valorBase(dto.getProveedor(), equipo == null || equipo.getProveedor() == null ? null : equipo.getProveedor().getNombre()));
        garantia.setFacturaProveedor(valorBase(dto.getFacturaProveedor(), equipo == null ? null : equipo.getFactura()));
        garantia.setFechaIngresoSerial(dto.getFechaIngresoSerial() != null ? dto.getFechaIngresoSerial() : parseFecha(equipo == null ? null : equipo.getFecha()));
        garantia.setFechaIngresoGarantia(dto.getFechaIngresoGarantia() != null ? dto.getFechaIngresoGarantia() : LocalDate.now());
        garantia.setMotivosGarantia(limpiar(dto.getMotivosGarantia()));
        garantia.setNumeroCasoProveedor(limpiar(dto.getNumeroCasoProveedor()));
        garantia.setMotivoNoAplicaGarantia(motivoNoAplica);
        garantia.setObservaciones(limpiar(dto.getObservaciones()));
        garantia.setEstadoGeneral(estadoGeneral);
        garantia.setEstadoEspecifico(estadoEspecifico);
        garantia.setEstado(estadoEspecifico);
    }

    private GarantiaDTO crearDtoBase(Equipo equipo) {
        GarantiaDTO dto = new GarantiaDTO();
        dto.setEquipoId(equipo.getId_equipo());
        dto.setNumeroTicket("");
        dto.setSede(equipo.getSede() == null ? "" : equipo.getSede().getNombre());
        dto.setReferenciaProducto(equipo.getProducto() == null ? "" : equipo.getProducto().getNombre());
        dto.setSerial(equipo.getSerial());
        dto.setEstadoGeneral(ESTADO_GENERAL_ABIERTO);
        dto.setEstadoEspecifico(ESTADO_EN_TRAMITE);
        dto.setEstado(ESTADO_EN_TRAMITE);
        dto.setProveedor(equipo.getProveedor() == null ? "" : equipo.getProveedor().getNombre());
        dto.setFacturaProveedor(equipo.getFactura());
        dto.setFechaIngresoSerial(parseFecha(equipo.getFecha()));
        dto.setFechaIngresoGarantia(LocalDate.now());
        return dto;
    }

    private String generarNumeroTicket() {
        int siguiente = 1;
        String maximo = garantiaRepository.maxNumeroTicketCorto();

        if (maximo != null) {
            siguiente = Integer.parseInt(maximo) + 1;
        }

        while (siguiente <= 99999) {
            String ticket = String.format("%05d", siguiente);
            if (!garantiaRepository.existsByNumeroTicket(ticket)) {
                return ticket;
            }
            siguiente++;
        }

        throw new RuntimeException("No hay numeros de ticket disponibles.");
    }

    private void validarEstados(String estadoGeneral, String estadoEspecifico) {
        if (!ESTADOS_GENERALES_VALIDOS.contains(estadoGeneral)) {
            throw new RuntimeException("Debe seleccionar un estado general valido.");
        }

        if (ESTADO_GENERAL_ABIERTO.equals(estadoGeneral)
                && !ESTADOS_ABIERTOS_VALIDOS.contains(estadoEspecifico)) {
            throw new RuntimeException("Debe seleccionar un estado especifico valido para garantias abiertas.");
        }

        if (ESTADO_GENERAL_CERRADO.equals(estadoGeneral)
                && !ESTADOS_CERRADOS_VALIDOS.contains(estadoEspecifico)) {
            throw new RuntimeException("Debe seleccionar un estado especifico valido para garantias cerradas.");
        }
    }

    private String estadoGeneralDesdeEstadoAnterior(String estado) {
        String estadoLimpio = limpiar(estado);
        if (ESTADOS_CERRADOS_VALIDOS.contains(estadoLimpio)) {
            return ESTADO_GENERAL_CERRADO;
        }
        return ESTADO_GENERAL_ABIERTO;
    }

    private void aplicarEstadoTiempoAbiertas(DashboardGarantias dashboard, LocalDate hoy, String sedeNombre) {
        LocalDate fechaMasAntigua = sedeNombre == null
                ? garantiaRepository.fechaAbiertaMasAntigua()
                : garantiaRepository.fechaAbiertaMasAntiguaPorSede(sedeNombre);
        if (fechaMasAntigua == null) {
            dashboard.setMaxDiasAbierta(0);
            dashboard.setEstadoTiempoAbiertas("normal");
            dashboard.setTextoTiempoAbiertas("Sin casos abiertos");
            return;
        }

        long dias = Math.max(0, ChronoUnit.DAYS.between(fechaMasAntigua, hoy));
        dashboard.setMaxDiasAbierta(dias);

        if (dias >= 10) {
            dashboard.setEstadoTiempoAbiertas("critico");
            dashboard.setTextoTiempoAbiertas("Casos abiertos +10 dias");
        } else if (dias >= 5) {
            dashboard.setEstadoTiempoAbiertas("seguimiento");
            dashboard.setTextoTiempoAbiertas("Casos abiertos en seguimiento");
        } else {
            dashboard.setEstadoTiempoAbiertas("normal");
            dashboard.setTextoTiempoAbiertas("Casos abiertos en tiempo normal");
        }
    }

    private long contarPorEstadoGeneral(String sedeNombre, String estadoGeneral) {
        return sedeNombre == null
                ? garantiaRepository.countByEstadoGeneral(estadoGeneral)
                : garantiaRepository.countBySedeAndEstadoGeneral(sedeNombre, estadoGeneral);
    }

    private long contarPorEstadoEspecifico(String sedeNombre, String estadoEspecifico) {
        return sedeNombre == null
                ? garantiaRepository.countByEstadoEspecifico(estadoEspecifico)
                : garantiaRepository.countBySedeAndEstadoEspecifico(sedeNombre, estadoEspecifico);
    }

    private String sedeVisible() {
        return usuarioContextService.esSuperUsuario() ? null : limpiar(usuarioContextService.sedeNombreActual());
    }

    private void validarSedeGarantia(Garantia garantia) {
        if (usuarioContextService.esSuperUsuario()) {
            return;
        }
        String sedeUsuario = limpiar(usuarioContextService.sedeNombreActual());
        String sedeGarantia = limpiar(garantia.getSede());
        if (sedeUsuario == null || sedeGarantia == null || !sedeUsuario.equalsIgnoreCase(sedeGarantia)) {
            throw new RuntimeException("No tiene permisos para modificar registros de otra sede.");
        }
    }

    private String normalizarEstadoGeneral(String estadoGeneral) {
        String limpio = limpiar(estadoGeneral);
        if (limpio == null) {
            return null;
        }
        if ("ABIERTO".equalsIgnoreCase(limpio) || ESTADO_GENERAL_ABIERTO.equalsIgnoreCase(limpio)) {
            return ESTADO_GENERAL_ABIERTO;
        }
        if ("CERRADO".equalsIgnoreCase(limpio) || ESTADO_GENERAL_CERRADO.equalsIgnoreCase(limpio)) {
            return ESTADO_GENERAL_CERRADO;
        }
        return limpio;
    }

    private String normalizarEstadoEspecifico(String estadoEspecifico) {
        String limpio = limpiar(estadoEspecifico);
        if (limpio == null) {
            return null;
        }

        return switch (limpio.toUpperCase()) {
            case "EN_TRAMITE" -> ESTADO_EN_TRAMITE;
            case "EN_REVISION_INTERNA" -> ESTADO_REVISION_INTERNA;
            case "ENVIADO_A_PROVEEDOR" -> ESTADO_ENVIADO_PROVEEDOR;
            case "REPARADO" -> ESTADO_REPARADO;
            case "NO_APLICO_GARANTIA" -> ESTADO_NO_APLICO;
            case "CAMBIO_EQUIPO_NUEVO" -> ESTADO_CAMBIO;
            case "NOTA_CREDITO" -> ESTADO_NOTA_CREDITO;
            default -> limpio;
        };
    }

    private String normalizarEstadoLibre(String estado) {
        String limpio = limpiar(estado);
        if (limpio == null) {
            return null;
        }
        String general = normalizarEstadoGeneral(limpio);
        if (ESTADO_GENERAL_ABIERTO.equals(general) || ESTADO_GENERAL_CERRADO.equals(general)) {
            return general;
        }
        return normalizarEstadoEspecifico(limpio);
    }

    private void validarPuedeGestionarGarantias() {
        if (!tieneRol("SUPER_ADMIN") && !tieneRol("ADMIN") && !tieneRol("TECNICO")) {
            throw new RuntimeException("Permisos insuficientes. Solo los usuarios TECNICO, ADMIN o SUPER_ADMIN pueden tramitar garantias.");
        }
    }

    private void validarPuedeEliminarGarantias() {
        if (!tieneRol("SUPER_ADMIN")) {
            throw new RuntimeException("Permisos insuficientes. Solo los usuarios SUPER_ADMIN pueden eliminar garantias.");
        }
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

    private String nombreUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    private LocalDate parseFecha(String fecha) {
        try {
            String valor = limpiar(fecha);
            return valor == null ? null : LocalDate.parse(valor);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private String valorBase(String valorDto, String valorEquipo) {
        String limpio = limpiar(valorDto);
        return limpio == null ? limpiar(valorEquipo) : limpio;
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
