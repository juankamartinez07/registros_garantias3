ALTER TABLE garantias
    ADD COLUMN estado_general VARCHAR(20) NULL AFTER numero_ticket,
    ADD COLUMN estado_especifico VARCHAR(80) NULL AFTER estado_general,
    ADD COLUMN observaciones TEXT NULL AFTER motivo_no_aplica_garantia,
    ADD COLUMN usuario_creacion VARCHAR(255) NULL AFTER observaciones;

UPDATE garantias
SET estado_general = CASE
    WHEN estado IN ('Reparado', 'No aplico garantia', 'No aplicó garantía', 'Cambio por equipo nuevo', 'Nota credito', 'Nota crédito')
        THEN 'Cerrado'
    ELSE 'Abierto'
END
WHERE estado_general IS NULL;

UPDATE garantias
SET estado_especifico = COALESCE(estado_especifico, estado, 'En tramite')
WHERE estado_especifico IS NULL;
