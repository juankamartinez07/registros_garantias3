package com.inventario.config;

import com.inventario.service.DemoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class DemoInterceptor implements HandlerInterceptor {

    private final DemoService demoService;

    public DemoInterceptor(DemoService demoService) {
        this.demoService = demoService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return true;
        }

        if (demoService.debeBloquearUsuarioActual()) {
            response.sendRedirect("/demo-expirada");
            return false;
        }
        return true;
    }
}
