-- Agregar columna bandera para indicar si el paciente tiene historial patológico cargado
ALTER TABLE patient ADD COLUMN IF NOT EXISTS has_pathological_history BOOLEAN NOT NULL DEFAULT FALSE;

-- Actualizar pacientes existentes que ya tienen historial patológico
UPDATE patient p
SET has_pathological_history = TRUE
WHERE EXISTS (
    SELECT 1 FROM medical_history_pathological_fam m WHERE m.patient_id = p.id
);
