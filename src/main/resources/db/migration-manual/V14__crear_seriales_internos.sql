-- Ejecutar manualmente solo en la base autorizada, despues de verificar su esquema.
CREATE TABLE IF NOT EXISTS lote_serial_interno (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(64) NOT NULL,
    sede_id BIGINT NOT NULL,
    sede_nombre VARCHAR(255) NOT NULL,
    usuario_creador VARCHAR(255) NOT NULL,
    cantidad INT NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_limite DATE NOT NULL,
    CONSTRAINT uk_lote_serial_codigo UNIQUE (codigo),
    CONSTRAINT fk_lote_serial_sede FOREIGN KEY (sede_id) REFERENCES sedes (id)
);

CREATE TABLE IF NOT EXISTS serial_interno (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    equipo_id BIGINT NOT NULL,
    lote_id BIGINT NOT NULL,
    estado VARCHAR(32) NOT NULL,
    fecha_generacion DATETIME NOT NULL,
    fecha_limite DATE NOT NULL,
    fecha_activacion DATETIME NULL,
    usuario_activacion VARCHAR(255) NULL,
    fecha_anulacion DATETIME NULL,
    usuario_anulacion VARCHAR(255) NULL,
    motivo_anulacion VARCHAR(255) NULL,
    observacion_anulacion TEXT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_serial_interno_equipo UNIQUE (equipo_id),
    CONSTRAINT fk_serial_interno_equipo FOREIGN KEY (equipo_id) REFERENCES equipos (id_equipo),
    CONSTRAINT fk_serial_interno_lote FOREIGN KEY (lote_id) REFERENCES lote_serial_interno (id),
    INDEX idx_serial_interno_lote (lote_id),
    INDEX idx_serial_interno_estado_limite (estado, fecha_limite)
);

CREATE TABLE IF NOT EXISTS serial_interno_historial (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    serial_interno_id BIGINT NOT NULL,
    tipo_evento VARCHAR(40) NOT NULL,
    usuario VARCHAR(255) NOT NULL,
    fecha_hora DATETIME NOT NULL,
    observacion TEXT NULL,
    valores_anteriores TEXT NULL,
    valores_nuevos TEXT NULL,
    CONSTRAINT fk_historial_serial_interno FOREIGN KEY (serial_interno_id) REFERENCES serial_interno (id),
    INDEX idx_historial_serial_fecha (serial_interno_id, fecha_hora)
);
