package com.inventario.controller;

import com.inventario.service.DemoService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class DemoModelAdvice {

    private final DemoService demoService;

    public DemoModelAdvice(DemoService demoService) {
        this.demoService = demoService;
    }

    @ModelAttribute("demoEstado")
    public DemoService.DemoEstado demoEstado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return demoService.estadoActual();
    }
}
