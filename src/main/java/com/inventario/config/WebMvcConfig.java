package com.inventario.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final DemoInterceptor demoInterceptor;

    public WebMvcConfig(DemoInterceptor demoInterceptor) {
        this.demoInterceptor = demoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(demoInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/logout",
                        "/demo-expirada",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/static/**",
                        "/webjars/**",
                        "/error",
                        "/favicon.ico");
    }
}
