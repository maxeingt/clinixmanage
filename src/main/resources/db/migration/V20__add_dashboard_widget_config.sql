CREATE TABLE dashboard_widget_config (
    user_id UUID NOT NULL,
    clinic_id UUID NOT NULL,
    widgets JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (user_id, clinic_id),
    CONSTRAINT fk_dwc_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_dwc_clinic FOREIGN KEY (clinic_id) REFERENCES clinic (id) ON DELETE CASCADE
);
