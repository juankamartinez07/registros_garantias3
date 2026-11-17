package com.inventario.dto;

import java.time.LocalDate;
import java.util.List;

public record EdicionSerialInternoRequest(List<Long> ids, String producto, String tipo,
        String proveedor, String factura, LocalDate fecha) { }
