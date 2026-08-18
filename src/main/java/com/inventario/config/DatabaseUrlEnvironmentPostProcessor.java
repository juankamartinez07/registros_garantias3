package com.inventario.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "databaseUrlDatasource";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");

        if (!StringUtils.hasText(databaseUrl) || StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_URL"))) {
            return;
        }

        DatasourceProperties datasourceProperties = parseDatabaseUrl(databaseUrl.trim());
        if (datasourceProperties == null) {
            return;
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("spring.datasource.url", datasourceProperties.jdbcUrl());

        if (!StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_USERNAME"))
                && StringUtils.hasText(datasourceProperties.username())) {
            properties.put("spring.datasource.username", datasourceProperties.username());
        }

        if (!StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_PASSWORD"))
                && datasourceProperties.password() != null) {
            properties.put("spring.datasource.password", datasourceProperties.password());
        }

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private DatasourceProperties parseDatabaseUrl(String databaseUrl) {
        int schemeSeparator = databaseUrl.indexOf("://");
        if (schemeSeparator < 0) {
            return null;
        }

        String scheme = databaseUrl.substring(0, schemeSeparator).toLowerCase(Locale.ROOT);
        String jdbcScheme = switch (scheme) {
            case "mariadb" -> "jdbc:mariadb://";
            case "mysql" -> "jdbc:mysql://";
            default -> null;
        };

        if (jdbcScheme == null) {
            return null;
        }

        String urlWithoutScheme = databaseUrl.substring(schemeSeparator + 3);
        int firstSlash = urlWithoutScheme.indexOf('/');
        String authority = firstSlash >= 0 ? urlWithoutScheme.substring(0, firstSlash) : urlWithoutScheme;
        String pathAndQuery = firstSlash >= 0 ? urlWithoutScheme.substring(firstSlash) : "";

        String username = null;
        String password = null;
        String hostAndPort = authority;

        int credentialsSeparator = authority.lastIndexOf('@');
        if (credentialsSeparator >= 0) {
            String userInfo = authority.substring(0, credentialsSeparator);
            hostAndPort = authority.substring(credentialsSeparator + 1);
            int passwordSeparator = userInfo.indexOf(':');

            if (passwordSeparator >= 0) {
                username = decode(userInfo.substring(0, passwordSeparator));
                password = decode(userInfo.substring(passwordSeparator + 1));
            } else {
                username = decode(userInfo);
            }
        }

        if (!StringUtils.hasText(hostAndPort) || !StringUtils.hasText(pathAndQuery)) {
            return null;
        }

        return new DatasourceProperties(jdbcScheme + hostAndPort + pathAndQuery, username, password);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private record DatasourceProperties(String jdbcUrl, String username, String password) {
    }
}
