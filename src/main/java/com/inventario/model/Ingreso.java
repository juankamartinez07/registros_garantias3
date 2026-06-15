package com.inventario.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ingresos")
public class Ingreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_ingreso;

    private String factura;

    private LocalDate fecha_ingreso;

    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    public Long getId_ingreso() { return id_ingreso; }
    public void setId_ingreso(Long id_ingreso) { this.id_ingreso = id_ingreso; }

    public String getFactura() { return factura; }
    public void setFactura(String factura) { this.factura = factura; }

    public LocalDate getFecha_ingreso() { return fecha_ingreso; }
    public void setFecha_ingreso(LocalDate fecha_ingreso) { this.fecha_ingreso = fecha_ingreso; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
}