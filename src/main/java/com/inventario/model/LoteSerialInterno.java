package com.inventario.model;

import com.inventario.config.TiempoColombiaConfig;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lote_serial_interno")
public class LoteSerialInterno {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 64)
    private String codigo;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;
    @Column(name = "sede_nombre", nullable = false)
    private String sedeNombre;
    @Column(name = "usuario_creador", nullable = false)
    private String usuarioCreador;
    @Column(nullable = false)
    private int cantidad;
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;

    @PrePersist public void inicializarFecha() { if (fechaCreacion == null) fechaCreacion = TiempoColombiaConfig.ahora(); }
    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String valor) { codigo = valor; }
    public Sede getSede() { return sede; }
    public void setSede(Sede valor) { sede = valor; }
    public String getSedeNombre() { return sedeNombre; }
    public void setSedeNombre(String valor) { sedeNombre = valor; }
    public String getUsuarioCreador() { return usuarioCreador; }
    public void setUsuarioCreador(String valor) { usuarioCreador = valor; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int valor) { cantidad = valor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime valor) { fechaCreacion = valor; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate valor) { fechaLimite = valor; }
}
