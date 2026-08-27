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
        boolean tieneUrlSpring = System.getenv("SPRING_DATASOURCE_URL") != null;
        boolean tieneDatabaseUrl = System.getenv("DATABASE_URL") != null;
        boolean tieneVariablesDb = System.getenv("DB_HOST") != null
                && System.getenv("DB_PORT") != null
                && System.getenv("DB_NAME") != null;
        boolean tieneVariablesMysql = System.getenv("MYSQL_HOST") != null
                && System.getenv("MYSQL_PORT") != null
                && System.getenv("MYSQL_DATABASE") != null;

        if (!tieneUrlSpring && !tieneDatabaseUrl && !tieneVariablesDb && !tieneVariablesMysql) {
            return;
        }

        jdbcTemplate.execute(
                """
                create table if not exists configuracion_demo (
                    id bigint not null auto_increment,
                    demo_activa boolean not null default true,
                    fecha_inicio_demo date not null,
                    dias_demo int not null default 10,
                    fecha_creacion datetime null default current_timestamp,
                    fecha_actualizacion datetime null default current_timestamp on update current_timestamp,
                    primary key (id)
                )
                """);

        Integer configuracionesDemo = jdbcTemplate.queryForObject(
                "select count(*) from configuracion_demo",
                Integer.class);

        if (configuracionesDemo == null || configuracionesDemo == 0) {
            jdbcTemplate.execute(
                    """
                    insert into configuracion_demo (demo_activa, fecha_inicio_demo, dias_demo)
                    values (true, current_date, 10)
                    """);
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
        agregarColumnaSiFalta("usuarios", "demo_individual_activa", "alter table usuarios add column demo_individual_activa boolean null");
        agregarColumnaSiFalta("usuarios", "fecha_inicio_demo_individual", "alter table usuarios add column fecha_inicio_demo_individual date null");
        agregarColumnaSiFalta("usuarios", "dias_demo_individual", "alter table usuarios add column dias_demo_individual int null");
        jdbcTemplate.execute("update usuarios set rol = 'SUPER_ADMIN' where upper(rol) in ('SUPERUSER', 'SUPERUSUARIO', 'SUPERADMIN', 'ROLE_SUPERUSER', 'ROLE_SUPERADMIN')");

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

        jdbcTemplate.execute(
                """
                create table if not exists servicio_tecnico (
                    id bigint not null auto_increment,
                    ticket varchar(5) not null,
                    fecha_ingreso date not null,
                    sede varchar(255) null,
                    cliente varchar(255) not null,
                    telefono varchar(80) not null,
                    serial varchar(255) not null,
                    producto_referencia varchar(255) not null,
                    marca varchar(255) null,
                    motivo_revision text not null,
                    estado_fisico text not null,
                    observaciones text null,
                    usuario_recibe varchar(255) null,
                    estado_servicio varchar(80) not null,
                    fecha_creacion datetime null,
                    fecha_actualizacion datetime null,
                    primary key (id),
                    unique index uk_servicio_tecnico_ticket (ticket),
                    index idx_servicio_tecnico_serial (serial),
                    index idx_servicio_tecnico_estado (estado_servicio),
                    index idx_servicio_tecnico_sede (sede)
                )
                """);

        agregarColumnaSiFalta("servicio_tecnico", "ticket", "alter table servicio_tecnico add column ticket varchar(5) not null after id");
        agregarColumnaSiFalta("servicio_tecnico", "fecha_ingreso", "alter table servicio_tecnico add column fecha_ingreso date not null after ticket");
        agregarColumnaSiFalta("servicio_tecnico", "sede", "alter table servicio_tecnico add column sede varchar(255) null after fecha_ingreso");
        agregarColumnaSiFalta("servicio_tecnico", "cliente", "alter table servicio_tecnico add column cliente varchar(255) not null after sede");
        agregarColumnaSiFalta("servicio_tecnico", "telefono", "alter table servicio_tecnico add column telefono varchar(80) not null after cliente");
        agregarColumnaSiFalta("servicio_tecnico", "serial", "alter table servicio_tecnico add column serial varchar(255) not null after telefono");
        agregarColumnaSiFalta("servicio_tecnico", "producto_referencia", "alter table servicio_tecnico add column producto_referencia varchar(255) not null after serial");
        agregarColumnaSiFalta("servicio_tecnico", "marca", "alter table servicio_tecnico add column marca varchar(255) null after producto_referencia");
        agregarColumnaSiFalta("servicio_tecnico", "motivo_revision", "alter table servicio_tecnico add column motivo_revision text not null after marca");
        agregarColumnaSiFalta("servicio_tecnico", "estado_fisico", "alter table servicio_tecnico add column estado_fisico text not null after motivo_revision");
        agregarColumnaSiFalta("servicio_tecnico", "observaciones", "alter table servicio_tecnico add column observaciones text null after estado_fisico");
        agregarColumnaSiFalta("servicio_tecnico", "usuario_recibe", "alter table servicio_tecnico add column usuario_recibe varchar(255) null after observaciones");
        agregarColumnaSiFalta("servicio_tecnico", "estado_servicio", "alter table servicio_tecnico add column estado_servicio varchar(80) not null after usuario_recibe");
        agregarColumnaSiFalta("servicio_tecnico", "fecha_creacion", "alter table servicio_tecnico add column fecha_creacion datetime null after estado_servicio");
        agregarColumnaSiFalta("servicio_tecnico", "fecha_actualizacion", "alter table servicio_tecnico add column fecha_actualizacion datetime null after fecha_creacion");

        Integer existeIndiceServicioTicket = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.statistics
                where table_schema = database()
                  and table_name = 'servicio_tecnico'
                  and index_name = 'uk_servicio_tecnico_ticket'
                """,
                Integer.class);

        if (existeIndiceServicioTicket == null || existeIndiceServicioTicket == 0) {
            jdbcTemplate.execute("create unique index uk_servicio_tecnico_ticket on servicio_tecnico (ticket)");
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
