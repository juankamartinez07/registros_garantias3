package com.inventario.controller;

import com.inventario.dto.ServicioTecnicoDTO;
import com.inventario.model.ServicioTecnico;
import com.inventario.service.ServicioTecnicoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/servicio-tecnico")
public class ServicioTecnicoController {

    private static final DateTimeFormatter FORMATO_FECHA_COMPROBANTE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ServicioTecnicoService servicioTecnicoService;

    public ServicioTecnicoController(ServicioTecnicoService servicioTecnicoService) {
        this.servicioTecnicoService = servicioTecnicoService;
    }

    @GetMapping
    public String vista() {
        return "servicio-tecnico";
    }

    @GetMapping("/{id}/comprobante")
    public String comprobante(@PathVariable Long id, Model model) {
        ServicioTecnico servicio = servicioTecnicoService.obtener(id);

        model.addAttribute("fechaIngreso", fechaComprobante(servicio.getFechaIngreso()));
        model.addAttribute("ticket", valorComprobante(servicio.getTicket()));
        model.addAttribute("cliente", valorComprobante(servicio.getCliente()));
        model.addAttribute("telefono", valorComprobante(servicio.getTelefono()));
        model.addAttribute("serial", valorComprobante(servicio.getSerial()));
        model.addAttribute("productoReferencia", valorComprobante(servicio.getProductoReferencia()));
        model.addAttribute("motivoRevision", valorComprobante(servicio.getMotivoRevision()));
        model.addAttribute("estadoFisico", valorComprobante(servicio.getEstadoFisico()));
        model.addAttribute("observaciones", valorComprobante(servicio.getObservaciones()));

        return "servicio-tecnico-comprobante";
    }

    @GetMapping("/api")
    @ResponseBody
    public Page<ServicioTecnico> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estadoServicio,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 10), 50),
                Sort.by(Sort.Direction.DESC, "fechaActualizacion"));

        return servicioTecnicoService.listar(busqueda, estadoServicio, pageable);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ServicioTecnico obtener(@PathVariable Long id) {
        return servicioTecnicoService.obtener(id);
    }

    @PostMapping("/api")
    @ResponseBody
    public ServicioTecnico crear(@RequestBody ServicioTecnicoDTO dto) {
        return servicioTecnicoService.crear(dto);
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ServicioTecnico actualizar(
            @PathVariable Long id,
            @RequestBody ServicioTecnicoDTO dto) {
        return servicioTecnicoService.actualizar(id, dto);
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public void eliminar(@PathVariable Long id) {
        servicioTecnicoService.eliminar(id);
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

    private String valorComprobante(String valor) {
        if (valor == null || valor.isBlank()) {
            return "No registrado";
        }
        return valor.trim();
    }
}
