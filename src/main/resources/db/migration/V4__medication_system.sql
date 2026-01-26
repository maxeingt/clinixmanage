-- =====================================================
-- CLINICXMANAGE - MIGRACIÓN V4
-- Sistema de Medicamentos
-- Fecha: 2026-01-26
-- =====================================================

-- =====================================================
-- PARTE 1: TIPO ENUMERADO PARA PRESENTACIÓN
-- =====================================================

DO $$ BEGIN
    CREATE TYPE presentation_type AS ENUM (
        'TABLET',       -- Tableta
        'CAPSULE',      -- Cápsula
        'SYRUP',        -- Jarabe
        'INJECTABLE',   -- Inyectable
        'CREAM',        -- Crema
        'OINTMENT',     -- Ungüento
        'DROPS',        -- Gotas
        'SUSPENSION',   -- Suspensión
        'POWDER',       -- Polvo
        'SOLUTION',     -- Solución
        'PATCH',        -- Parche
        'SUPPOSITORY',  -- Supositorio
        'INHALER',      -- Inhalador
        'GEL',          -- Gel
        'SPRAY'         -- Spray
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- =====================================================
-- PARTE 2: TABLA PHARMACEUTICAL (Farmacéuticas)
-- =====================================================

CREATE TABLE IF NOT EXISTS pharmaceutical (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

COMMENT ON TABLE pharmaceutical IS 'Compañías farmacéuticas productoras de medicamentos';

-- =====================================================
-- PARTE 3: TABLA DISTRIBUTOR (Distribuidores)
-- =====================================================

CREATE TABLE IF NOT EXISTS distributor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

COMMENT ON TABLE distributor IS 'Distribuidores de medicamentos';

-- =====================================================
-- PARTE 4: TABLA MEDICATION (Medicamentos)
-- =====================================================

CREATE TABLE IF NOT EXISTS medication (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    code VARCHAR(50) UNIQUE,
    active_ingredient VARCHAR(200) NOT NULL,
    concentration VARCHAR(100) NOT NULL,
    presentation VARCHAR(50),
    indications TEXT,
    contraindications TEXT,
    price DECIMAL(10, 2),
    active BOOLEAN NOT NULL DEFAULT true,
    pharmaceutical_id UUID,
    distributor_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_medication_pharmaceutical
        FOREIGN KEY (pharmaceutical_id) REFERENCES pharmaceutical(id) ON DELETE SET NULL,
    CONSTRAINT fk_medication_distributor
        FOREIGN KEY (distributor_id) REFERENCES distributor(id) ON DELETE SET NULL
);

COMMENT ON TABLE medication IS 'Catálogo de medicamentos disponibles';
COMMENT ON COLUMN medication.active_ingredient IS 'Principio activo del medicamento';
COMMENT ON COLUMN medication.concentration IS 'Concentración del principio activo (ej: 500mg, 10mg/ml)';
COMMENT ON COLUMN medication.presentation IS 'Tipo de presentación (TABLET, CAPSULE, SYRUP, etc.)';

-- =====================================================
-- PARTE 5: TABLA PRESCRIPTION_MEDICATION (Relación Prescripción-Medicamento)
-- =====================================================

CREATE TABLE IF NOT EXISTS prescription_medication (
    prescription_id UUID NOT NULL,
    medication_id UUID NOT NULL,
    dose VARCHAR(100) NOT NULL,
    frequency VARCHAR(100) NOT NULL,
    duration VARCHAR(100),
    quantity INTEGER,
    administration_route VARCHAR(100),
    specific_indications TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (prescription_id, medication_id),

    CONSTRAINT fk_prescription_medication_prescription
        FOREIGN KEY (prescription_id) REFERENCES prescription(id) ON DELETE CASCADE,
    CONSTRAINT fk_prescription_medication_medication
        FOREIGN KEY (medication_id) REFERENCES medication(id) ON DELETE RESTRICT
);

COMMENT ON TABLE prescription_medication IS 'Medicamentos incluidos en una prescripción médica';
COMMENT ON COLUMN prescription_medication.dose IS 'Dosis a administrar (ej: 1 tableta, 5ml)';
COMMENT ON COLUMN prescription_medication.frequency IS 'Frecuencia de administración (ej: cada 8 horas)';
COMMENT ON COLUMN prescription_medication.duration IS 'Duración del tratamiento (ej: 7 días)';
COMMENT ON COLUMN prescription_medication.administration_route IS 'Vía de administración (oral, tópica, etc.)';

-- =====================================================
-- PARTE 6: ÍNDICES DE RENDIMIENTO
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_pharmaceutical_name ON pharmaceutical(name);
CREATE INDEX IF NOT EXISTS idx_pharmaceutical_active ON pharmaceutical(active);

CREATE INDEX IF NOT EXISTS idx_distributor_name ON distributor(name);
CREATE INDEX IF NOT EXISTS idx_distributor_active ON distributor(active);

CREATE INDEX IF NOT EXISTS idx_medication_name ON medication(name);
CREATE INDEX IF NOT EXISTS idx_medication_code ON medication(code);
CREATE INDEX IF NOT EXISTS idx_medication_active_ingredient ON medication(active_ingredient);
CREATE INDEX IF NOT EXISTS idx_medication_pharmaceutical ON medication(pharmaceutical_id);
CREATE INDEX IF NOT EXISTS idx_medication_distributor ON medication(distributor_id);
CREATE INDEX IF NOT EXISTS idx_medication_active ON medication(active);

CREATE INDEX IF NOT EXISTS idx_prescription_medication_prescription ON prescription_medication(prescription_id);
CREATE INDEX IF NOT EXISTS idx_prescription_medication_medication ON prescription_medication(medication_id);

-- =====================================================
-- PARTE 7: DATOS DEMO - FARMACÉUTICAS
-- =====================================================

INSERT INTO pharmaceutical (name, description) VALUES
    ('Bayer', 'Empresa farmacéutica alemana fundada en 1863'),
    ('Pfizer', 'Empresa farmacéutica estadounidense'),
    ('Novartis', 'Empresa farmacéutica suiza'),
    ('Roche', 'Empresa farmacéutica y de diagnósticos suiza'),
    ('Johnson & Johnson', 'Empresa estadounidense de productos farmacéuticos y de consumo'),
    ('Sanofi', 'Empresa farmacéutica francesa'),
    ('GlaxoSmithKline', 'Empresa farmacéutica británica'),
    ('AstraZeneca', 'Empresa farmacéutica británica-sueca'),
    ('Merck', 'Empresa farmacéutica alemana'),
    ('Abbott', 'Empresa farmacéutica estadounidense')
ON CONFLICT DO NOTHING;

-- =====================================================
-- PARTE 8: DATOS DEMO - DISTRIBUIDORES
-- =====================================================

INSERT INTO distributor (name, description) VALUES
    ('Farmacias Galeno', 'Cadena de farmacias con cobertura nacional'),
    ('Droguería Central', 'Distribuidora mayorista de medicamentos'),
    ('MediDistribuciones', 'Distribuidora especializada en medicamentos de alta especialidad'),
    ('FarmaExpress', 'Distribuidora con servicio de entrega express'),
    ('Salud Total', 'Distribuidora de productos farmacéuticos y de salud')
ON CONFLICT DO NOTHING;

-- =====================================================
-- PARTE 9: DATOS DEMO - MEDICAMENTOS
-- =====================================================

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Ibuprofeno 400mg',
    'IBU400',
    'Ibuprofeno',
    '400mg',
    'TABLET',
    'Dolor leve a moderado, fiebre, inflamación',
    'Úlcera gástrica, insuficiencia renal severa, tercer trimestre de embarazo',
    15.50,
    (SELECT id FROM pharmaceutical WHERE name = 'Bayer' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'Farmacias Galeno' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'IBU400');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Paracetamol 500mg',
    'PARA500',
    'Paracetamol',
    '500mg',
    'TABLET',
    'Dolor leve a moderado, fiebre',
    'Insuficiencia hepática severa',
    8.00,
    (SELECT id FROM pharmaceutical WHERE name = 'GlaxoSmithKline' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'Droguería Central' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'PARA500');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Amoxicilina 500mg',
    'AMOX500',
    'Amoxicilina',
    '500mg',
    'CAPSULE',
    'Infecciones bacterianas del tracto respiratorio, urinario, piel',
    'Alergia a penicilinas',
    25.00,
    (SELECT id FROM pharmaceutical WHERE name = 'Pfizer' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'MediDistribuciones' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'AMOX500');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Omeprazol 20mg',
    'OMEP20',
    'Omeprazol',
    '20mg',
    'CAPSULE',
    'Úlcera gástrica, reflujo gastroesofágico, gastritis',
    'Hipersensibilidad al omeprazol',
    18.00,
    (SELECT id FROM pharmaceutical WHERE name = 'AstraZeneca' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'Farmacias Galeno' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'OMEP20');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Loratadina 10mg',
    'LORA10',
    'Loratadina',
    '10mg',
    'TABLET',
    'Rinitis alérgica, urticaria',
    'Hipersensibilidad a loratadina',
    12.00,
    (SELECT id FROM pharmaceutical WHERE name = 'Sanofi' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'Droguería Central' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'LORA10');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Metformina 850mg',
    'METF850',
    'Metformina',
    '850mg',
    'TABLET',
    'Diabetes mellitus tipo 2',
    'Insuficiencia renal, cetoacidosis diabética',
    22.00,
    (SELECT id FROM pharmaceutical WHERE name = 'Merck' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'MediDistribuciones' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'METF850');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Atorvastatina 20mg',
    'ATOR20',
    'Atorvastatina',
    '20mg',
    'TABLET',
    'Hipercolesterolemia, prevención cardiovascular',
    'Enfermedad hepática activa, embarazo',
    35.00,
    (SELECT id FROM pharmaceutical WHERE name = 'Pfizer' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'FarmaExpress' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'ATOR20');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Losartán 50mg',
    'LOSA50',
    'Losartán',
    '50mg',
    'TABLET',
    'Hipertensión arterial, insuficiencia cardíaca',
    'Embarazo, hiperpotasemia severa',
    28.00,
    (SELECT id FROM pharmaceutical WHERE name = 'Merck' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'Salud Total' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'LOSA50');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Ciprofloxacino 500mg',
    'CIPRO500',
    'Ciprofloxacino',
    '500mg',
    'TABLET',
    'Infecciones urinarias, respiratorias, gastrointestinales',
    'Menores de 18 años, embarazo, lactancia',
    32.00,
    (SELECT id FROM pharmaceutical WHERE name = 'Bayer' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'Droguería Central' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'CIPRO500');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Diclofenaco 75mg/3ml',
    'DICLO75INJ',
    'Diclofenaco',
    '75mg/3ml',
    'INJECTABLE',
    'Dolor agudo, inflamación post-operatoria',
    'Úlcera gástrica activa, insuficiencia renal/hepática severa',
    45.00,
    (SELECT id FROM pharmaceutical WHERE name = 'Novartis' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'MediDistribuciones' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'DICLO75INJ');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Salbutamol Inhalador',
    'SALBU100',
    'Salbutamol',
    '100mcg/dosis',
    'INHALER',
    'Asma bronquial, broncoespasmo',
    'Hipersensibilidad a salbutamol',
    85.00,
    (SELECT id FROM pharmaceutical WHERE name = 'GlaxoSmithKline' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'FarmaExpress' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'SALBU100');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Amoxicilina Suspensión',
    'AMOX250SUS',
    'Amoxicilina',
    '250mg/5ml',
    'SUSPENSION',
    'Infecciones bacterianas en niños',
    'Alergia a penicilinas',
    35.00,
    (SELECT id FROM pharmaceutical WHERE name = 'Pfizer' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'Salud Total' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'AMOX250SUS');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Hidrocortisona Crema 1%',
    'HIDRO1CR',
    'Hidrocortisona',
    '1%',
    'CREAM',
    'Dermatitis, eczema, picaduras de insectos',
    'Infecciones cutáneas bacterianas o fúngicas',
    42.00,
    (SELECT id FROM pharmaceutical WHERE name = 'Johnson & Johnson' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'Farmacias Galeno' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'HIDRO1CR');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Clotrimazol Crema 1%',
    'CLOTRI1CR',
    'Clotrimazol',
    '1%',
    'CREAM',
    'Infecciones fúngicas de la piel',
    'Hipersensibilidad al clotrimazol',
    28.00,
    (SELECT id FROM pharmaceutical WHERE name = 'Bayer' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'Droguería Central' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'CLOTRI1CR');

INSERT INTO medication (name, code, active_ingredient, concentration, presentation, indications, contraindications, price, pharmaceutical_id, distributor_id)
SELECT
    'Gotas Oftálmicas Lubricantes',
    'LUBOFT',
    'Carboximetilcelulosa',
    '0.5%',
    'DROPS',
    'Ojo seco, irritación ocular',
    'Hipersensibilidad a los componentes',
    55.00,
    (SELECT id FROM pharmaceutical WHERE name = 'Abbott' LIMIT 1),
    (SELECT id FROM distributor WHERE name = 'FarmaExpress' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM medication WHERE code = 'LUBOFT');

-- =====================================================
-- FIN DE LA MIGRACIÓN V4
-- =====================================================
