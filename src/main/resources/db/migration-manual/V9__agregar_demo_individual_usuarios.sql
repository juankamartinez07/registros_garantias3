ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS demo_individual_activa BOOLEAN NULL,
    ADD COLUMN IF NOT EXISTS fecha_inicio_demo_individual DATE NULL,
    ADD COLUMN IF NOT EXISTS dias_demo_individual INT NULL;
