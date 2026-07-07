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
                    numero_ticket varchar(32) null,
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
                    unique index uk_garantias_numero_ticket (numero_ticket),
                    index idx_garantias_serial (serial),
                    index idx_garantias_estado (estado),
                    constraint fk_garantias_equipo
                        foreign key (equipo_id)
                        references equipos (id_equipo)
                        on delete set null
                )
                """);

        Integer existeNumeroTicket = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.columns
                where table_schema = database()
                  and table_name = 'garantias'
                  and column_name = 'numero_ticket'
                """,
                Integer.class);

        if (existeNumeroTicket == null || existeNumeroTicket == 0) {
            jdbcTemplate.execute("alter table garantias add column numero_ticket varchar(32) null after equipo_id");
        }

        Integer existeIndiceTicket = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.statistics
                where table_schema = database()
                  and table_name = 'garantias'
                  and index_name = 'uk_garantias_numero_ticket'
                """,
                Integer.class);

        if (existeIndiceTicket == null || existeIndiceTicket == 0) {
            jdbcTemplate.execute("create unique index uk_garantias_numero_ticket on garantias (numero_ticket)");
        }
    }
}
