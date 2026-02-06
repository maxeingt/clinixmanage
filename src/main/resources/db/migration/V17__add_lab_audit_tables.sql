-- Auditoría (Envers) para lab_order y lab_result

CREATE TABLE lab_order_AUD (
    id UUID NOT NULL,
    REV INTEGER NOT NULL REFERENCES REVINFO(REV),
    REVTYPE SMALLINT,
    appointment_id UUID,
    patient_id UUID,
    doctor_id UUID,
    order_date TIMESTAMP,
    status VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (id, REV)
);

CREATE TABLE lab_result_AUD (
    id UUID NOT NULL,
    REV INTEGER NOT NULL REFERENCES REVINFO(REV),
    REVTYPE SMALLINT,
    lab_order_id UUID,
    test_name VARCHAR(255),
    test_code VARCHAR(50),
    value VARCHAR(255),
    unit VARCHAR(50),
    reference_min NUMERIC(10,4),
    reference_max NUMERIC(10,4),
    is_abnormal BOOLEAN,
    result_date TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (id, REV)
);

CREATE INDEX idx_lab_order_aud_rev ON lab_order_AUD(REV);
CREATE INDEX idx_lab_result_aud_rev ON lab_result_AUD(REV);
