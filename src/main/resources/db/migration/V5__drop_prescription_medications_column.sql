-- =====================================================
-- CLINICXMANAGE - MIGRACIÓN V5
-- Eliminar columna sobrante 'medications' de prescription
-- La relación se maneja en la tabla prescription_medication
-- Fecha: 2026-01-26
-- =====================================================

ALTER TABLE prescription DROP COLUMN IF EXISTS medications;
