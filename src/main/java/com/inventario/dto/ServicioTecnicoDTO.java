package com.inventario.dto;

import java.time.LocalDate;

public class ServicioTecnicoDTO {

    private LocalDate fechaIngreso;
    private String cliente;
    private String telefono;
    private String serial;
    private String productoReferencia;
    private String marca;
    private String motivoRevision;
    private String estadoFisico;
    private String observaciones;
    private String estadoServicio;

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getProductoReferencia() {
        return productoReferencia;
    }

    public void setProductoReferencia(String productoReferencia) {
        this.productoReferencia = productoReferencia;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getMotivoRevision() {
        return motivoRevision;
    }

    public void setMotivoRevision(String motivoRevision) {
        this.motivoRevision = motivoRevision;
    }

    public String getEstadoFisico() {
        return estadoFisico;
    }

    public void setEstadoFisico(String estadoFisico) {
        this.estadoFisico = estadoFisico;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEstadoServicio() {
        return estadoServicio;
    }

    public void setEstadoServicio(String estadoServicio) {
        this.estadoServicio = estadoServicio;
    }
}
