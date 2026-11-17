package com.inventario.controller;

import com.inventario.dto.GenerarLoteSerialesRequest;
import com.inventario.dto.LoteSerialInternoRespuesta;
import com.inventario.service.LoteSerialInternoService;
import com.inventario.service.ActivacionSerialInternoService;
import com.inventario.dto.ActivacionSerialRequest;
import com.inventario.dto.ActivacionSerialRespuesta;
import com.inventario.dto.AnulacionSerialRequest;
import com.inventario.dto.ProrrogaSerialRequest;
import com.inventario.dto.EdicionSerialInternoRequest;
import com.inventario.service.AdministracionSerialInternoService;
import com.inventario.service.ConsultaSerialInternoService;
import com.inventario.dto.SerialInternoConsultaDTO;
import com.inventario.model.EstadoSerialInterno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final ConsultaSerialInternoService consulta;

    public SerialInternoController(LoteSerialInternoService servicio, ActivacionSerialInternoService activacion,
            AdministracionSerialInternoService administracion, ConsultaSerialInternoService consulta) {
        this.servicio = servicio;
        this.activacion = activacion;
        this.administracion = administracion;
        this.consulta = consulta;
    }

    @GetMapping
    public Page<SerialInternoConsultaDTO> consultar(@RequestParam(required = false) EstadoSerialInterno estado,
            @RequestParam(required = false) String lote, @RequestParam(required = false) String referencia,
            @RequestParam(defaultValue = "false") boolean vencidos, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return consulta.consultar(estado, lote, referencia, vencidos,
                PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by("id").descending()));
    }

    @GetMapping("/contadores")
    public ConsultaSerialInternoService.Contadores contadores() {
        return consulta.contadores();
    }

    @GetMapping("/lotes")
    public Page<ConsultaSerialInternoService.LoteDTO> lotes(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return consulta.lotes(PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by("id").descending()));
    }

    @GetMapping("/{id}/historial")
    public Page<ConsultaSerialInternoService.HistorialDTO> historial(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        return consulta.historial(id, PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by("fechaHora").descending()));
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

    @PostMapping("/{id}/editar")
    public Map<String, Integer> editar(@PathVariable Long id, @RequestBody CamposEdicion solicitud) {
        return Map.of("modificados", administracion.editar(new EdicionSerialInternoRequest(List.of(id),
                solicitud.producto(), solicitud.tipo(), solicitud.proveedor(), solicitud.factura(), solicitud.fecha())));
    }

    @PostMapping("/editar-seleccion")
    public Map<String, Integer> editarSeleccion(@RequestBody EdicionSerialInternoRequest solicitud) {
        return Map.of("modificados", administracion.editar(solicitud));
    }

    public record CamposEdicion(String producto, String tipo, String proveedor, String factura, java.time.LocalDate fecha) { }

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
