package com.inventario.service;

import com.inventario.dto.GarantiaDTO;
import com.inventario.model.Equipo;
import com.inventario.model.Garantia;
import com.inventario.repository.EquipoRepository;
import com.inventario.repository.GarantiaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
public class GarantiaService {

    public static final String ESTADO_EN_TRAMITE = "En trámite";
    public static final String ESTADO_REPARADO = "Reparado";
    public static final String ESTADO_NO_APLICO = "No aplicó garantía";
    public static final String ESTADO_CAMBIO = "Cambio por equipo nuevo";
    public static final String ESTADO_NOTA_CREDITO = "Nota crédito";

    private static final int DIAS_GARANTIA = 365;
    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            ESTADO_EN_TRAMITE,
            ESTADO_REPARADO,
            ESTADO_NO_APLICO,
            ESTADO_CAMBIO,
            ESTADO_NOTA_CREDITO
    );

    private final GarantiaRepository garantiaRepository;
    private final EquipoRepository equipoRepository;

    public GarantiaService(
            GarantiaRepository garantiaRepository,
            EquipoRepository equipoRepository) {
        this.garantiaRepository = garantiaRepository;
        this.equipoRepository = equipoRepository;
    }

    public Page<Garantia> listar(String serial, String estado, Pageable pageable) {
        return garantiaRepository.buscar(limpiar(serial), limpiar(estado), pageable);
    }

    public Garantia obtener(Long id) {
        return garantiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Garantía no encontrada"));
    }

    public GarantiaDTO preparar(String serial) {
        Equipo equipo = obtenerEquipoApto(serial);
        validarGarantiaAbierta(equipo.getSerial());
        return crearDtoBase(equipo);
    }

    @Transactional
    public Garantia crear(GarantiaDTO dto) {
        Equipo equipo = obtenerEquipoApto(dto.getSerial());
        validarGarantiaAbierta(equipo.getSerial());

        Garantia garantia = new Garantia();
        garantia.setEquipo(equipo);
        aplicarDatos(garantia, dto, equipo);
        garantia.setEstado(ESTADO_EN_TRAMITE);
        return garantiaRepository.save(garantia);
    }

    @Transactional
    public Garantia actualizar(Long id, GarantiaDTO dto) {
        Garantia garantia = obtener(id);
        String serialDto = limpiar(dto.getSerial());
        if (serialDto != null && !serialDto.equalsIgnoreCase(garantia.getSerial())) {
            throw new RuntimeException("No se permite cambiar el serial de una garantía existente.");
        }

        Equipo equipo = garantia.getEquipo();
        aplicarDatos(garantia, dto, equipo);
        return garantiaRepository.save(garantia);
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
                .orElseThrow(() -> new RuntimeException("No se puede tramitar garantía de un serial inexistente."));

        if (!estaEnGarantia(equipo)) {
            throw new RuntimeException("No se puede tramitar garantía porque el serial está fuera del periodo de garantía.");
        }

        return equipo;
    }

    private void validarGarantiaAbierta(String serial) {
        if (garantiaRepository.existsBySerialIgnoreCaseAndEstado(serial, ESTADO_EN_TRAMITE)) {
            throw new RuntimeException("Este serial ya tiene una garantía en trámite.");
        }
    }

    private void aplicarDatos(Garantia garantia, GarantiaDTO dto, Equipo equipo) {
        String estado = limpiar(dto.getEstado());
        if (estado == null) {
            estado = garantia.getEstado() == null ? ESTADO_EN_TRAMITE : garantia.getEstado();
        }

        if (!ESTADOS_VALIDOS.contains(estado)) {
            throw new RuntimeException("Estado de garantía no válido.");
        }

        String motivoNoAplica = limpiar(dto.getMotivoNoAplicaGarantia());
        if (ESTADO_NO_APLICO.equals(estado) && motivoNoAplica == null) {
            throw new RuntimeException("Debe ingresar el motivo cuando no aplica garantía.");
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
        garantia.setEstado(estado);
    }

    private GarantiaDTO crearDtoBase(Equipo equipo) {
        GarantiaDTO dto = new GarantiaDTO();
        dto.setEquipoId(equipo.getId_equipo());
        dto.setSede(equipo.getSede() == null ? "" : equipo.getSede().getNombre());
        dto.setReferenciaProducto(equipo.getProducto() == null ? "" : equipo.getProducto().getNombre());
        dto.setSerial(equipo.getSerial());
        dto.setEstado(ESTADO_EN_TRAMITE);
        dto.setProveedor(equipo.getProveedor() == null ? "" : equipo.getProveedor().getNombre());
        dto.setFacturaProveedor(equipo.getFactura());
        dto.setFechaIngresoSerial(parseFecha(equipo.getFecha()));
        dto.setFechaIngresoGarantia(LocalDate.now());
        return dto;
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
