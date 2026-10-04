package com.inventario.dto;

public class ServicioTecnicoActualizacionDTO {

    private String estadoServicio;
    private String observacion;
    private String enlaceEvidencia;

    public String getEstadoServicio() {
        return estadoServicio;
    }

    public void setEstadoServicio(String estadoServicio) {
        this.estadoServicio = estadoServicio;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getEnlaceEvidencia() {
        return enlaceEvidencia;
    }

    public void setEnlaceEvidencia(String enlaceEvidencia) {
        this.enlaceEvidencia = enlaceEvidencia;
    }
}
