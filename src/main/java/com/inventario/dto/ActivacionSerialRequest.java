package com.inventario.dto;

import jakarta.validation.constraints.NotBlank;

public record ActivacionSerialRequest(@NotBlank String serial) { }
