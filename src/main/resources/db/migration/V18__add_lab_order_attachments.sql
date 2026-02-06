CREATE TABLE lab_order_attachment (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    lab_order_id UUID NOT NULL REFERENCES lab_order(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    file_data BYTEA NOT NULL,
    uploaded_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_lab_order_attachment_order_id ON lab_order_attachment(lab_order_id);

-- Auditoría (Envers) para lab_order_attachment (sin file_data por @NotAudited)

CREATE TABLE lab_order_attachment_AUD (
    id UUID NOT NULL,
    REV INTEGER NOT NULL REFERENCES REVINFO(REV),
    REVTYPE SMALLINT,
    lab_order_id UUID,
    file_name VARCHAR(255),
    content_type VARCHAR(100),
    file_size BIGINT,
    uploaded_by VARCHAR(255),
    created_at TIMESTAMP,
    PRIMARY KEY (id, REV)
);

CREATE INDEX idx_lab_order_attachment_aud_rev ON lab_order_attachment_AUD(REV);
