CREATE TABLE lab_order (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    appointment_id UUID REFERENCES medical_appointment(id),
    patient_id UUID NOT NULL REFERENCES patient(id),
    doctor_id UUID NOT NULL REFERENCES doctor(id),
    order_date TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE lab_result (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    lab_order_id UUID NOT NULL REFERENCES lab_order(id) ON DELETE CASCADE,
    test_name VARCHAR(255) NOT NULL,
    test_code VARCHAR(50),
    value VARCHAR(255),
    unit VARCHAR(50),
    reference_min NUMERIC(10,4),
    reference_max NUMERIC(10,4),
    is_abnormal BOOLEAN DEFAULT FALSE,
    result_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);
