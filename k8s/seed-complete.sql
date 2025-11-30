-- =====================================================
-- Script COMPLETO de población de datos para VetClinic Pro
-- =====================================================

-- Obtener IDs necesarios
DO $$
DECLARE
    vet_id UUID := '96205823-8954-4570-83b0-028ea041db9f'; -- admin como veterinario
    owner1_id BIGINT;
    owner2_id BIGINT;
    patient1_id BIGINT;
    patient2_id BIGINT;
    patient3_id BIGINT;
    patient4_id BIGINT;
    patient5_id BIGINT;
    apt1_id BIGINT;
    apt2_id BIGINT;
    apt3_id BIGINT;
    apt4_id BIGINT;
    apt5_id BIGINT;
    apt6_id BIGINT;
    mr1_id BIGINT;
    mr2_id BIGINT;
    mr3_id BIGINT;
    mr4_id BIGINT;
BEGIN
    -- Obtener IDs de propietarios
    SELECT id INTO owner1_id FROM owners ORDER BY id LIMIT 1;
    SELECT id INTO owner2_id FROM owners ORDER BY id DESC LIMIT 1;
    
    -- Obtener IDs de pacientes
    SELECT id INTO patient1_id FROM patients WHERE name = 'Max' LIMIT 1;
    SELECT id INTO patient2_id FROM patients WHERE name = 'Luna' LIMIT 1;
    SELECT id INTO patient3_id FROM patients WHERE name = 'Rocky' LIMIT 1;
    SELECT id INTO patient4_id FROM patients WHERE name = 'Nala' LIMIT 1;
    SELECT id INTO patient5_id FROM patients WHERE name = 'Thor' LIMIT 1;

    IF patient1_id IS NULL THEN
        SELECT id INTO patient1_id FROM patients ORDER BY id LIMIT 1;
    END IF;
    IF patient2_id IS NULL THEN
        SELECT id INTO patient2_id FROM patients ORDER BY id OFFSET 1 LIMIT 1;
    END IF;
    IF patient3_id IS NULL THEN
        SELECT id INTO patient3_id FROM patients ORDER BY id OFFSET 2 LIMIT 1;
    END IF;
    IF patient4_id IS NULL THEN
        SELECT id INTO patient4_id FROM patients ORDER BY id OFFSET 3 LIMIT 1;
    END IF;
    IF patient5_id IS NULL THEN
        SELECT id INTO patient5_id FROM patients ORDER BY id OFFSET 4 LIMIT 1;
    END IF;

    RAISE NOTICE 'IDs obtenidos - Owner1: %, Owner2: %, Patients: %, %, %, %, %', 
        owner1_id, owner2_id, patient1_id, patient2_id, patient3_id, patient4_id, patient5_id;

    -- =====================================================
    -- CITAS COMPLETADAS (pasadas)
    -- =====================================================
    INSERT INTO appointments (appointment_type, reason, scheduled_date, duration_minutes, status, notes, owner_id, patient_id, veterinarian_id, is_active, created_at, updated_at)
    VALUES 
        ('CONSULTA', 'Chequeo anual de rutina', NOW() - INTERVAL '30 days', 30, 'COMPLETED', 'Paciente en excelente estado de salud', owner1_id, patient1_id, vet_id, true, NOW() - INTERVAL '35 days', NOW() - INTERVAL '30 days')
    RETURNING id INTO apt1_id;

    INSERT INTO appointments (appointment_type, reason, scheduled_date, duration_minutes, status, notes, owner_id, patient_id, veterinarian_id, is_active, created_at, updated_at)
    VALUES 
        ('VACUNACION', 'Vacuna antirrábica anual', NOW() - INTERVAL '25 days', 20, 'COMPLETED', 'Vacuna aplicada sin complicaciones', owner1_id, patient2_id, vet_id, true, NOW() - INTERVAL '30 days', NOW() - INTERVAL '25 days')
    RETURNING id INTO apt2_id;

    INSERT INTO appointments (appointment_type, reason, scheduled_date, duration_minutes, status, notes, owner_id, patient_id, veterinarian_id, is_active, created_at, updated_at)
    VALUES 
        ('EMERGENCIA', 'Vómitos y diarrea persistente', NOW() - INTERVAL '15 days', 45, 'COMPLETED', 'Gastroenteritis tratada exitosamente', owner2_id, patient3_id, vet_id, true, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days')
    RETURNING id INTO apt3_id;

    INSERT INTO appointments (appointment_type, reason, scheduled_date, duration_minutes, status, notes, owner_id, patient_id, veterinarian_id, is_active, created_at, updated_at)
    VALUES 
        ('CIRUGIA', 'Esterilización programada', NOW() - INTERVAL '10 days', 60, 'COMPLETED', 'Cirugía exitosa, recuperación normal', owner2_id, patient4_id, vet_id, true, NOW() - INTERVAL '20 days', NOW() - INTERVAL '10 days')
    RETURNING id INTO apt4_id;

    -- =====================================================
    -- CITAS FUTURAS (programadas)
    -- =====================================================
    INSERT INTO appointments (appointment_type, reason, scheduled_date, duration_minutes, status, notes, owner_id, patient_id, veterinarian_id, is_active, created_at, updated_at)
    VALUES 
        ('CONSULTA', 'Seguimiento post-operatorio', NOW() + INTERVAL '2 days', 30, 'SCHEDULED', 'Revisión de puntos', owner2_id, patient4_id, vet_id, true, NOW(), NOW())
    RETURNING id INTO apt5_id;

    INSERT INTO appointments (appointment_type, reason, scheduled_date, duration_minutes, status, notes, owner_id, patient_id, veterinarian_id, is_active, created_at, updated_at)
    VALUES 
        ('VACUNACION', 'Refuerzo vacuna séxtuple', NOW() + INTERVAL '5 days', 20, 'CONFIRMED', NULL, owner1_id, patient1_id, vet_id, true, NOW(), NOW());

    INSERT INTO appointments (appointment_type, reason, scheduled_date, duration_minutes, status, notes, owner_id, patient_id, veterinarian_id, is_active, created_at, updated_at)
    VALUES 
        ('CONSULTA', 'Control de peso y alimentación', NOW() + INTERVAL '7 days', 30, 'SCHEDULED', NULL, owner2_id, patient3_id, vet_id, true, NOW(), NOW());

    INSERT INTO appointments (appointment_type, reason, scheduled_date, duration_minutes, status, notes, owner_id, patient_id, veterinarian_id, is_active, created_at, updated_at)
    VALUES 
        ('ESTETICA', 'Grooming completo', NOW() + INTERVAL '10 days', 90, 'SCHEDULED', 'Baño, corte y limpieza de oídos', owner1_id, patient1_id, vet_id, true, NOW(), NOW());

    INSERT INTO appointments (appointment_type, reason, scheduled_date, duration_minutes, status, notes, owner_id, patient_id, veterinarian_id, is_active, created_at, updated_at)
    VALUES 
        ('LABORATORIO', 'Análisis de sangre de rutina', NOW() + INTERVAL '14 days', 15, 'SCHEDULED', 'Hemograma y bioquímica', owner2_id, patient5_id, vet_id, true, NOW(), NOW());

    RAISE NOTICE 'Citas insertadas: apt1=%, apt2=%, apt3=%, apt4=%', apt1_id, apt2_id, apt3_id, apt4_id;

    -- =====================================================
    -- HISTORIALES MÉDICOS
    -- =====================================================
    INSERT INTO medical_records (record_date, diagnosis, treatment, symptoms, vital_signs, weight, temperature, heart_rate, follow_up_required, follow_up_date, notes, patient_id, veterinarian_id, appointment_id, is_active, created_at, updated_at)
    VALUES 
        (NOW() - INTERVAL '30 days', 
         'Paciente sano. Sin hallazgos patológicos en examen físico.',
         'No requiere tratamiento. Continuar con dieta balanceada y ejercicio regular.',
         'Ninguno reportado',
         'FC: 80 bpm, FR: 20 rpm, T: 38.5°C, Mucosas rosadas',
         32.5, 38.5, 80, false, NULL,
         'Peso estable. Pelaje brillante. Dientes en buen estado.',
         patient1_id, vet_id, apt1_id, true, NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days')
    RETURNING id INTO mr1_id;

    INSERT INTO medical_records (record_date, diagnosis, treatment, symptoms, vital_signs, weight, temperature, heart_rate, follow_up_required, follow_up_date, notes, patient_id, veterinarian_id, appointment_id, is_active, created_at, updated_at)
    VALUES 
        (NOW() - INTERVAL '25 days',
         'Paciente apto para vacunación. Vacuna antirrábica aplicada.',
         'Vacuna antirrábica inyectable. Observación 15 minutos post-vacunación.',
         'Ninguno',
         'FC: 90 bpm, FR: 22 rpm, T: 38.3°C',
         28.0, 38.3, 90, true, NOW() + INTERVAL '365 days',
         'Sin reacciones adversas. Próxima vacuna en 1 año.',
         patient2_id, vet_id, apt2_id, true, NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days')
    RETURNING id INTO mr2_id;

    INSERT INTO medical_records (record_date, diagnosis, treatment, symptoms, vital_signs, weight, temperature, heart_rate, follow_up_required, follow_up_date, notes, patient_id, veterinarian_id, appointment_id, is_active, created_at, updated_at)
    VALUES 
        (NOW() - INTERVAL '15 days',
         'Gastroenteritis aguda de probable origen alimentario.',
         'Metronidazol 250mg c/12h x 7 días. Omeprazol 20mg c/24h x 5 días. Dieta blanda.',
         'Vómitos frecuentes, diarrea acuosa, inapetencia, letargia',
         'FC: 120 bpm, FR: 30 rpm, T: 39.2°C, Deshidratación leve',
         11.5, 39.2, 120, true, NOW() - INTERVAL '8 days',
         'Administrar suero oral. Control en 7 días.',
         patient3_id, vet_id, apt3_id, true, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days')
    RETURNING id INTO mr3_id;

    INSERT INTO medical_records (record_date, diagnosis, treatment, symptoms, vital_signs, weight, temperature, heart_rate, follow_up_required, follow_up_date, notes, patient_id, veterinarian_id, appointment_id, is_active, created_at, updated_at)
    VALUES 
        (NOW() - INTERVAL '10 days',
         'Ovariohisterectomía electiva realizada sin complicaciones.',
         'Meloxicam 0.1mg/kg c/24h x 5 días. Amoxicilina 25mg/kg c/12h x 7 días. Collar isabelino.',
         'N/A - Procedimiento quirúrgico electivo',
         'FC: 160 bpm (bajo anestesia), FR: 18 rpm, T: 37.8°C',
         3.8, 37.8, 160, true, NOW() + INTERVAL '2 days',
         'Cirugía exitosa. Retiro de puntos en 10-12 días.',
         patient4_id, vet_id, apt4_id, true, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days')
    RETURNING id INTO mr4_id;

    RAISE NOTICE 'Historiales médicos insertados: mr1=%, mr2=%, mr3=%, mr4=%', mr1_id, mr2_id, mr3_id, mr4_id;

    -- =====================================================
    -- PRESCRIPCIONES
    -- =====================================================
    INSERT INTO prescriptions (medication_name, dosage, frequency, duration, start_date, end_date, instructions, notes, patient_id, medical_record_id, is_active, created_at, updated_at)
    VALUES 
        ('Metronidazol 250mg', '1 tableta', 'Cada 12 horas', '7 días', NOW() - INTERVAL '15 days', NOW() - INTERVAL '8 days',
         'Administrar con alimento para evitar malestar estomacal', 'Completar tratamiento aunque mejore',
         patient3_id, mr3_id, true, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
        
        ('Omeprazol 20mg', '1 cápsula', 'Cada 24 horas', '5 días', NOW() - INTERVAL '15 days', NOW() - INTERVAL '10 days',
         'Administrar 30 minutos antes del alimento de la mañana', NULL,
         patient3_id, mr3_id, true, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
        
        ('Meloxicam 2mg', '0.5 tableta', 'Cada 24 horas', '5 días', NOW() - INTERVAL '10 days', NOW() - INTERVAL '5 days',
         'Administrar con alimento. No usar otros antiinflamatorios', 'Para manejo del dolor post-quirúrgico',
         patient4_id, mr4_id, true, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
        
        ('Amoxicilina 500mg', '1/4 tableta', 'Cada 12 horas', '7 días', NOW() - INTERVAL '10 days', NOW() - INTERVAL '3 days',
         'Triturar y mezclar con alimento húmedo', 'Antibiótico preventivo post-cirugía',
         patient4_id, mr4_id, true, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days');

    RAISE NOTICE 'Prescripciones insertadas';

    -- =====================================================
    -- CONSENTIMIENTOS INFORMADOS
    -- =====================================================
    INSERT INTO informed_consents (procedure_type, procedure_description, risks, benefits, alternatives, is_signed, signed_date, owner_signature, patient_id, owner_id, veterinarian_id, appointment_id, is_active, created_at, updated_at)
    VALUES 
        ('CIRUGIA', 
         'Ovariohisterectomía (esterilización) felina. Procedimiento quirúrgico para remover ovarios y útero.',
         'Riesgos anestésicos, sangrado, infección post-operatoria, reacción a suturas',
         'Prevención de embarazos no deseados, reducción de riesgo de tumores mamarios, eliminación de celo',
         'No realizar procedimiento, anticonceptivos hormonales (no recomendado a largo plazo)',
         true, NOW() - INTERVAL '10 days', 'Firmado electrónicamente',
         patient4_id, owner2_id, vet_id, apt4_id, true, NOW() - INTERVAL '12 days', NOW() - INTERVAL '10 days'),
        
        ('VACUNACION',
         'Aplicación de vacuna antirrábica anual según normativa sanitaria vigente.',
         'Reacción alérgica local o sistémica (raro), malestar temporal',
         'Protección contra el virus de la rabia, cumplimiento legal',
         'No vacunar (no recomendado, riesgo legal y de salud)',
         true, NOW() - INTERVAL '25 days', 'Firmado electrónicamente',
         patient2_id, owner1_id, vet_id, apt2_id, true, NOW() - INTERVAL '26 days', NOW() - INTERVAL '25 days');

    RAISE NOTICE 'Consentimientos informados insertados';

    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ TODOS LOS DATOS INSERTADOS CORRECTAMENTE';
    RAISE NOTICE '========================================';

END $$;

-- =====================================================
-- Verificación final
-- =====================================================
DO $$
DECLARE
    apt_count INTEGER;
    mr_count INTEGER;
    presc_count INTEGER;
    consent_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO apt_count FROM appointments;
    SELECT COUNT(*) INTO mr_count FROM medical_records;
    SELECT COUNT(*) INTO presc_count FROM prescriptions;
    SELECT COUNT(*) INTO consent_count FROM informed_consents;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'RESUMEN DE DATOS:';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Citas: %', apt_count;
    RAISE NOTICE 'Historiales Médicos: %', mr_count;
    RAISE NOTICE 'Prescripciones: %', presc_count;
    RAISE NOTICE 'Consentimientos: %', consent_count;
    RAISE NOTICE '========================================';
END $$;

