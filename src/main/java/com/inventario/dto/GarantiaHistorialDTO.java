package com.inventario.dto;

import java.time.LocalDateTime;

public class GarantiaHistorialDTO {

    private Long id;
    private LocalDateTime fechaHora;
    private String usuario;
    private String estadoGeneralAnterior;
    private String estadoGeneralNuevo;
    private String estadoEspecificoAnterior;
    private String estadoEspecificoNuevo;
    private String numeroCasoProveedor;
    private String observacion;
    private String tipoEvento;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
