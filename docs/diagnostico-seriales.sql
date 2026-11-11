-- Consultas de solo lectura. Ejecutar en la base seleccionada y autorizada.
SELECT VERSION() AS version_servidor, DATABASE() AS base_actual;

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, CHARACTER_SET_NAME, COLLATION_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'equipos' AND COLUMN_NAME = 'serial';

SELECT INDEX_NAME, NON_UNIQUE, SEQ_IN_INDEX, COLUMN_NAME, SUB_PART
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'equipos'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

SELECT UPPER(TRIM(serial)) AS serial_normalizado, COUNT(*) AS cantidad
FROM equipos
WHERE serial IS NOT NULL AND TRIM(serial) <> ''
GROUP BY UPPER(TRIM(serial))
HAVING COUNT(*) > 1;

SELECT COUNT(*) AS seriales_nulos_o_vacios
FROM equipos
WHERE serial IS NULL OR TRIM(serial) = '';
