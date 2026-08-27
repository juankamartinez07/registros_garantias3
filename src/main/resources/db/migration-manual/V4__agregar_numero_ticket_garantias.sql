ALTER TABLE garantias
    ADD COLUMN numero_ticket VARCHAR(5) NULL AFTER equipo_id;

CREATE UNIQUE INDEX uk_garantias_numero_ticket
    ON garantias (numero_ticket);
