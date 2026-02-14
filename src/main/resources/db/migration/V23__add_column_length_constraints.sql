-- Doctor: agregar límites a columnas sin restricción
ALTER TABLE doctor ALTER COLUMN first_name TYPE VARCHAR(150);
ALTER TABLE doctor ALTER COLUMN last_name TYPE VARCHAR(150);
ALTER TABLE doctor ALTER COLUMN address TYPE VARCHAR(250);
ALTER TABLE doctor ALTER COLUMN email TYPE VARCHAR(150);
ALTER TABLE doctor ALTER COLUMN phone TYPE VARCHAR(20);
ALTER TABLE doctor ALTER COLUMN created_by TYPE VARCHAR(255);
ALTER TABLE doctor ALTER COLUMN updated_by TYPE VARCHAR(255);

-- Doctor AUD
ALTER TABLE doctor_aud ALTER COLUMN first_name TYPE VARCHAR(150);
ALTER TABLE doctor_aud ALTER COLUMN last_name TYPE VARCHAR(150);
ALTER TABLE doctor_aud ALTER COLUMN address TYPE VARCHAR(250);
ALTER TABLE doctor_aud ALTER COLUMN email TYPE VARCHAR(150);
ALTER TABLE doctor_aud ALTER COLUMN phone TYPE VARCHAR(20);
ALTER TABLE doctor_aud ALTER COLUMN created_by TYPE VARCHAR(255);
ALTER TABLE doctor_aud ALTER COLUMN updated_by TYPE VARCHAR(255);

-- LabResult: explicitar límites
ALTER TABLE lab_result ALTER COLUMN test_name TYPE VARCHAR(200);
ALTER TABLE lab_result ALTER COLUMN value TYPE VARCHAR(255);

-- LabResult AUD
ALTER TABLE lab_result_aud ALTER COLUMN test_name TYPE VARCHAR(200);
ALTER TABLE lab_result_aud ALTER COLUMN value TYPE VARCHAR(255);

-- DiagnosisCatalog: agregar límites a category y chapter
ALTER TABLE diagnosis_catalog ALTER COLUMN category TYPE VARCHAR(250);
ALTER TABLE diagnosis_catalog ALTER COLUMN chapter TYPE VARCHAR(250);

-- LabOrderAttachment: explicitar límites
ALTER TABLE lab_order_attachment ALTER COLUMN file_name TYPE VARCHAR(255);
ALTER TABLE lab_order_attachment ALTER COLUMN uploaded_by TYPE VARCHAR(100);

-- LabOrderAttachment AUD
ALTER TABLE lab_order_attachment_aud ALTER COLUMN file_name TYPE VARCHAR(255);
ALTER TABLE lab_order_attachment_aud ALTER COLUMN uploaded_by TYPE VARCHAR(100);
