package com.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProrrogaSerialRequest(@NotNull LocalDate fechaLimite, @NotBlank String motivo) { }
