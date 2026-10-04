ALTER TABLE garantias
    ADD COLUMN IF NOT EXISTS enlace_evidencias VARCHAR(1000) NULL;

ALTER TABLE garantia_historial
    ADD COLUMN IF NOT EXISTS enlace_evidencia VARCHAR(1000) NULL;

ALTER TABLE servicio_tecnico
    ADD COLUMN IF NOT EXISTS enlace_evidencias VARCHAR(1000) NULL;

ALTER TABLE servicio_tecnico_historial
    ADD COLUMN IF NOT EXISTS enlace_evidencia VARCHAR(1000) NULL;
