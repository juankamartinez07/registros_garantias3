package com.inventario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_demo")
public class ConfiguracionDemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "demo_activa", nullable = false)
    private Boolean demoActiva = true;

    @Column(name = "fecha_inicio_demo", nullable = false)
    private LocalDate fechaInicioDemo = LocalDate.now();

    @Column(name = "dias_demo", nullable = false)
    private Integer diasDemo = 10;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();
        fechaCreacion = ahora;
        fechaActualizacion = ahora;
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Boolean getDemoActiva() {
        return demoActiva;
    }

    public void setDemoActiva(Boolean demoActiva) {
        this.demoActiva = demoActiva;
    }

    public LocalDate getFechaInicioDemo() {
        return fechaInicioDemo;
    }

    public void setFechaInicioDemo(LocalDate fechaInicioDemo) {
        this.fechaInicioDemo = fechaInicioDemo;
    }

    public Integer getDiasDemo() {
        return diasDemo;
    }

    public void setDiasDemo(Integer diasDemo) {
        this.diasDemo = diasDemo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
