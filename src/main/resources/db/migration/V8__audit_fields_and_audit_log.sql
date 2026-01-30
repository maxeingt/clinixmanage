-- =============================================
-- Fase 1: Auditoría con Hibernate Envers
-- =============================================

-- 1. Agregar created_by / updated_by a tablas transaccionales
ALTER TABLE medical_appointment ADD COLUMN created_by UUID REFERENCES "user"(id);
ALTER TABLE medical_appointment ADD COLUMN updated_by UUID REFERENCES "user"(id);

ALTER TABLE patient ADD COLUMN created_by UUID REFERENCES "user"(id);
ALTER TABLE patient ADD COLUMN updated_by UUID REFERENCES "user"(id);

ALTER TABLE prescription ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE prescription ADD COLUMN updated_by UUID REFERENCES "user"(id);
ALTER TABLE prescription ADD COLUMN created_by UUID REFERENCES "user"(id);

ALTER TABLE medical_history_pathological_fam ADD COLUMN created_by UUID REFERENCES "user"(id);
ALTER TABLE medical_history_pathological_fam ADD COLUMN updated_by UUID REFERENCES "user"(id);

ALTER TABLE clinic ADD COLUMN created_by UUID REFERENCES "user"(id);
ALTER TABLE clinic ADD COLUMN updated_by UUID REFERENCES "user"(id);

ALTER TABLE specialty ADD COLUMN created_by UUID REFERENCES "user"(id);
ALTER TABLE specialty ADD COLUMN updated_by UUID REFERENCES "user"(id);

ALTER TABLE medication ADD COLUMN created_by UUID REFERENCES "user"(id);
ALTER TABLE medication ADD COLUMN updated_by UUID REFERENCES "user"(id);

-- 2. Tabla de revisiones de Envers
CREATE TABLE REVINFO (
    REV INTEGER NOT NULL PRIMARY KEY,
    REVTSTMP BIGINT
);

CREATE SEQUENCE REVINFO_SEQ START WITH 1 INCREMENT BY 50;

-- 3. Tablas AUD para entidades transaccionales
CREATE TABLE medical_appointment_AUD (
    id UUID NOT NULL,
    REV INTEGER NOT NULL REFERENCES REVINFO(REV),
    REVTYPE SMALLINT,
    patient_id UUID,
    doctor_id UUID,
    clinic_id UUID,
    specialty_id UUID,
    appointment_date TIMESTAMP,
    status VARCHAR(255),
    reason TEXT,
    diagnosis TEXT,
    notes TEXT,
    notified_30_min BOOLEAN,
    notified_10_min BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    PRIMARY KEY (id, REV)
);

CREATE TABLE medical_record_AUD (
    id UUID NOT NULL,
    REV INTEGER NOT NULL REFERENCES REVINFO(REV),
    REVTYPE SMALLINT,
    patient_id UUID,
    appointment_id UUID,
    specialty_id UUID,
    doctor_id UUID,
    record_type VARCHAR(255),
    chief_complaint TEXT,
    present_illness TEXT,
    physical_exam TEXT,
    diagnosis TEXT,
    treatment_plan TEXT,
    vital_signs JSONB,
    specialty_data JSONB,
    -- attachments excluido: campo pesado, no relevante para auditoría
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    PRIMARY KEY (id, REV)
);

CREATE TABLE prescription_AUD (
    id UUID NOT NULL,
    REV INTEGER NOT NULL REFERENCES REVINFO(REV),
    REVTYPE SMALLINT,
    medical_record_id UUID,
    patient_id UUID,
    doctor_id UUID,
    notes TEXT,
    issue_date DATE,
    expiry_date DATE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    PRIMARY KEY (id, REV)
);

CREATE TABLE prescription_medication_AUD (
    prescription_id UUID NOT NULL,
    medication_id UUID NOT NULL,
    REV INTEGER NOT NULL REFERENCES REVINFO(REV),
    REVTYPE SMALLINT,
    dose VARCHAR(255),
    frequency VARCHAR(255),
    duration VARCHAR(255),
    quantity INTEGER,
    administration_route VARCHAR(255),
    specific_indications TEXT,
    created_at TIMESTAMP,
    PRIMARY KEY (prescription_id, medication_id, REV)
);

CREATE TABLE patient_AUD (
    id UUID NOT NULL,
    REV INTEGER NOT NULL REFERENCES REVINFO(REV),
    REVTYPE SMALLINT,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    birthdate DATE,
    gender VARCHAR(255),
    blood_group VARCHAR(255),
    phone VARCHAR(255),
    address VARCHAR(255),
    email VARCHAR(255),
    marital_status VARCHAR(255),
    occupation VARCHAR(255),
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(255),
    allergies TEXT,
    chronic_conditions TEXT,
    insurance_provider VARCHAR(255),
    insurance_number VARCHAR(255),
    has_pathological_history BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    PRIMARY KEY (id, REV)
);

CREATE TABLE doctor_AUD (
    id UUID NOT NULL,
    REV INTEGER NOT NULL REFERENCES REVINFO(REV),
    REVTYPE SMALLINT,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    birthdate DATE,
    address VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    user_id UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by TEXT,
    updated_by TEXT,
    PRIMARY KEY (id, REV)
);

CREATE TABLE doctor_clinic_AUD (
    doctor_id UUID NOT NULL,
    clinic_id UUID NOT NULL,
    REV INTEGER NOT NULL REFERENCES REVINFO(REV),
    REVTYPE SMALLINT,
    assigned_at TIMESTAMP,
    unassigned_at TIMESTAMP,
    active BOOLEAN,
    PRIMARY KEY (doctor_id, clinic_id, REV)
);

CREATE TABLE clinic_AUD (
    id UUID NOT NULL,
    REV INTEGER NOT NULL REFERENCES REVINFO(REV),
    REVTYPE SMALLINT,
    name VARCHAR(255),
    address VARCHAR(255),
    phone VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    PRIMARY KEY (id, REV)
);

CREATE TABLE medication_AUD (
    id UUID NOT NULL,
    REV INTEGER NOT NULL REFERENCES REVINFO(REV),
    REVTYPE SMALLINT,
    name VARCHAR(255),
    description TEXT,
    code VARCHAR(255),
    active_ingredient VARCHAR(255),
    concentration VARCHAR(255),
    presentation VARCHAR(255),
    indications TEXT,
    contraindications TEXT,
    price NUMERIC,
    active BOOLEAN,
    pharmaceutical_id UUID,
    distributor_id UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    PRIMARY KEY (id, REV)
);

-- 4. Índices para consultas de auditoría
CREATE INDEX idx_medical_appointment_aud_rev ON medical_appointment_AUD(REV);
CREATE INDEX idx_medical_record_aud_rev ON medical_record_AUD(REV);
CREATE INDEX idx_prescription_aud_rev ON prescription_AUD(REV);
CREATE INDEX idx_patient_aud_rev ON patient_AUD(REV);
CREATE INDEX idx_doctor_aud_rev ON doctor_AUD(REV);
CREATE INDEX idx_medication_aud_rev ON medication_AUD(REV);
