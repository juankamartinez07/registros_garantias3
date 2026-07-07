package com.inventario.dto;

import java.time.LocalDate;

public class GarantiaDTO {

    private Long id;
    private Long equipoId;
    private String sede;
    private String referenciaProducto;
    private String serial;
    private String estado;
    private String proveedor;
    private String facturaProveedor;
    private LocalDate fechaIngresoGarantia;
    private LocalDate fechaIngresoSerial;
    private String motivosGarantia;
    private String numeroCasoProveedor;
    private String motivoNoAplicaGarantia;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEquipoId() {
        return equipoId;
    }

    public void setEquipoId(Long equipoId) {
        this.equipoId = equipoId;
    }

    public String getSede() {
        return sede;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public String getReferenciaProducto() {
        return referenciaProducto;
    }

    public void setReferenciaProducto(String referenciaProducto) {
        this.referenciaProducto = referenciaProducto;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getFacturaProveedor() {
        return facturaProveedor;
    }

    public void setFacturaProveedor(String facturaProveedor) {
        this.facturaProveedor = facturaProveedor;
    }

    public LocalDate getFechaIngresoGarantia() {
        return fechaIngresoGarantia;
    }

    public void setFechaIngresoGarantia(LocalDate fechaIngresoGarantia) {
        this.fechaIngresoGarantia = fechaIngresoGarantia;
    }

    public LocalDate getFechaIngresoSerial() {
        return fechaIngresoSerial;
    }

    public void setFechaIngresoSerial(LocalDate fechaIngresoSerial) {
        this.fechaIngresoSerial = fechaIngresoSerial;
    }

    public String getMotivosGarantia() {
        return motivosGarantia;
    }

    public void setMotivosGarantia(String motivosGarantia) {
        this.motivosGarantia = motivosGarantia;
    }

    public String getNumeroCasoProveedor() {
        return numeroCasoProveedor;
    }

    public void setNumeroCasoProveedor(String numeroCasoProveedor) {
        this.numeroCasoProveedor = numeroCasoProveedor;
    }

    public String getMotivoNoAplicaGarantia() {
        return motivoNoAplicaGarantia;
    }

    public void setMotivoNoAplicaGarantia(String motivoNoAplicaGarantia) {
        this.motivoNoAplicaGarantia = motivoNoAplicaGarantia;
    }
}
