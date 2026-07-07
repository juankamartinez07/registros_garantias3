package com.inventario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "garantias")
public class Garantia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    @Column(name = "numero_ticket", unique = true)
    private String numeroTicket;

    private String sede;

    @Column(name = "referencia_producto")
    private String referenciaProducto;

    private String serial;

    private String estado;

    private String proveedor;

    @Column(name = "factura_proveedor")
    private String facturaProveedor;

    @Column(name = "fecha_ingreso_garantia")
    private LocalDate fechaIngresoGarantia;

    @Column(name = "fecha_ingreso_serial")
    private LocalDate fechaIngresoSerial;

    @Column(name = "motivos_garantia", columnDefinition = "TEXT")
    private String motivosGarantia;

    @Column(name = "numero_caso_proveedor")
    private String numeroCasoProveedor;

    @Column(name = "motivo_no_aplica_garantia", columnDefinition = "TEXT")
    private String motivoNoAplicaGarantia;

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

    public void setId(Long id) {
        this.id = id;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public void setNumeroTicket(String numeroTicket) {
        this.numeroTicket = numeroTicket;
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
