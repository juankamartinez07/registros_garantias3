CREATE TABLE IF NOT EXISTS garantia_historial (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    garantia_id BIGINT NOT NULL,
    fecha_hora DATETIME NULL,
    usuario VARCHAR(255) NULL,
    estado_general_anterior VARCHAR(20) NULL,
    estado_general_nuevo VARCHAR(20) NULL,
    estado_especifico_anterior VARCHAR(80) NULL,
    estado_especifico_nuevo VARCHAR(80) NULL,
    numero_caso_proveedor VARCHAR(255) NULL,
    observacion TEXT NULL,
    tipo_evento VARCHAR(40) NULL,
    INDEX idx_garantia_historial_garantia (garantia_id),
    CONSTRAINT fk_garantia_historial_garantia
        FOREIGN KEY (garantia_id)
        REFERENCES garantias (id)
        ON DELETE CASCADE
);

UPDATE garantias
SET estado_especifico = 'Pendiente de gestion'
WHERE estado_especifico IN ('En tramite', 'EN_TRAMITE');

UPDATE garantias
SET estado = 'Pendiente de gestion'
WHERE estado IN ('En tramite', 'EN_TRAMITE');
