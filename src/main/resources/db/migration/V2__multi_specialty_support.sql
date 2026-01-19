-- =====================================================
-- CLINICXMANAGE - MIGRACIÓN V2
-- Soporte Multi-Especialidad y Mejoras Estructurales
-- Fecha: 2026-01-19
-- =====================================================

-- =====================================================
-- PARTE 1: TIPOS ENUMERADOS
-- =====================================================

-- Status de citas médicas
DO $$ BEGIN
    CREATE TYPE appointment_status AS ENUM (
        'scheduled',    -- Programada
        'confirmed',    -- Confirmada por paciente
        'in_progress',  -- En curso
        'completed',    -- Completada
        'cancelled',    -- Cancelada
        'no_show'       -- Paciente no se presentó
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Tipo de registro médico
DO $$ BEGIN
    CREATE TYPE medical_record_type AS ENUM (
        'consultation',     -- Consulta general
        'exam',             -- Examen
        'procedure',        -- Procedimiento
        'lab_result',       -- Resultado de laboratorio
        'imaging',          -- Imagen (rayos X, ultrasonido, etc.)
        'prescription',     -- Receta/Prescripción
        'follow_up',        -- Seguimiento
        'referral'          -- Referencia a otro especialista
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Género
DO $$ BEGIN
    CREATE TYPE gender_type AS ENUM ('male', 'female', 'other', 'prefer_not_to_say');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Tipo de sangre
DO $$ BEGIN
    CREATE TYPE blood_type AS ENUM (
        'A+', 'A-',
        'B+', 'B-',
        'AB+', 'AB-',
        'O+', 'O-',
        'unknown'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- =====================================================
-- PARTE 2: MODIFICACIONES A TABLA PATIENT
-- =====================================================

-- 2.1 Agregar birthdate y migrar desde age
ALTER TABLE patient ADD COLUMN IF NOT EXISTS birthdate DATE;
UPDATE patient SET birthdate = (CURRENT_DATE - (age || ' years')::INTERVAL)::DATE
WHERE birthdate IS NULL AND age IS NOT NULL;
ALTER TABLE patient ALTER COLUMN birthdate SET NOT NULL;
ALTER TABLE patient DROP COLUMN IF EXISTS age;

-- 2.2 Separar name en first_name y last_name
ALTER TABLE patient ADD COLUMN IF NOT EXISTS first_name VARCHAR(150);
ALTER TABLE patient ADD COLUMN IF NOT EXISTS last_name VARCHAR(150);
UPDATE patient SET
    first_name = SPLIT_PART(name, ' ', 1),
    last_name = SUBSTRING(name FROM POSITION(' ' IN name) + 1)
WHERE first_name IS NULL AND name IS NOT NULL;
ALTER TABLE patient ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE patient ALTER COLUMN last_name SET NOT NULL;
ALTER TABLE patient DROP COLUMN IF EXISTS name;

-- 2.3 Agregar campos adicionales
ALTER TABLE patient ADD COLUMN IF NOT EXISTS gender gender_type;
ALTER TABLE patient ADD COLUMN IF NOT EXISTS blood_group blood_type DEFAULT 'unknown';
ALTER TABLE patient ADD COLUMN IF NOT EXISTS email VARCHAR(150);
ALTER TABLE patient ADD COLUMN IF NOT EXISTS emergency_contact_name VARCHAR(200);
ALTER TABLE patient ADD COLUMN IF NOT EXISTS emergency_contact_phone VARCHAR(20);
ALTER TABLE patient ADD COLUMN IF NOT EXISTS allergies TEXT;
ALTER TABLE patient ADD COLUMN IF NOT EXISTS chronic_conditions TEXT;
ALTER TABLE patient ADD COLUMN IF NOT EXISTS insurance_provider VARCHAR(150);
ALTER TABLE patient ADD COLUMN IF NOT EXISTS insurance_number VARCHAR(50);

-- 2.4 Constraint de validación de teléfono
DO $$ BEGIN
    ALTER TABLE patient ADD CONSTRAINT patient_phone_format
    CHECK (phone IS NULL OR phone ~ '^[0-9\s\-\+\(\)]+$');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- =====================================================
-- PARTE 3: MODIFICACIONES A TABLA MEDICAL_APPOINTMENT
-- =====================================================

-- 3.1 Agregar status
ALTER TABLE medical_appointment
ADD COLUMN IF NOT EXISTS status appointment_status NOT NULL DEFAULT 'scheduled';

-- 3.2 Agregar specialty_id
ALTER TABLE medical_appointment ADD COLUMN IF NOT EXISTS specialty_id UUID;
DO $$ BEGIN
    ALTER TABLE medical_appointment
    ADD CONSTRAINT medical_appointment_specialty_id_fkey
    FOREIGN KEY (specialty_id) REFERENCES specialty(id) ON DELETE SET NULL;
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 3.3 Agregar campos para la cita
ALTER TABLE medical_appointment ADD COLUMN IF NOT EXISTS reason TEXT;
ALTER TABLE medical_appointment ADD COLUMN IF NOT EXISTS diagnosis TEXT;
ALTER TABLE medical_appointment ADD COLUMN IF NOT EXISTS notes TEXT;

-- 3.4 Eliminar columnas legacy (si existen)
ALTER TABLE medical_appointment DROP CONSTRAINT IF EXISTS medical_appointment_med_hist_gyneco_id_fkey;
ALTER TABLE medical_appointment DROP COLUMN IF EXISTS med_hist_gyneco_id;
ALTER TABLE medical_appointment DROP COLUMN IF EXISTS medical_history;
ALTER TABLE medical_appointment DROP COLUMN IF EXISTS observation;

-- =====================================================
-- PARTE 4: NUEVAS TABLAS
-- =====================================================

-- 4.1 Tabla medical_record (genérica para todas las especialidades)
CREATE TABLE IF NOT EXISTS medical_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    appointment_id UUID,
    specialty_id UUID,
    doctor_id UUID NOT NULL,
    record_type medical_record_type NOT NULL DEFAULT 'consultation',

    -- Datos clínicos comunes
    chief_complaint TEXT,           -- Motivo principal de consulta
    present_illness TEXT,           -- Historia de enfermedad actual
    physical_exam TEXT,             -- Examen físico
    diagnosis TEXT,                 -- Diagnóstico
    treatment_plan TEXT,            -- Plan de tratamiento

    -- Signos vitales (comunes a todas las especialidades)
    vital_signs JSONB,              -- {blood_pressure, heart_rate, temperature, weight, height, etc.}

    -- Datos específicos por especialidad (flexible)
    specialty_data JSONB,           -- Datos específicos según el template de la especialidad

    -- Archivos adjuntos
    attachments JSONB,              -- [{name, url, type, size}]

    -- Auditoría
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Foreign Keys
    CONSTRAINT fk_medical_record_patient
        FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE,
    CONSTRAINT fk_medical_record_appointment
        FOREIGN KEY (appointment_id) REFERENCES medical_appointment(id) ON DELETE SET NULL,
    CONSTRAINT fk_medical_record_specialty
        FOREIGN KEY (specialty_id) REFERENCES specialty(id) ON DELETE SET NULL,
    CONSTRAINT fk_medical_record_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE RESTRICT
);

-- 4.2 Tabla specialty_form_template (define estructura de datos por especialidad)
CREATE TABLE IF NOT EXISTS specialty_form_template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    specialty_id UUID NOT NULL,
    form_name VARCHAR(100) NOT NULL,
    description TEXT,
    form_schema JSONB NOT NULL,     -- Define estructura del formulario
    is_active BOOLEAN DEFAULT true,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_template_specialty
        FOREIGN KEY (specialty_id) REFERENCES specialty(id) ON DELETE CASCADE,
    CONSTRAINT unique_specialty_form
        UNIQUE (specialty_id, form_name, version)
);

-- 4.3 Tabla prescription (recetas médicas)
CREATE TABLE IF NOT EXISTS prescription (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medical_record_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,

    -- Detalles de prescripción
    medications JSONB NOT NULL,     -- [{name, dosage, frequency, duration, instructions}]
    notes TEXT,

    -- Vigencia
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expiry_date DATE,

    -- Auditoría
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prescription_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_record(id) ON DELETE CASCADE,
    CONSTRAINT fk_prescription_patient
        FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE,
    CONSTRAINT fk_prescription_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE RESTRICT
);

-- =====================================================
-- PARTE 5: MIGRAR Y ELIMINAR TABLA LEGACY
-- =====================================================

-- 5.1 Migrar datos de medical_hist_gyneco_obstetric a medical_record (si existen)
INSERT INTO medical_record (
    patient_id,
    specialty_id,
    doctor_id,
    record_type,
    specialty_data,
    created_at
)
SELECT
    mhgo.patient_id,
    (SELECT id FROM specialty WHERE name ILIKE '%ginec%' LIMIT 1),
    (SELECT doctor_id FROM medical_appointment WHERE patient_id = mhgo.patient_id ORDER BY created_at DESC LIMIT 1),
    'consultation'::medical_record_type,
    jsonb_build_object(
        'last_menstrual_period', mhgo.last_menstrual_period,
        'weight', mhgo.weight,
        'height', mhgo.height,
        'duration', mhgo.duration,
        'cycles', mhgo.cycles,
        'reliable', mhgo.reliable,
        'papanicolaou', mhgo.papanicolaou,
        'medical_history_type', mhgo.medical_history_type
    ),
    mhgo.created_at
FROM medical_hist_gyneco_obstetric mhgo
WHERE EXISTS (SELECT 1 FROM medical_hist_gyneco_obstetric)
ON CONFLICT DO NOTHING;

-- 5.2 Eliminar tabla legacy
DROP TABLE IF EXISTS medical_hist_gyneco_obstetric CASCADE;

-- =====================================================
-- PARTE 6: CONSTRAINTS ADICIONALES
-- =====================================================

-- 6.1 UNIQUE en doctor.email
DO $$ BEGIN
    ALTER TABLE doctor ADD CONSTRAINT doctor_email_unique UNIQUE (email);
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 6.2 UNIQUE en specialty.name
DO $$ BEGIN
    ALTER TABLE specialty ADD CONSTRAINT specialty_name_unique UNIQUE (name);
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 6.3 UNIQUE en patient.email (parcial, solo donde no es null)
CREATE UNIQUE INDEX IF NOT EXISTS idx_patient_email_unique
ON patient(email) WHERE email IS NOT NULL;

-- 6.4 Validación de teléfono en doctor
DO $$ BEGIN
    ALTER TABLE doctor ADD CONSTRAINT doctor_phone_format
    CHECK (phone IS NULL OR phone ~ '^[0-9\s\-\+\(\)]+$');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- =====================================================
-- PARTE 7: ÍNDICES DE RENDIMIENTO
-- =====================================================

-- Índices para medical_appointment
CREATE INDEX IF NOT EXISTS idx_appointment_patient ON medical_appointment(patient_id);
CREATE INDEX IF NOT EXISTS idx_appointment_doctor ON medical_appointment(doctor_id);
CREATE INDEX IF NOT EXISTS idx_appointment_clinic ON medical_appointment(clinic_id);
CREATE INDEX IF NOT EXISTS idx_appointment_specialty ON medical_appointment(specialty_id);
CREATE INDEX IF NOT EXISTS idx_appointment_date ON medical_appointment(appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointment_status ON medical_appointment(status);
CREATE INDEX IF NOT EXISTS idx_appointment_date_status ON medical_appointment(appointment_date, status);

-- Índices para medical_record
CREATE INDEX IF NOT EXISTS idx_record_patient ON medical_record(patient_id);
CREATE INDEX IF NOT EXISTS idx_record_appointment ON medical_record(appointment_id);
CREATE INDEX IF NOT EXISTS idx_record_doctor ON medical_record(doctor_id);
CREATE INDEX IF NOT EXISTS idx_record_specialty ON medical_record(specialty_id);
CREATE INDEX IF NOT EXISTS idx_record_type ON medical_record(record_type);
CREATE INDEX IF NOT EXISTS idx_record_created ON medical_record(created_at);
-- Índice GIN para búsquedas en JSONB
CREATE INDEX IF NOT EXISTS idx_record_specialty_data ON medical_record USING GIN (specialty_data);
CREATE INDEX IF NOT EXISTS idx_record_vital_signs ON medical_record USING GIN (vital_signs);

-- Índices para medical_history_pathological_fam (antecedentes familiares)
CREATE INDEX IF NOT EXISTS idx_pathological_patient ON medical_history_pathological_fam(patient_id);

-- Índices para prescription
CREATE INDEX IF NOT EXISTS idx_prescription_patient ON prescription(patient_id);
CREATE INDEX IF NOT EXISTS idx_prescription_doctor ON prescription(doctor_id);
CREATE INDEX IF NOT EXISTS idx_prescription_record ON prescription(medical_record_id);
CREATE INDEX IF NOT EXISTS idx_prescription_date ON prescription(issue_date);

-- Índices para doctor_clinic
CREATE INDEX IF NOT EXISTS idx_doctor_clinic_doctor ON doctor_clinic(doctor_id);
CREATE INDEX IF NOT EXISTS idx_doctor_clinic_clinic ON doctor_clinic(clinic_id);
CREATE INDEX IF NOT EXISTS idx_doctor_clinic_active ON doctor_clinic(active);

-- Índices para doctor_specialty
CREATE INDEX IF NOT EXISTS idx_doctor_specialty_doctor ON doctor_specialty(doctor_id);
CREATE INDEX IF NOT EXISTS idx_doctor_specialty_specialty ON doctor_specialty(specialty_id);

-- Índices para patient
CREATE INDEX IF NOT EXISTS idx_patient_name ON patient(first_name, last_name);
CREATE INDEX IF NOT EXISTS idx_patient_email ON patient(email);
CREATE INDEX IF NOT EXISTS idx_patient_phone ON patient(phone);

-- Índices para specialty_form_template
CREATE INDEX IF NOT EXISTS idx_template_specialty ON specialty_form_template(specialty_id);
CREATE INDEX IF NOT EXISTS idx_template_active ON specialty_form_template(is_active);

-- =====================================================
-- PARTE 8: DATOS INICIALES - TEMPLATES DE ESPECIALIDADES
-- =====================================================

-- Template para Ginecología
INSERT INTO specialty_form_template (specialty_id, form_name, description, form_schema)
SELECT
    s.id,
    'Historial Gineco-Obstétrico',
    'Formulario para historial ginecológico y obstétrico',
    '{
        "fields": [
            {"name": "last_menstrual_period", "type": "date", "label": "Fecha última menstruación", "required": false},
            {"name": "weight", "type": "number", "label": "Peso (kg)", "required": true, "min": 0, "max": 500},
            {"name": "height", "type": "number", "label": "Altura (cm)", "required": true, "min": 0, "max": 300},
            {"name": "duration", "type": "number", "label": "Duración del ciclo (días)", "required": false},
            {"name": "cycles", "type": "number", "label": "Ciclos", "required": true},
            {"name": "reliable", "type": "select", "label": "Ciclo confiable", "options": ["Sí", "No"], "required": false},
            {"name": "papanicolaou", "type": "text", "label": "Último Papanicolaou", "required": false},
            {"name": "pregnancies", "type": "number", "label": "Gestas", "required": false},
            {"name": "births", "type": "number", "label": "Partos", "required": false},
            {"name": "cesareans", "type": "number", "label": "Cesáreas", "required": false},
            {"name": "abortions", "type": "number", "label": "Abortos", "required": false},
            {"name": "contraceptive_method", "type": "text", "label": "Método anticonceptivo", "required": false}
        ]
    }'::jsonb
FROM specialty s
WHERE s.name ILIKE '%ginec%' OR s.name ILIKE '%obstet%'
ON CONFLICT (specialty_id, form_name, version) DO NOTHING;

-- Template para Medicina General
INSERT INTO specialty_form_template (specialty_id, form_name, description, form_schema)
SELECT
    s.id,
    'Consulta General',
    'Formulario para consulta de medicina general',
    '{
        "fields": [
            {"name": "symptoms", "type": "textarea", "label": "Síntomas principales", "required": true},
            {"name": "symptom_duration", "type": "text", "label": "Duración de síntomas", "required": false},
            {"name": "allergies_noted", "type": "textarea", "label": "Alergias observadas", "required": false},
            {"name": "current_medications", "type": "textarea", "label": "Medicamentos actuales", "required": false},
            {"name": "family_history", "type": "textarea", "label": "Antecedentes familiares relevantes", "required": false}
        ]
    }'::jsonb
