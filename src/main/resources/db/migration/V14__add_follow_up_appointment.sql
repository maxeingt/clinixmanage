ALTER TABLE medical_appointment ADD COLUMN follow_up_appointment_id UUID REFERENCES medical_appointment(id);

-- Auditoría (Envers)
ALTER TABLE medical_appointment_aud ADD COLUMN IF NOT EXISTS follow_up_appointment_id UUID;
