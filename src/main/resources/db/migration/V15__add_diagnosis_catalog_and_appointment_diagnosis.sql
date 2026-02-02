-- Catálogo CIE-10
CREATE TABLE diagnosis_catalog (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(500) NOT NULL,
    category VARCHAR(255),
    chapter VARCHAR(255),
    created_at TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_diagnosis_catalog_code ON diagnosis_catalog(code);
CREATE INDEX idx_diagnosis_catalog_name ON diagnosis_catalog(LOWER(name));

-- Diagnósticos por cita
CREATE TABLE appointment_diagnosis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID NOT NULL REFERENCES medical_appointment(id),
    diagnosis_id UUID NOT NULL REFERENCES diagnosis_catalog(id),
    type VARCHAR(20) NOT NULL DEFAULT 'principal',
    notes TEXT,
    created_at TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_appointment_diagnosis_appointment ON appointment_diagnosis(appointment_id);
CREATE INDEX idx_appointment_diagnosis_diagnosis ON appointment_diagnosis(diagnosis_id);

-- Auditoría (Envers)
CREATE TABLE appointment_diagnosis_aud (
    id UUID,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    appointment_id UUID,
    diagnosis_id UUID,
    type VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP,
    PRIMARY KEY (id, rev)
);