FROM specialty s
WHERE s.name ILIKE '%general%' OR s.name ILIKE '%medicina%'
ON CONFLICT (specialty_id, form_name, version) DO NOTHING;

-- Template para Cardiología
INSERT INTO specialty_form_template (specialty_id, form_name, description, form_schema)
SELECT
    s.id,
    'Evaluación Cardiovascular',
    'Formulario para evaluación cardiovascular',
    '{
        "fields": [
            {"name": "blood_pressure_systolic", "type": "number", "label": "Presión sistólica (mmHg)", "required": true},
            {"name": "blood_pressure_diastolic", "type": "number", "label": "Presión diastólica (mmHg)", "required": true},
            {"name": "heart_rate", "type": "number", "label": "Frecuencia cardíaca (lpm)", "required": true},
            {"name": "chest_pain", "type": "boolean", "label": "Presenta dolor torácico", "required": false},
            {"name": "chest_pain_description", "type": "textarea", "label": "Descripción del dolor", "required": false},
            {"name": "shortness_of_breath", "type": "boolean", "label": "Presenta disnea", "required": false},
            {"name": "palpitations", "type": "boolean", "label": "Presenta palpitaciones", "required": false},
            {"name": "edema", "type": "boolean", "label": "Presenta edema", "required": false},
            {"name": "ecg_findings", "type": "textarea", "label": "Hallazgos ECG", "required": false},
            {"name": "cardiac_history", "type": "textarea", "label": "Antecedentes cardíacos", "required": false},
            {"name": "risk_factors", "type": "multiselect", "label": "Factores de riesgo", "options": ["Hipertensión", "Diabetes", "Tabaquismo", "Obesidad", "Sedentarismo", "Dislipidemia", "Historia familiar"], "required": false}
        ]
    }'::jsonb
