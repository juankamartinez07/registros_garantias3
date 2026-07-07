package com.inventario.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (System.getenv("DB_HOST") == null || System.getenv("DB_PORT") == null || System.getenv("DB_NAME") == null) {
            return;
        }

        Integer existeObservaciones = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.columns
                where table_schema = database()
                  and table_name = 'equipos'
                  and column_name = 'observaciones'
                """,
                Integer.class);

        if (existeObservaciones == null || existeObservaciones == 0) {
            jdbcTemplate.execute("alter table equipos add column observaciones text null");
        }
    }
}
