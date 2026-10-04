package com.inventario.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utilidades para guardar enlaces de evidencia sin almacenar archivos en el sistema. */
public final class EvidenciaUrlUtil {

    private static final int LONGITUD_MAXIMA = 1000;
    private static final Pattern ARCHIVO_DRIVE_EN_RUTA = Pattern.compile("/file/d/([^/?#]+)");
    private static final Pattern ARCHIVO_DRIVE_EN_QUERY = Pattern.compile("(?:[?&])id=([^&#]+)");

    private EvidenciaUrlUtil() {
    }

    public static String normalizarYValidar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String enlace = valor.trim();
        if (enlace.length() > LONGITUD_MAXIMA) {
            throw new IllegalArgumentException("El enlace de evidencia no puede superar los 1000 caracteres.");
        }
        try {
            URI uri = new URI(enlace);
            String esquema = uri.getScheme();
            if (esquema == null || !("http".equalsIgnoreCase(esquema) || "https".equalsIgnoreCase(esquema))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException("El enlace de evidencia debe ser una URL valida que use http o https.");
            }
            return enlace;
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("El enlace de evidencia debe ser una URL valida que use http o https.");
        }
    }

    public static boolean esCarpetaDrive(String enlace) {
        URI uri = uriValida(enlace);
        return uri != null
                && esHostDrive(uri)
                && uri.getPath() != null
                && uri.getPath().contains("/drive/folders/");
    }

    public static Optional<String> extraerIdArchivoDrive(String enlace) {
        if (esCarpetaDrive(enlace)) {
            return Optional.empty();
        }

        URI uri = uriValida(enlace);
        if (uri == null || !esHostDrive(uri)) {
            return Optional.empty();
        }

        String ruta = uri.getPath() == null ? "" : uri.getPath();
        Matcher coincidenciaRuta = ARCHIVO_DRIVE_EN_RUTA.matcher(ruta);
        if (coincidenciaRuta.find()) {
            return Optional.of(coincidenciaRuta.group(1));
        }

        Matcher coincidenciaQuery = ARCHIVO_DRIVE_EN_QUERY.matcher(uri.getQuery() == null ? "" : "?" + uri.getQuery());
        return coincidenciaQuery.find() ? Optional.of(coincidenciaQuery.group(1)) : Optional.empty();
    }

    public static Optional<String> construirUrlMiniatura(String enlace) {
        return extraerIdArchivoDrive(enlace)
                .map(id -> "https://drive.google.com/thumbnail?id="
                        + URLEncoder.encode(id, StandardCharsets.UTF_8) + "&sz=w300");
    }

    private static URI uriValida(String enlace) {
        try {
            String normalizada = normalizarYValidar(enlace);
            return normalizada == null ? null : new URI(normalizada);
        } catch (IllegalArgumentException | URISyntaxException exception) {
            return null;
        }
    }

    private static boolean esHostDrive(URI uri) {
        return "drive.google.com".equals(uri.getHost().toLowerCase(Locale.ROOT));
    }
}
