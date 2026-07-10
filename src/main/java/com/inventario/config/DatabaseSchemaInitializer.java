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

        agregarColumnaSiFalta("usuarios", "activo", "alter table usuarios add column activo boolean not null default true");
        jdbcTemplate.execute("update usuarios set rol = 'SUPER_ADMIN' where upper(rol) in ('SUPERUSER', 'SUPERUSUARIO', 'ROLE_SUPERUSER')");

        jdbcTemplate.execute(
                """
                create table if not exists garantias (
                    id bigint not null auto_increment,
                    equipo_id bigint null,
                    numero_ticket varchar(5) null,
                    estado_general varchar(20) null,
                    estado_especifico varchar(80) null,
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
                    observaciones text null,
                    usuario_creacion varchar(255) null,
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

        agregarColumnaSiFalta("garantias", "numero_ticket", "alter table garantias add column numero_ticket varchar(5) null after equipo_id");
        agregarColumnaSiFalta("garantias", "estado_general", "alter table garantias add column estado_general varchar(20) null after numero_ticket");
        agregarColumnaSiFalta("garantias", "estado_especifico", "alter table garantias add column estado_especifico varchar(80) null after estado_general");
        agregarColumnaSiFalta("garantias", "observaciones", "alter table garantias add column observaciones text null after motivo_no_aplica_garantia");
        agregarColumnaSiFalta("garantias", "usuario_creacion", "alter table garantias add column usuario_creacion varchar(255) null after observaciones");

        jdbcTemplate.execute(
                """
                update garantias
                set estado_general = case
                    when estado in ('Reparado', 'No aplico garantia', 'No aplicó garantía', 'Cambio por equipo nuevo', 'Nota credito', 'Nota crédito')
                        then 'Cerrado'
                    else 'Abierto'
                end
                where estado_general is null
                """);

        jdbcTemplate.execute(
                """
                update garantias
                set estado_especifico = coalesce(estado_especifico, estado, 'En tramite')
                where estado_especifico is null
                """);

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

    private void agregarColumnaSiFalta(String tabla, String columna, String sql) {
        Integer existe = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                  and column_name = ?
                """,
                Integer.class,
                tabla,
                columna);

        if (existe == null || existe == 0) {
            jdbcTemplate.execute(sql);
        }
    }
}
