-- Migración para agregar columna heart_rate a la tabla medical_records
-- Ejecutar este script en tu base de datos

ALTER TABLE medical_records 
ADD COLUMN heart_rate DECIMAL(5,2) NULL;

-- Comentario: La frecuencia cardíaca se mide en bpm (latidos por minuto)
-- Rango típico: 60-200 bpm para perros y gatos
-- NULL permite que sea opcional

