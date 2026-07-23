package com.inventario.service;

import com.inventario.model.ConfiguracionDemo;
import com.inventario.repository.ConfiguracionDemoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class DemoService {

    private final ConfiguracionDemoRepository repository;
    private final UsuarioContextService usuarioContextService;

    public DemoService(ConfiguracionDemoRepository repository, UsuarioContextService usuarioContextService) {
        this.repository = repository;
        this.usuarioContextService = usuarioContextService;
    }

    public DemoEstado estadoActual() {
        ConfiguracionDemo configuracion = obtenerConfiguracion();
        LocalDate fechaInicio = configuracion.getFechaInicioDemo() == null
                ? LocalDate.now()
                : configuracion.getFechaInicioDemo();
        int diasDemo = configuracion.getDiasDemo() == null || configuracion.getDiasDemo() < 1
                ? 10
                : configuracion.getDiasDemo();
        boolean activa = Boolean.TRUE.equals(configuracion.getDemoActiva());
        LocalDate fechaFinalizacion = fechaInicio.plusDays(diasDemo);
        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaFinalizacion);
        boolean expirada = activa && diasRestantes <= 0;
        boolean superAdmin = usuarioContextService.esSuperUsuario();
        String estado = !activa ? "desactivada" : expirada ? "expirada" : "activa";

        return new DemoEstado(
                configuracion.getId(),
                activa,
                fechaInicio,
                diasDemo,
                fechaFinalizacion,
                diasRestantes,
                expirada,
                estado,
                superAdmin,
                activa && !expirada,
                activa && (superAdmin || !expirada));
    }

    public boolean debeBloquearUsuarioActual() {
        DemoEstado estado = estadoActual();
        return estado.demoActiva() && estado.demoExpirada() && !estado.superAdmin();
    }

    public DemoEstado guardar(DemoSolicitud solicitud) {
        ConfiguracionDemo configuracion = obtenerConfiguracion();
        if (solicitud.demoActiva() != null) {
            configuracion.setDemoActiva(solicitud.demoActiva());
        }
        if (solicitud.fechaInicioDemo() != null) {
            configuracion.setFechaInicioDemo(solicitud.fechaInicioDemo());
        }
        if (solicitud.diasDemo() != null) {
            if (solicitud.diasDemo() < 1) {
                throw new RuntimeException("La duracion de la demo debe ser mayor a cero.");
            }
            configuracion.setDiasDemo(solicitud.diasDemo());
        }
        repository.save(configuracion);
        return estadoActual();
    }

    public DemoEstado activar() {
        return guardar(new DemoSolicitud(true, null, null));
    }

    public DemoEstado desactivar() {
        return guardar(new DemoSolicitud(false, null, null));
    }

    public DemoEstado reiniciar() {
        return guardar(new DemoSolicitud(true, LocalDate.now(), null));
    }

    public ConfiguracionDemo obtenerConfiguracion() {
        return repository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    ConfiguracionDemo configuracion = new ConfiguracionDemo();
                    configuracion.setDemoActiva(true);
                    configuracion.setFechaInicioDemo(LocalDate.now());
                    configuracion.setDiasDemo(10);
                    return repository.save(configuracion);
                });
    }

    public record DemoSolicitud(Boolean demoActiva, LocalDate fechaInicioDemo, Integer diasDemo) {
    }

    public record DemoEstado(
            Long id,
            boolean demoActiva,
            LocalDate fechaInicioDemo,
            int diasDemo,
            LocalDate fechaFinalizacion,
            long diasRestantes,
            boolean demoExpirada,
            String estado,
            boolean superAdmin,
            boolean mostrarBannerUsuarios,
            boolean mostrarBanner) {
    }
}
