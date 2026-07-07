package com.inventario.controller;

import com.inventario.dto.GarantiaDTO;
import com.inventario.model.Garantia;
import com.inventario.service.GarantiaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
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

@Controller
@RequestMapping("/garantias")
public class GarantiaController {

    private final GarantiaService garantiaService;

    public GarantiaController(GarantiaService garantiaService) {
        this.garantiaService = garantiaService;
    }

    @GetMapping
    public String vista() {
        return "garantias";
    }

    @GetMapping("/api")
    @ResponseBody
    public Page<Garantia> listar(
            @RequestParam(required = false) String serial,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 10), 50),
                Sort.by(Sort.Direction.DESC, "fechaActualizacion"));

        return garantiaService.listar(serial, estado, pageable);
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

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String manejarError(RuntimeException exception) {
        return exception.getMessage();
    }
}
