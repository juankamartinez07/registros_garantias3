package com.inventario.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record GenerarLoteSerialesRequest(
        @NotBlank String producto,
        @NotBlank String tipo,
        @NotBlank String proveedor,
        @NotBlank String factura,
        @NotNull LocalDate fecha,
        @Min(1) @Max(100) int cantidad,
        String observaciones) {
}
