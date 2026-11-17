package com.inventario.controller;

import com.inventario.dto.GenerarLoteSerialesRequest;
import com.inventario.dto.LoteSerialInternoRespuesta;
import com.inventario.service.LoteSerialInternoService;
import com.inventario.service.ActivacionSerialInternoService;
import com.inventario.dto.ActivacionSerialRequest;
import com.inventario.dto.ActivacionSerialRespuesta;
import com.inventario.dto.AnulacionSerialRequest;
import com.inventario.dto.ProrrogaSerialRequest;
import com.inventario.service.AdministracionSerialInternoService;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seriales-internos")
public class SerialInternoController {
    private final LoteSerialInternoService servicio;
    private final ActivacionSerialInternoService activacion;
    private final AdministracionSerialInternoService administracion;

    public SerialInternoController(LoteSerialInternoService servicio, ActivacionSerialInternoService activacion,
            AdministracionSerialInternoService administracion) {
        this.servicio = servicio;
        this.activacion = activacion;
        this.administracion = administracion;
    }

    @PostMapping("/{id}/anular")
    public Map<String, Integer> anular(@PathVariable Long id, @Valid @RequestBody AnulacionIndividual solicitud) {
        return Map.of("anulados", administracion.anular(new AnulacionSerialRequest(List.of(id), solicitud.motivo(), solicitud.observacion())));
    }

    @PostMapping("/anular-seleccion")
    public Map<String, Integer> anularSeleccion(@Valid @RequestBody AnulacionSerialRequest solicitud) {
        return Map.of("anulados", administracion.anular(solicitud));
    }

    @PostMapping("/lotes/{id}/prorrogar")
    public Map<String, Integer> prorrogar(@PathVariable Long id, @Valid @RequestBody ProrrogaSerialRequest solicitud) {
        return Map.of("prorrogados", administracion.prorrogar(id, solicitud));
    }

    public record AnulacionIndividual(@jakarta.validation.constraints.NotBlank String motivo,
            @jakarta.validation.constraints.NotBlank String observacion) { }

    @PostMapping("/activar")
    public ActivacionSerialRespuesta activar(@Valid @RequestBody ActivacionSerialRequest solicitud) {
        return activacion.activar(solicitud.serial());
    }

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
