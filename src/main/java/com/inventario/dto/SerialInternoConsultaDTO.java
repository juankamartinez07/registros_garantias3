package com.inventario.dto;

import com.inventario.model.EstadoSerialInterno;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SerialInternoConsultaDTO(Long id, String serial, String lote, String sede,
        String referencia, EstadoSerialInterno estado, LocalDateTime fechaGeneracion,
        LocalDate fechaLimite, boolean vencido) { }
