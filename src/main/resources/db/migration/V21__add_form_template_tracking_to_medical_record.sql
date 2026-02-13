ALTER TABLE medical_record ADD COLUMN form_template_id UUID;
ALTER TABLE medical_record ADD COLUMN form_template_version INTEGER;
ALTER TABLE medical_record ADD CONSTRAINT fk_record_form_template
    FOREIGN KEY (form_template_id) REFERENCES specialty_form_template(id) ON DELETE SET NULL;
