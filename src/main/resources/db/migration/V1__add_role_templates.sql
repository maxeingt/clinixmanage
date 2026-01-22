-- =============================================
-- Migración: Sistema de plantillas de roles
-- =============================================

-- 1. Crear tabla role_template
CREATE TABLE IF NOT EXISTS role_template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    admin_patients BOOLEAN DEFAULT FALSE,
    admin_doctors BOOLEAN DEFAULT FALSE,
    admin_appointments BOOLEAN DEFAULT FALSE,
    admin_clinics BOOLEAN DEFAULT FALSE,
    admin_users BOOLEAN DEFAULT FALSE,
    admin_specialties BOOLEAN DEFAULT FALSE,
    manage_assignments BOOLEAN DEFAULT FALSE,
    view_medical_records BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 2. Agregar nuevas columnas a user_clinic_permission
ALTER TABLE user_clinic_permission
ADD COLUMN IF NOT EXISTS view_medical_records BOOLEAN DEFAULT FALSE;

ALTER TABLE user_clinic_permission
ADD COLUMN IF NOT EXISTS role_template_id UUID;

-- 3. Agregar FK a role_template
ALTER TABLE user_clinic_permission
ADD CONSTRAINT fk_user_clinic_permission_role_template
FOREIGN KEY (role_template_id) REFERENCES role_template(id);

-- 4. Insertar roles por defecto
INSERT INTO role_template (id, name, description, admin_patients, admin_doctors, admin_appointments, admin_clinics, admin_users, admin_specialties, manage_assignments, view_medical_records, created_at)
VALUES
    (gen_random_uuid(), 'ADMIN', 'Administrador del sistema', TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'DOCTOR', 'Médico', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'SECRETARY', 'Secretaria/Recepcionista', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- 5. Índice para búsquedas por nombre de rol
CREATE INDEX IF NOT EXISTS idx_role_template_name ON role_template(name);

-- 6. Índice para búsquedas de permisos por rol
CREATE INDEX IF NOT EXISTS idx_user_clinic_permission_role ON user_clinic_permission(role_template_id);
