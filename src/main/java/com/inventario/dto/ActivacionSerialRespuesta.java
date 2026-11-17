package com.inventario.dto;

import com.inventario.model.EstadoSerialInterno;
import java.time.LocalDateTime;

public record ActivacionSerialRespuesta(Long id, String serial, EstadoSerialInterno estadoAnterior,
        EstadoSerialInterno estadoActual, LocalDateTime fechaActivacion, String mensaje) { }