FROM specialty s
WHERE s.name ILIKE '%cardio%'
ON CONFLICT (specialty_id, form_name, version) DO NOTHING;

-- Template para Pediatría
INSERT INTO specialty_form_template (specialty_id, form_name, description, form_schema)
SELECT
    s.id,
    'Control Pediátrico',
    'Formulario para control pediátrico',
    '{
        "fields": [
            {"name": "weight", "type": "number", "label": "Peso (kg)", "required": true},
            {"name": "height", "type": "number", "label": "Talla (cm)", "required": true},
            {"name": "head_circumference", "type": "number", "label": "Perímetro cefálico (cm)", "required": false},
            {"name": "weight_percentile", "type": "number", "label": "Percentil peso", "required": false},
            {"name": "height_percentile", "type": "number", "label": "Percentil talla", "required": false},
            {"name": "feeding_type", "type": "select", "label": "Tipo de alimentación", "options": ["Lactancia materna exclusiva", "Fórmula", "Mixta", "Alimentación complementaria"], "required": false},
            {"name": "vaccinations_up_to_date", "type": "boolean", "label": "Vacunas al día", "required": false},
            {"name": "development_milestones", "type": "textarea", "label": "Hitos del desarrollo", "required": false},
            {"name": "concerns", "type": "textarea", "label": "Preocupaciones de los padres", "required": false}
        ]
    }'::jsonb
