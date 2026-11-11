package com.inventario.dto;

import com.inventario.model.EstadoSerialInterno;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record LoteSerialInternoRespuesta(
        Long loteId,
        String codigo,
        LocalDateTime fechaGeneracion,
        LocalDate fechaLimite,
        int cantidad,
        List<String> seriales,
        EstadoSerialInterno estadoInicial) {
    public LoteSerialInternoRespuesta {
        seriales = List.copyOf(seriales);
    }
}
