package com.inventario.model;

import com.inventario.config.TiempoColombiaConfig;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "garantia_historial")
public class GarantiaHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "garantia_id")
    private Garantia garantia;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "estado_general_anterior")
    private String estadoGeneralAnterior;

    @Column(name = "estado_general_nuevo")
    private String estadoGeneralNuevo;

    @Column(name = "estado_especifico_anterior")
    private String estadoEspecificoAnterior;

    @Column(name = "estado_especifico_nuevo")
    private String estadoEspecificoNuevo;

    @Column(name = "numero_caso_proveedor")
    private String numeroCasoProveedor;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "tipo_evento")
    private String tipoEvento;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) {
            fechaHora = TiempoColombiaConfig.ahora();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Garantia getGarantia() {
        return garantia;
    }

    public void setGarantia(Garantia garantia) {
        this.garantia = garantia;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getEstadoGeneralAnterior() {
        return estadoGeneralAnterior;
    }

    public void setEstadoGeneralAnterior(String estadoGeneralAnterior) {
        this.estadoGeneralAnterior = estadoGeneralAnterior;
    }

    public String getEstadoGeneralNuevo() {
        return estadoGeneralNuevo;
    }

    public void setEstadoGeneralNuevo(String estadoGeneralNuevo) {
        this.estadoGeneralNuevo = estadoGeneralNuevo;
    }

    public String getEstadoEspecificoAnterior() {
        return estadoEspecificoAnterior;
    }

    public void setEstadoEspecificoAnterior(String estadoEspecificoAnterior) {
        this.estadoEspecificoAnterior = estadoEspecificoAnterior;
    }

    public String getEstadoEspecificoNuevo() {
        return estadoEspecificoNuevo;
    }

    public void setEstadoEspecificoNuevo(String estadoEspecificoNuevo) {
        this.estadoEspecificoNuevo = estadoEspecificoNuevo;
    }

    public String getNumeroCasoProveedor() {
        return numeroCasoProveedor;
    }

    public void setNumeroCasoProveedor(String numeroCasoProveedor) {
        this.numeroCasoProveedor = numeroCasoProveedor;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }
}
