-- Agregar campos de tiempos y tracking a medical_appointment
ALTER TABLE medical_appointment ADD COLUMN check_in_time TIMESTAMP;
ALTER TABLE medical_appointment ADD COLUMN start_time TIMESTAMP;
ALTER TABLE medical_appointment ADD COLUMN end_time TIMESTAMP;
ALTER TABLE medical_appointment ADD COLUMN cancellation_reason TEXT;
ALTER TABLE medical_appointment ADD COLUMN source VARCHAR(20) DEFAULT 'web';

-- Actualizar registros existentes con source = 'web'
UPDATE medical_appointment SET source = 'web' WHERE source IS NULL;

-- Agregar los mismos campos a la tabla de auditoría
ALTER TABLE medical_appointment_aud ADD COLUMN IF NOT EXISTS check_in_time TIMESTAMP;
ALTER TABLE medical_appointment_aud ADD COLUMN IF NOT EXISTS start_time TIMESTAMP;
ALTER TABLE medical_appointment_aud ADD COLUMN IF NOT EXISTS end_time TIMESTAMP;
ALTER TABLE medical_appointment_aud ADD COLUMN IF NOT EXISTS cancellation_reason TEXT;
ALTER TABLE medical_appointment_aud ADD COLUMN IF NOT EXISTS source VARCHAR(20);