FROM specialty s
WHERE s.name ILIKE '%pediatr%'
ON CONFLICT (specialty_id, form_name, version) DO NOTHING;

-- =====================================================
-- PARTE 9: COMENTARIOS EN TABLAS Y COLUMNAS
-- =====================================================

COMMENT ON TABLE medical_record IS 'Registro médico genérico para todas las especialidades. Los datos específicos de cada especialidad se almacenan en specialty_data según el template definido en specialty_form_template.';
COMMENT ON COLUMN medical_record.specialty_data IS 'Datos específicos de la especialidad en formato JSON, validados contra el schema de specialty_form_template';
COMMENT ON COLUMN medical_record.vital_signs IS 'Signos vitales: {blood_pressure: {systolic, diastolic}, heart_rate, temperature, weight, height, respiratory_rate, oxygen_saturation}';

COMMENT ON TABLE specialty_form_template IS 'Define la estructura de formularios por especialidad. El campo form_schema contiene la definición de campos esperados.';
COMMENT ON COLUMN specialty_form_template.form_schema IS 'Esquema JSON que define los campos: [{name, type, label, required, options, min, max, ...}]';

COMMENT ON TABLE prescription IS 'Recetas/Prescripciones médicas vinculadas a un registro médico';
COMMENT ON COLUMN prescription.medications IS 'Lista de medicamentos: [{name, dosage, frequency, duration, instructions, quantity}]';

