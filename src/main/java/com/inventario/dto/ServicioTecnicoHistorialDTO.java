package com.inventario.dto;

import java.time.LocalDateTime;

public class ServicioTecnicoHistorialDTO {

    private Long id;
    private LocalDateTime fechaHora;
    private String usuario;
    private String estadoAnterior;
    private String estadoNuevo;
    private String observacion;
    private String tipoEvento;
    private String enlaceEvidencia;

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

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(String estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public String getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(String estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
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

    public String getEnlaceEvidencia() {
        return enlaceEvidencia;
    }

    public void setEnlaceEvidencia(String enlaceEvidencia) {
        this.enlaceEvidencia = enlaceEvidencia;
    }
}
