package com.inventario.controller;

import com.inventario.service.DemoService;
import com.inventario.service.DemoService.DemoEstado;
import com.inventario.service.DemoService.DemoSolicitud;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/demo-expirada")
    public String demoExpirada() {
        return "demo-expirada";
    }

    @GetMapping("/configuracion/demo")
    @ResponseBody
    public DemoEstado estadoDemo() {
        return demoService.estadoActual();
    }

    @PutMapping("/configuracion/demo")
    @ResponseBody
    public DemoEstado guardar(@RequestBody DemoSolicitud solicitud) {
        return demoService.guardar(solicitud);
    }

    @PostMapping("/configuracion/demo/activar")
    @ResponseBody
    public DemoEstado activar() {
        return demoService.activar();
    }

    @PostMapping("/configuracion/demo/desactivar")
    @ResponseBody
    public DemoEstado desactivar() {
        return demoService.desactivar();
    }

    @PostMapping("/configuracion/demo/reiniciar")
    @ResponseBody
    public DemoEstado reiniciar() {
        return demoService.reiniciar();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String manejarError(RuntimeException ex) {
        return ex.getMessage();
    }
}
