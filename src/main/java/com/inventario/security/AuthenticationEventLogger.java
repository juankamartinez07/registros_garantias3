package com.inventario.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventLogger {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AuthenticationEventLogger.class);

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {

        logger.info(
                "Login exitoso para usuario '{}'",
                event.getAuthentication().getName());

    }

    @EventListener
    public void onBadCredentials(
            AuthenticationFailureBadCredentialsEvent event) {

        logger.warn(
                "Login fallido: credenciales invÃ¡lidas para usuario '{}'",
                event.getAuthentication().getName());

    }

}

