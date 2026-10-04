package com.inventario.dto;

public class GarantiaActualizacionDTO {

    private String estadoGeneral;
    private String estadoEspecifico;
    private String numeroCasoProveedor;
    private String observacion;
    private String motivoNoAplicaGarantia;
    private String enlaceEvidencia;

    public String getEstadoGeneral() {
        return estadoGeneral;
    }

    public void setEstadoGeneral(String estadoGeneral) {
        this.estadoGeneral = estadoGeneral;
    }

    public String getEstadoEspecifico() {
        return estadoEspecifico;
    }

    public void setEstadoEspecifico(String estadoEspecifico) {
        this.estadoEspecifico = estadoEspecifico;
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

    public String getMotivoNoAplicaGarantia() {
        return motivoNoAplicaGarantia;
    }

    public void setMotivoNoAplicaGarantia(String motivoNoAplicaGarantia) {
        this.motivoNoAplicaGarantia = motivoNoAplicaGarantia;
    }

    public String getEnlaceEvidencia() {
        return enlaceEvidencia;
    }

    public void setEnlaceEvidencia(String enlaceEvidencia) {
        this.enlaceEvidencia = enlaceEvidencia;
    }
}
