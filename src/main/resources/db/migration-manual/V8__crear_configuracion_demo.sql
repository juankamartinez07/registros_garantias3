CREATE TABLE IF NOT EXISTS configuracion_demo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    demo_activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_inicio_demo DATE NOT NULL,
    dias_demo INT NOT NULL DEFAULT 10,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO configuracion_demo (demo_activa, fecha_inicio_demo, dias_demo)
SELECT TRUE, CURRENT_DATE, 10
WHERE NOT EXISTS (SELECT 1 FROM configuracion_demo);
