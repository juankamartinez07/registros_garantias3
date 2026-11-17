package com.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AnulacionSerialRequest(@NotEmpty List<Long> ids, @NotBlank String motivo, @NotBlank String observacion) { }
