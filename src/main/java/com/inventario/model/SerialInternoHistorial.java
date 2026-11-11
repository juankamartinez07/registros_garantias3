package com.inventario.model;

import com.inventario.config.TiempoColombiaConfig;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "serial_interno_historial")
public class SerialInternoHistorial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "serial_interno_id", nullable = false)
    private SerialInterno serialInterno;
    @Enumerated(EnumType.STRING) @Column(name = "tipo_evento", nullable = false, length = 40)
    private EventoSerialInterno tipoEvento;
    @Column(nullable = false)
    private String usuario;
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    @Column(columnDefinition = "TEXT")
    private String observacion;
    @Column(name = "valores_anteriores", columnDefinition = "TEXT")
    private String valoresAnteriores;
    @Column(name = "valores_nuevos", columnDefinition = "TEXT")
    private String valoresNuevos;

    @PrePersist public void inicializarFecha() { if (fechaHora == null) fechaHora = TiempoColombiaConfig.ahora(); }
    public Long getId() { return id; }
    public SerialInterno getSerialInterno() { return serialInterno; }
    public void setSerialInterno(SerialInterno valor) { serialInterno = valor; }
    public EventoSerialInterno getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(EventoSerialInterno valor) { tipoEvento = valor; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String valor) { usuario = valor; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime valor) { fechaHora = valor; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String valor) { observacion = valor; }
    public String getValoresAnteriores() { return valoresAnteriores; }
    public void setValoresAnteriores(String valor) { valoresAnteriores = valor; }
    public String getValoresNuevos() { return valoresNuevos; }
    public void setValoresNuevos(String valor) { valoresNuevos = valor; }
}
