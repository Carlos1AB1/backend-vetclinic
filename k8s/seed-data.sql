-- =====================================================
-- Script de población de datos para VetClinic Pro
-- =====================================================

-- =====================================================
-- SERVICIOS VETERINARIOS
-- =====================================================
INSERT INTO services (name, description, category, price, duration_minutes, requires_appointment, is_active, created_at, updated_at)
VALUES 
    ('Consulta General', 'Examen físico completo y evaluación del estado de salud', 'CONSULTA', 45000, 30, true, true, NOW(), NOW()),
    ('Consulta de Emergencia', 'Atención urgente para casos críticos', 'EMERGENCIA', 85000, 45, false, true, NOW(), NOW()),
    ('Vacunación Canina', 'Aplicación de vacunas para perros (Séxtuple, Rabia)', 'VACUNACION', 55000, 20, true, true, NOW(), NOW()),
    ('Vacunación Felina', 'Aplicación de vacunas para gatos (Triple Felina, Rabia)', 'VACUNACION', 50000, 20, true, true, NOW(), NOW()),
    ('Desparasitación Interna', 'Tratamiento antiparasitario interno', 'PREVENCION', 35000, 15, true, true, NOW(), NOW()),
    ('Desparasitación Externa', 'Aplicación de antipulgas y garrapatas', 'PREVENCION', 40000, 15, true, true, NOW(), NOW()),
    ('Esterilización Canina Macho', 'Cirugía de castración para perros', 'CIRUGIA', 180000, 60, true, true, NOW(), NOW()),
    ('Esterilización Canina Hembra', 'Cirugía de ovariohisterectomía para perras', 'CIRUGIA', 250000, 90, true, true, NOW(), NOW()),
    ('Esterilización Felina Macho', 'Cirugía de castración para gatos', 'CIRUGIA', 120000, 45, true, true, NOW(), NOW()),
    ('Esterilización Felina Hembra', 'Cirugía de ovariohisterectomía para gatas', 'CIRUGIA', 180000, 60, true, true, NOW(), NOW()),
    ('Limpieza Dental', 'Profilaxis dental con ultrasonido', 'DENTAL', 150000, 60, true, true, NOW(), NOW()),
    ('Radiografía', 'Estudio radiográfico digital', 'DIAGNOSTICO', 80000, 30, true, true, NOW(), NOW()),
    ('Ecografía Abdominal', 'Estudio ecográfico de órganos abdominales', 'DIAGNOSTICO', 120000, 45, true, true, NOW(), NOW()),
    ('Análisis de Sangre Completo', 'Hemograma, bioquímica y perfil hepático/renal', 'LABORATORIO', 95000, 15, true, true, NOW(), NOW()),
    ('Análisis de Orina', 'Urianálisis completo', 'LABORATORIO', 45000, 10, true, true, NOW(), NOW()),
    ('Hospitalización (por día)', 'Internación con monitoreo y cuidados', 'HOSPITALIZACION', 85000, 1440, false, true, NOW(), NOW()),
    ('Grooming Canino Pequeño', 'Baño, corte y arreglo para razas pequeñas', 'ESTETICA', 45000, 60, true, true, NOW(), NOW()),
    ('Grooming Canino Grande', 'Baño, corte y arreglo para razas grandes', 'ESTETICA', 70000, 90, true, true, NOW(), NOW()),
    ('Microchip', 'Implantación de microchip de identificación', 'IDENTIFICACION', 65000, 15, true, true, NOW(), NOW()),
    ('Certificado de Salud', 'Emisión de certificado veterinario oficial', 'DOCUMENTOS', 35000, 20, true, true, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- INVENTARIO
-- =====================================================
INSERT INTO inventory_items (name, description, category, sku, quantity, min_stock_level, unit_price, supplier, location, is_active, created_at, updated_at)
VALUES 
    -- Medicamentos
    ('Amoxicilina 500mg', 'Antibiótico de amplio espectro - Caja x 20 tabletas', 'MEDICAMENTO', 'MED-AMX-500', 150, 30, 25000, 'Laboratorios VetPharma', 'Estante A1', true, NOW(), NOW()),
    ('Meloxicam 2mg', 'Antiinflamatorio no esteroideo - Caja x 10 tabletas', 'MEDICAMENTO', 'MED-MLX-002', 80, 20, 35000, 'Laboratorios VetPharma', 'Estante A1', true, NOW(), NOW()),
    ('Metronidazol 250mg', 'Antiparasitario y antibacteriano - Caja x 20 tabletas', 'MEDICAMENTO', 'MED-MTZ-250', 100, 25, 22000, 'Laboratorios VetPharma', 'Estante A2', true, NOW(), NOW()),
    ('Omeprazol 20mg', 'Protector gástrico - Caja x 14 cápsulas', 'MEDICAMENTO', 'MED-OMP-020', 60, 15, 28000, 'Farmacéuticos Unidos', 'Estante A2', true, NOW(), NOW()),
    ('Prednisolona 5mg', 'Corticosteroide - Caja x 30 tabletas', 'MEDICAMENTO', 'MED-PRD-005', 45, 10, 32000, 'Laboratorios VetPharma', 'Estante A3', true, NOW(), NOW()),
    ('Ivermectina 1%', 'Antiparasitario inyectable - Frasco 50ml', 'MEDICAMENTO', 'MED-IVR-001', 25, 8, 45000, 'Zoetis', 'Refrigerador R1', true, NOW(), NOW()),
    ('Cefalexina 500mg', 'Antibiótico cefalosporina - Caja x 20 cápsulas', 'MEDICAMENTO', 'MED-CFX-500', 70, 15, 38000, 'Laboratorios VetPharma', 'Estante A1', true, NOW(), NOW()),
    
    -- Vacunas
    ('Vacuna Séxtuple Canina', 'Moquillo, Hepatitis, Parvo, Para, Lepto, Corona', 'VACUNA', 'VAC-SEX-CAN', 50, 15, 35000, 'MSD Animal Health', 'Refrigerador R2', true, NOW(), NOW()),
    ('Vacuna Antirrábica', 'Vacuna contra la rabia - Uso en perros y gatos', 'VACUNA', 'VAC-RAB-001', 80, 20, 25000, 'Zoetis', 'Refrigerador R2', true, NOW(), NOW()),
    ('Vacuna Triple Felina', 'Rinotraqueitis, Calicivirus, Panleucopenia', 'VACUNA', 'VAC-TRI-FEL', 40, 10, 32000, 'MSD Animal Health', 'Refrigerador R2', true, NOW(), NOW()),
    ('Vacuna Bordetella', 'Tos de las perreras intranasal', 'VACUNA', 'VAC-BOR-001', 30, 8, 45000, 'Zoetis', 'Refrigerador R2', true, NOW(), NOW()),
    
    -- Insumos Quirúrgicos
    ('Sutura Vicryl 3-0', 'Sutura absorbible - Caja x 12 unidades', 'INSUMO_QUIRURGICO', 'SUQ-VIC-030', 20, 5, 85000, 'Ethicon', 'Estante Q1', true, NOW(), NOW()),
    ('Sutura Nylon 3-0', 'Sutura no absorbible - Caja x 12 unidades', 'INSUMO_QUIRURGICO', 'SUQ-NYL-030', 25, 5, 45000, 'Covidien', 'Estante Q1', true, NOW(), NOW()),
    ('Guantes Quirúrgicos Estériles', 'Talla M - Caja x 50 pares', 'INSUMO_QUIRURGICO', 'SUQ-GUA-MED', 15, 3, 65000, 'Ansell', 'Estante Q2', true, NOW(), NOW()),
    ('Gasas Estériles', 'Paquete x 100 unidades 10x10cm', 'INSUMO_QUIRURGICO', 'SUQ-GAS-100', 30, 10, 25000, 'Medifarma', 'Estante Q2', true, NOW(), NOW()),
    ('Catéter Intravenoso 22G', 'Caja x 50 unidades', 'INSUMO_QUIRURGICO', 'SUQ-CAT-022', 10, 3, 55000, 'BD Medical', 'Estante Q3', true, NOW(), NOW()),
    
    -- Alimentos
    ('Royal Canin Adulto Razas Pequeñas', 'Alimento seco premium - Bolsa 3kg', 'ALIMENTO', 'ALI-RC-APQ', 25, 8, 95000, 'Royal Canin', 'Estante B1', true, NOW(), NOW()),
    ('Hills Science Diet Adulto', 'Alimento seco premium - Bolsa 4kg', 'ALIMENTO', 'ALI-HS-ADU', 20, 6, 115000, 'Hills Pet', 'Estante B1', true, NOW(), NOW()),
    ('Whiskas Gato Adulto', 'Alimento seco - Bolsa 1.5kg', 'ALIMENTO', 'ALI-WK-GAD', 30, 10, 35000, 'Mars Petcare', 'Estante B2', true, NOW(), NOW()),
    ('Royal Canin Recovery', 'Alimento de recuperación - Lata 195g', 'ALIMENTO', 'ALI-RC-REC', 40, 15, 18000, 'Royal Canin', 'Estante B3', true, NOW(), NOW()),
    
    -- Accesorios
    ('Collar Isabelino Mediano', 'Collar de protección talla M', 'ACCESORIO', 'ACC-COL-MED', 15, 5, 25000, 'PetSupplies', 'Estante C1', true, NOW(), NOW()),
    ('Jeringa 5ml', 'Caja x 100 unidades', 'ACCESORIO', 'ACC-JER-005', 20, 5, 35000, 'BD Medical', 'Estante C2', true, NOW(), NOW()),
    ('Termómetro Digital Veterinario', 'Lectura rápida, punta flexible', 'ACCESORIO', 'ACC-TER-DIG', 8, 2, 45000, 'VetEquip', 'Estante C3', true, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- PACIENTES (necesitan owner_id existente)
-- =====================================================
DO $$
DECLARE
    owner1_id BIGINT;
    owner2_id BIGINT;
BEGIN
    -- Obtener IDs de propietarios existentes
    SELECT id INTO owner1_id FROM owners WHERE is_active = true ORDER BY id LIMIT 1;
    SELECT id INTO owner2_id FROM owners WHERE is_active = true ORDER BY id DESC LIMIT 1;
    
    IF owner1_id IS NOT NULL THEN
        -- Insertar pacientes para el primer propietario
        INSERT INTO patients (name, species, breed, gender, birth_date, weight, color, microchip_number, allergies, medical_history, notes, owner_id, is_active, created_at, updated_at)
        VALUES 
            ('Max', 'PERRO', 'Golden Retriever', 'MACHO', '2020-03-15', 32.5, 'Dorado', '985112000123456', 'Ninguna conocida', 'Vacunación al día, desparasitado', 'Muy juguetón y activo', owner1_id, true, NOW(), NOW()),
            ('Luna', 'PERRO', 'Labrador', 'HEMBRA', '2019-08-22', 28.0, 'Negro', '985112000123457', 'Alergia al pollo', 'Dermatitis tratada en 2022', 'Tranquila, buen comportamiento', owner1_id, true, NOW(), NOW()),
            ('Michi', 'GATO', 'Persa', 'MACHO', '2021-01-10', 4.5, 'Blanco', '985112000123458', 'Ninguna', 'Castrado en 2022', 'Gato de interior', owner1_id, true, NOW(), NOW())
        ON CONFLICT DO NOTHING;
        
        RAISE NOTICE 'Pacientes insertados para owner_id: %', owner1_id;
    END IF;
    
    IF owner2_id IS NOT NULL AND owner2_id != owner1_id THEN
        -- Insertar pacientes para el segundo propietario
        INSERT INTO patients (name, species, breed, gender, birth_date, weight, color, microchip_number, allergies, medical_history, notes, owner_id, is_active, created_at, updated_at)
        VALUES 
            ('Rocky', 'PERRO', 'Bulldog Francés', 'MACHO', '2022-05-20', 12.0, 'Atigrado', '985112000123459', 'Sensibilidad digestiva', 'Problemas respiratorios leves', 'Necesita paseos cortos', owner2_id, true, NOW(), NOW()),
            ('Nala', 'GATO', 'Siamés', 'HEMBRA', '2020-11-05', 3.8, 'Seal Point', '985112000123460', 'Ninguna', 'Esterilizada', 'Muy vocal y cariñosa', owner2_id, true, NOW(), NOW()),
            ('Thor', 'PERRO', 'Pastor Alemán', 'MACHO', '2018-02-14', 38.0, 'Negro y fuego', '985112000123461', 'Ninguna', 'Displasia de cadera en tratamiento', 'Requiere suplementos articulares', owner2_id, true, NOW(), NOW()),
            ('Coco', 'OTRO', 'Conejo Holland Lop', 'HEMBRA', '2023-04-01', 1.8, 'Gris', NULL, 'Ninguna', 'Revisión dental cada 6 meses', 'Dieta rica en heno', owner2_id, true, NOW(), NOW())
        ON CONFLICT DO NOTHING;
        
        RAISE NOTICE 'Pacientes insertados para owner_id: %', owner2_id;
    END IF;
END $$;

-- =====================================================
-- Verificar datos insertados
-- =====================================================
DO $$
DECLARE
    srv_count INTEGER;
    inv_count INTEGER;
    pat_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO srv_count FROM services;
    SELECT COUNT(*) INTO inv_count FROM inventory_items;
    SELECT COUNT(*) INTO pat_count FROM patients;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ DATOS INSERTADOS CORRECTAMENTE';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Servicios: %', srv_count;
    RAISE NOTICE 'Items de Inventario: %', inv_count;
    RAISE NOTICE 'Pacientes: %', pat_count;
    RAISE NOTICE '========================================';
END $$;
