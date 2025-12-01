-- Migración: Agregar relación entre InformedConsent y MedicalRecord
-- Fecha: 2025-12-01
-- Descripción: Agrega la columna medical_record_id a la tabla informed_consents

-- Agregar columna medical_record_id
ALTER TABLE informed_consents 
ADD COLUMN IF NOT EXISTS medical_record_id BIGINT;

-- Agregar foreign key constraint
ALTER TABLE informed_consents
ADD CONSTRAINT fk_informed_consent_medical_record 
FOREIGN KEY (medical_record_id) 
REFERENCES medical_records(id) 
ON DELETE SET NULL;

-- Crear índice para mejorar performance
CREATE INDEX IF NOT EXISTS idx_informed_consent_medical_record 
ON informed_consents(medical_record_id);

-- Actualizar consentimientos existentes para asociarlos a sus historias clínicas
-- Esto asocia los consentimientos a la historia clínica activa de cada paciente
UPDATE informed_consents ic
SET medical_record_id = (
    SELECT mr.id 
    FROM medical_records mr 
    WHERE mr.patient_id = ic.patient_id 
    AND mr.is_active = true 
    ORDER BY mr.created_at ASC 
    LIMIT 1
)
WHERE ic.medical_record_id IS NULL
AND EXISTS (
    SELECT 1 
    FROM medical_records mr 
    WHERE mr.patient_id = ic.patient_id 
    AND mr.is_active = true
);

-- Comentario en la columna
COMMENT ON COLUMN informed_consents.medical_record_id IS 'Referencia a la historia clínica asociada al consentimiento informado';

