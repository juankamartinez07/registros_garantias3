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

        jdbcTemplate.execute(
                """
                create table if not exists garantias (
                    id bigint not null auto_increment,
                    equipo_id bigint null,
                    sede varchar(255) null,
                    referencia_producto varchar(255) null,
                    serial varchar(255) not null,
                    estado varchar(80) not null,
                    proveedor varchar(255) null,
                    factura_proveedor varchar(255) null,
                    fecha_ingreso_garantia date null,
                    fecha_ingreso_serial date null,
                    motivos_garantia text null,
                    numero_caso_proveedor varchar(255) null,
                    motivo_no_aplica_garantia text null,
                    fecha_creacion datetime null,
                    fecha_actualizacion datetime null,
                    primary key (id),
                    index idx_garantias_serial (serial),
                    index idx_garantias_estado (estado),
                    constraint fk_garantias_equipo
                        foreign key (equipo_id)
                        references equipos (id_equipo)
                        on delete set null
                )
                """);
    }
}
