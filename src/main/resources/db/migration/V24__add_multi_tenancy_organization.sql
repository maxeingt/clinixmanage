-- ============================================================
-- V24: Multi-tenancy - Agregar tabla organization y organization_id
-- ============================================================

-- 1. Crear tabla organization
CREATE TABLE organization (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,
    legal_name VARCHAR(250),
    tax_id VARCHAR(20),
    email VARCHAR(150),
    phone VARCHAR(20),
    address VARCHAR(250),
    logo_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT true,
    subscription_plan VARCHAR(20) DEFAULT 'basic',
    max_users INTEGER DEFAULT 10,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- 2. Insertar organización default para datos existentes
INSERT INTO organization (id, name, slug, active)
VALUES ('00000000-0000-0000-0000-000000000001', 'Default', 'default', true);

-- ============================================================
-- 3. Agregar organization_id a tablas scoped (15 tablas)
-- ============================================================

-- clinic
ALTER TABLE clinic ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE clinic SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE clinic ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE clinic ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_clinic_org ON clinic(organization_id);

-- doctor
ALTER TABLE doctor ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE doctor SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE doctor ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE doctor ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_doctor_org ON doctor(organization_id);

-- "user"
ALTER TABLE "user" ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE "user" SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE "user" ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE "user" ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_user_org ON "user"(organization_id);

-- patient
ALTER TABLE patient ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE patient SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE patient ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE patient ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_patient_org ON patient(organization_id);

-- medical_appointment
ALTER TABLE medical_appointment ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE medical_appointment SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE medical_appointment ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE medical_appointment ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_medical_appointment_org ON medical_appointment(organization_id);

-- medical_record
ALTER TABLE medical_record ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE medical_record SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE medical_record ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE medical_record ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_medical_record_org ON medical_record(organization_id);

-- prescription
ALTER TABLE prescription ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE prescription SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE prescription ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE prescription ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_prescription_org ON prescription(organization_id);

-- lab_order
ALTER TABLE lab_order ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE lab_order SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE lab_order ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE lab_order ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_lab_order_org ON lab_order(organization_id);

-- lab_result
ALTER TABLE lab_result ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE lab_result SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE lab_result ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE lab_result ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_lab_result_org ON lab_result(organization_id);

-- lab_order_attachment
ALTER TABLE lab_order_attachment ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE lab_order_attachment SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE lab_order_attachment ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE lab_order_attachment ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_lab_order_attachment_org ON lab_order_attachment(organization_id);

-- medical_history_pathological_fam
ALTER TABLE medical_history_pathological_fam ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE medical_history_pathological_fam SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE medical_history_pathological_fam ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE medical_history_pathological_fam ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_medical_history_pathological_fam_org ON medical_history_pathological_fam(organization_id);

-- doctor_clinic
ALTER TABLE doctor_clinic ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE doctor_clinic SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE doctor_clinic ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE doctor_clinic ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_doctor_clinic_org ON doctor_clinic(organization_id);

-- doctor_specialty
ALTER TABLE doctor_specialty ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE doctor_specialty SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE doctor_specialty ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE doctor_specialty ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_doctor_specialty_org ON doctor_specialty(organization_id);

-- dashboard_widget_config
ALTER TABLE dashboard_widget_config ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE dashboard_widget_config SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE dashboard_widget_config ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE dashboard_widget_config ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_dashboard_widget_config_org ON dashboard_widget_config(organization_id);

-- specialty_form_template
ALTER TABLE specialty_form_template ADD COLUMN organization_id UUID DEFAULT '00000000-0000-0000-0000-000000000001' REFERENCES organization(id);
UPDATE specialty_form_template SET organization_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE specialty_form_template ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE specialty_form_template ALTER COLUMN organization_id DROP DEFAULT;
CREATE INDEX idx_specialty_form_template_org ON specialty_form_template(organization_id);

-- ============================================================
-- 4. Agregar organization_id a tablas de auditoría (_aud)
-- ============================================================

ALTER TABLE clinic_aud ADD COLUMN organization_id UUID;
UPDATE clinic_aud SET organization_id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE doctor_aud ADD COLUMN organization_id UUID;
UPDATE doctor_aud SET organization_id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE patient_aud ADD COLUMN organization_id UUID;
UPDATE patient_aud SET organization_id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE medical_appointment_aud ADD COLUMN organization_id UUID;
UPDATE medical_appointment_aud SET organization_id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE medical_record_aud ADD COLUMN organization_id UUID;
UPDATE medical_record_aud SET organization_id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE prescription_aud ADD COLUMN organization_id UUID;
UPDATE prescription_aud SET organization_id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE prescription_medication_aud ADD COLUMN organization_id UUID;
UPDATE prescription_medication_aud SET organization_id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE lab_order_aud ADD COLUMN organization_id UUID;
UPDATE lab_order_aud SET organization_id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE lab_result_aud ADD COLUMN organization_id UUID;
UPDATE lab_result_aud SET organization_id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE lab_order_attachment_aud ADD COLUMN organization_id UUID;
UPDATE lab_order_attachment_aud SET organization_id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE doctor_clinic_aud ADD COLUMN organization_id UUID;
UPDATE doctor_clinic_aud SET organization_id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE medication_aud ADD COLUMN organization_id UUID;
UPDATE medication_aud SET organization_id = '00000000-0000-0000-0000-000000000001';

ALTER TABLE appointment_diagnosis_aud ADD COLUMN organization_id UUID;
UPDATE appointment_diagnosis_aud SET organization_id = '00000000-0000-0000-0000-000000000001';

-- ============================================================
-- 5. Ajustar constraints únicos para ser per-organization
-- ============================================================

-- patient.dpi: UNIQUE(dpi) → UNIQUE(dpi, organization_id)
ALTER TABLE patient DROP CONSTRAINT IF EXISTS patient_dpi_key;
CREATE UNIQUE INDEX uq_patient_dpi_org ON patient(dpi, organization_id) WHERE dpi IS NOT NULL;

-- doctor.email: ahora puede repetirse entre organizaciones
ALTER TABLE doctor DROP CONSTRAINT IF EXISTS doctor_email_unique;
CREATE UNIQUE INDEX uq_doctor_email_org ON doctor(email, organization_id) WHERE email IS NOT NULL;

-- doctor.user_id: un user puede ser doctor en diferentes orgs (poco probable pero seguro)
ALTER TABLE doctor DROP CONSTRAINT IF EXISTS doctor_user_id_key;
CREATE UNIQUE INDEX uq_doctor_user_org ON doctor(user_id, organization_id) WHERE user_id IS NOT NULL;

-- specialty_form_template: unique per specialty per org
ALTER TABLE specialty_form_template DROP CONSTRAINT IF EXISTS unique_specialty_form;
CREATE UNIQUE INDEX uq_specialty_form_org ON specialty_form_template(specialty_id, organization_id);

-- user.username y user.email siguen siendo globalmente únicos (Keycloak lo requiere)
-- NO se modifican user_username_key ni user_email_key
