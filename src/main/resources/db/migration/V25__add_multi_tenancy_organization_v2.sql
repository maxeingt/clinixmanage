ALTER TABLE clinic DROP CONSTRAINT IF EXISTS clinic_organization_id_fkey;
ALTER TABLE doctor DROP CONSTRAINT IF EXISTS doctor_organization_id_fkey;
ALTER TABLE "user" DROP CONSTRAINT IF EXISTS user_organization_id_fkey;
ALTER TABLE patient DROP CONSTRAINT IF EXISTS patient_organization_id_fkey;
ALTER TABLE medical_appointment DROP CONSTRAINT IF EXISTS medical_appointment_organization_id_fkey;
ALTER TABLE medical_record DROP CONSTRAINT IF EXISTS medical_record_organization_id_fkey;
ALTER TABLE prescription DROP CONSTRAINT IF EXISTS prescription_organization_id_fkey;
ALTER TABLE lab_order DROP CONSTRAINT IF EXISTS lab_order_organization_id_fkey;
ALTER TABLE lab_result DROP CONSTRAINT IF EXISTS lab_result_organization_id_fkey;
ALTER TABLE lab_order_attachment DROP CONSTRAINT IF EXISTS lab_order_attachment_organization_id_fkey;
ALTER TABLE medical_history_pathological_fam DROP CONSTRAINT IF EXISTS medical_history_pathological_fam_organization_id_fkey;
ALTER TABLE doctor_clinic DROP CONSTRAINT IF EXISTS doctor_clinic_organization_id_fkey;
ALTER TABLE doctor_specialty DROP CONSTRAINT IF EXISTS doctor_specialty_organization_id_fkey;
ALTER TABLE dashboard_widget_config DROP CONSTRAINT IF EXISTS dashboard_widget_config_organization_id_fkey;
ALTER TABLE specialty_form_template DROP CONSTRAINT IF EXISTS specialty_form_template_organization_id_fkey;

ALTER TABLE organization
ALTER COLUMN id TYPE VARCHAR(36)
USING id::text;

ALTER TABLE clinic ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE doctor ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE "user" ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE patient ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE medical_appointment ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE medical_record ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE prescription ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE lab_order ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE lab_result ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE lab_order_attachment ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE medical_history_pathological_fam ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE doctor_clinic ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE doctor_specialty ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE dashboard_widget_config ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE specialty_form_template ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;

ALTER TABLE clinic_aud ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE doctor_aud ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE patient_aud ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE medical_appointment_aud ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE medical_record_aud ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE prescription_aud ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE prescription_medication_aud ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE lab_order_aud ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE lab_result_aud ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE lab_order_attachment_aud ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE doctor_clinic_aud ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE medication_aud ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;
ALTER TABLE appointment_diagnosis_aud ALTER COLUMN organization_id TYPE VARCHAR(36) USING organization_id::text;

ALTER TABLE clinic ADD CONSTRAINT clinic_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE doctor ADD CONSTRAINT doctor_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE "user" ADD CONSTRAINT user_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE patient ADD CONSTRAINT patient_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE medical_appointment ADD CONSTRAINT medical_appointment_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE medical_record ADD CONSTRAINT medical_record_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE prescription ADD CONSTRAINT prescription_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE lab_order ADD CONSTRAINT lab_order_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE lab_result ADD CONSTRAINT lab_result_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE lab_order_attachment ADD CONSTRAINT lab_order_attachment_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE medical_history_pathological_fam ADD CONSTRAINT medical_history_pathological_fam_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE doctor_clinic ADD CONSTRAINT doctor_clinic_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE doctor_specialty ADD CONSTRAINT doctor_specialty_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE dashboard_widget_config ADD CONSTRAINT dashboard_widget_config_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE specialty_form_template ADD CONSTRAINT specialty_form_template_organization_id_fkey
    FOREIGN KEY (organization_id) REFERENCES organization(id);