ALTER TABLE garantias
    ADD COLUMN IF NOT EXISTS sede_correspondiente VARCHAR(255) NULL AFTER sede;
