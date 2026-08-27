CREATE TABLE IF NOT EXISTS garantias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    equipo_id BIGINT NULL,
    numero_ticket VARCHAR(5) NULL,
    estado_general VARCHAR(20) NULL,
    estado_especifico VARCHAR(80) NULL,
    sede VARCHAR(255) NULL,
    referencia_producto VARCHAR(255) NULL,
    serial VARCHAR(255) NOT NULL,
    estado VARCHAR(80) NOT NULL,
    proveedor VARCHAR(255) NULL,
    factura_proveedor VARCHAR(255) NULL,
    fecha_ingreso_garantia DATE NULL,
    fecha_ingreso_serial DATE NULL,
    motivos_garantia TEXT NULL,
    numero_caso_proveedor VARCHAR(255) NULL,
    motivo_no_aplica_garantia TEXT NULL,
    observaciones TEXT NULL,
    usuario_creacion VARCHAR(255) NULL,
    fecha_creacion DATETIME NULL,
    fecha_actualizacion DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_garantias_numero_ticket (numero_ticket),
    INDEX idx_garantias_serial (serial),
    INDEX idx_garantias_estado (estado),
    CONSTRAINT fk_garantias_equipo
        FOREIGN KEY (equipo_id)
        REFERENCES equipos (id_equipo)
        ON DELETE SET NULL
);
