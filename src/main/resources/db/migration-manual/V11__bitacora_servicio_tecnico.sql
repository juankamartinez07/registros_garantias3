CREATE TABLE IF NOT EXISTS servicio_tecnico_historial (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    servicio_tecnico_id BIGINT NOT NULL,
    fecha_hora DATETIME NULL,
    usuario VARCHAR(255) NULL,
    estado_anterior VARCHAR(80) NULL,
    estado_nuevo VARCHAR(80) NULL,
    observacion TEXT NULL,
    tipo_evento VARCHAR(40) NULL,
    INDEX idx_servicio_tecnico_historial_servicio (servicio_tecnico_id),
    CONSTRAINT fk_servicio_tecnico_historial_servicio
        FOREIGN KEY (servicio_tecnico_id)
        REFERENCES servicio_tecnico (id)
        ON DELETE CASCADE
);
