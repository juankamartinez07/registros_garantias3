package com.inventario.controller;

import com.inventario.dto.DashboardGarantias;
import com.inventario.dto.GarantiaActualizacionDTO;
import com.inventario.dto.GarantiaDTO;
import com.inventario.dto.GarantiaHistorialDTO;
import com.inventario.model.Garantia;
import com.inventario.service.ComprobantePdfService;
import com.inventario.service.GarantiaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/garantias")
public class GarantiaController {

    private static final DateTimeFormatter FORMATO_FECHA_COMPROBANTE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final GarantiaService garantiaService;
    private final ComprobantePdfService comprobantePdfService;

    public GarantiaController(
            GarantiaService garantiaService,
            ComprobantePdfService comprobantePdfService) {
        this.garantiaService = garantiaService;
        this.comprobantePdfService = comprobantePdfService;
    }

    @GetMapping
    public String vista() {
        return "garantias";
    }

    @GetMapping("/{id}/comprobante")
    public String comprobante(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pos") String formato,
            Model model) {

        Garantia garantia = garantiaService.obtener(id);

        model.addAttribute("formato", formatoComprobante(formato));
        model.addAttribute("id", garantia.getId());
        model.addAttribute("sedeComprobante", valorComprobante(garantiaService.sedeComprobante(garantia)));
        model.addAttribute("telefonoEncabezado", "318 0974067");
        model.addAttribute("fechaIngresoEquipo", fechaComprobante(garantia.getFechaIngresoGarantia()));
        model.addAttribute("ticket", valorComprobante(garantia.getNumeroTicket()));
        model.addAttribute("serial", valorComprobante(garantia.getSerial()));
        model.addAttribute("productoReferencia", valorComprobante(garantia.getReferenciaProducto()));
        model.addAttribute("motivoGarantia", valorComprobante(garantia.getMotivosGarantia()));
        model.addAttribute("observaciones", valorComprobante(garantia.getObservaciones()));
        model.addAttribute("estadoActual", valorComprobante(
                garantia.getEstadoEspecifico() == null ? garantia.getEstado() : garantia.getEstadoEspecifico()));

        return "garantia-comprobante";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        Garantia garantia = garantiaService.obtener(id);
        byte[] pdf = comprobantePdfService.garantia(
                garantia,
                garantiaService.sedeComprobante(garantia));
        return respuestaPdf(pdf, comprobantePdfService.nombreArchivoGarantia(garantia));
    }

    @GetMapping("/api")
    @ResponseBody
    public Page<Garantia> listar(
            @RequestParam(required = false) String serial,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String estadoGeneral,
            @RequestParam(required = false) String estadoEspecifico,
            @RequestParam(required = false) String filtro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 10), 50),
                Sort.by(Sort.Direction.DESC, "fechaActualizacion"));

        return garantiaService.listar(serial, estado, estadoGeneral, estadoEspecifico, filtro, pageable);
    }

    @GetMapping("/api/dashboard")
    @ResponseBody
    public DashboardGarantias dashboard() {
        return garantiaService.dashboard();
    }

    @GetMapping("/api/preparar")
    @ResponseBody
    public GarantiaDTO preparar(@RequestParam String serial) {
        return garantiaService.preparar(serial);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Garantia obtener(@PathVariable Long id) {
        return garantiaService.obtener(id);
    }

    @GetMapping("/api/{id}/historial")
    @ResponseBody
    public List<GarantiaHistorialDTO> historial(@PathVariable Long id) {
        return garantiaService.historial(id);
    }

    @PostMapping("/api")
    @ResponseBody
    public Garantia crear(@RequestBody GarantiaDTO dto) {
        return garantiaService.crear(dto);
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public Garantia actualizar(
            @PathVariable Long id,
            @RequestBody GarantiaDTO dto) {
        return garantiaService.actualizar(id, dto);
    }

    @PostMapping("/api/{id}/actualizaciones")
    @ResponseBody
    public Garantia actualizarProceso(
            @PathVariable Long id,
            @RequestBody GarantiaActualizacionDTO dto) {
        return garantiaService.actualizarProceso(id, dto);
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public void eliminar(@PathVariable Long id) {
        garantiaService.eliminar(id);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String manejarError(RuntimeException exception) {
        return exception.getMessage();
    }

    private String fechaComprobante(LocalDate fecha) {
        return fecha == null ? "No registrado" : fecha.format(FORMATO_FECHA_COMPROBANTE);
    }

    private String formatoComprobante(String formato) {
        return "a4".equalsIgnoreCase(formato) ? "a4" : "pos";
    }

    private String valorComprobante(String valor) {
        if (valor == null || valor.isBlank()) {
            return "No registrado";
        }
        return valor.trim();
    }

    private ResponseEntity<byte[]> respuestaPdf(byte[] pdf, String nombreArchivo) {
        ContentDisposition disposicion = ContentDisposition.attachment()
                .filename(nombreArchivo, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .body(pdf);
    }
}
