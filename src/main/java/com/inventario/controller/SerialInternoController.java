package com.inventario.controller;

import com.inventario.dto.GenerarLoteSerialesRequest;
import com.inventario.dto.LoteSerialInternoRespuesta;
import com.inventario.service.LoteSerialInternoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seriales-internos")
public class SerialInternoController {
    private final LoteSerialInternoService servicio;

    public SerialInternoController(LoteSerialInternoService servicio) { this.servicio = servicio; }

    @PostMapping("/lotes")
    @ResponseStatus(HttpStatus.CREATED)
    public LoteSerialInternoRespuesta generar(@Valid @RequestBody GenerarLoteSerialesRequest solicitud) {
        return servicio.generar(solicitud);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String solicitudInvalida(IllegalArgumentException error) { return error.getMessage(); }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String conflicto(IllegalStateException error) { return error.getMessage(); }
}
