package com.inventario.model;

import com.inventario.config.TiempoColombiaConfig;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "serial_interno")
public class SerialInterno {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false, unique = true)
    private Equipo equipo;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteSerialInterno lote;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32)
    private EstadoSerialInterno estado = EstadoSerialInterno.PENDIENTE_ACTIVACION;
    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;
    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;
    @Column(name = "fecha_activacion")
    private LocalDateTime fechaActivacion;
    @Column(name = "usuario_activacion")
    private String usuarioActivacion;
    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;
    @Column(name = "usuario_anulacion")
    private String usuarioAnulacion;
    @Column(name = "motivo_anulacion")
    private String motivoAnulacion;
    @Column(name = "observacion_anulacion", columnDefinition = "TEXT")
    private String observacionAnulacion;
    @Version @Column(nullable = false)
    private long version;

    @PrePersist public void inicializarFecha() { if (fechaGeneracion == null) fechaGeneracion = TiempoColombiaConfig.ahora(); }
    public Long getId() { return id; }
    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo valor) { equipo = valor; }
    public LoteSerialInterno getLote() { return lote; }
    public void setLote(LoteSerialInterno valor) { lote = valor; }
    public EstadoSerialInterno getEstado() { return estado; }
    public void setEstado(EstadoSerialInterno valor) { estado = valor; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime valor) { fechaGeneracion = valor; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate valor) { fechaLimite = valor; }
    public LocalDateTime getFechaActivacion() { return fechaActivacion; }
    public void setFechaActivacion(LocalDateTime valor) { fechaActivacion = valor; }
    public String getUsuarioActivacion() { return usuarioActivacion; }
    public void setUsuarioActivacion(String valor) { usuarioActivacion = valor; }
    public LocalDateTime getFechaAnulacion() { return fechaAnulacion; }
    public void setFechaAnulacion(LocalDateTime valor) { fechaAnulacion = valor; }
    public String getUsuarioAnulacion() { return usuarioAnulacion; }
    public void setUsuarioAnulacion(String valor) { usuarioAnulacion = valor; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String valor) { motivoAnulacion = valor; }
    public String getObservacionAnulacion() { return observacionAnulacion; }
    public void setObservacionAnulacion(String valor) { observacionAnulacion = valor; }
    public long getVersion() { return version; }
}