COMMENT ON COLUMN medical_appointment.status IS 'Estado de la cita: scheduled (programada), confirmed (confirmada), in_progress (en curso), completed (completada), cancelled (cancelada), no_show (no se presentó)';
COMMENT ON COLUMN medical_appointment.specialty_id IS 'Especialidad de la cita - permite filtrar y validar que el doctor tenga esta especialidad';
COMMENT ON COLUMN medical_appointment.reason IS 'Motivo de la consulta indicado por el paciente al agendar';
COMMENT ON COLUMN medical_appointment.diagnosis IS 'Diagnóstico resumido de la cita';
COMMENT ON COLUMN medical_appointment.notes IS 'Notas adicionales sobre la cita';

COMMENT ON COLUMN patient.blood_group IS 'Tipo de sangre del paciente';
COMMENT ON COLUMN patient.allergies IS 'Alergias conocidas del paciente (texto libre)';
COMMENT ON COLUMN patient.chronic_conditions IS 'Condiciones crónicas del paciente (texto libre)';

COMMENT ON TABLE medical_history_pathological_fam IS 'Antecedentes patológicos familiares del paciente (diabetes, hipertensión, cáncer, etc. en la familia)';

-- =====================================================
-- FIN DE LA MIGRACIÓN V2
-- =====================================================
