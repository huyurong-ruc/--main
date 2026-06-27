CREATE TABLE certificate_attachment (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128),
    file_size BIGINT,
    storage_path VARCHAR(500),
    CONSTRAINT fk_certificate_attachment_request FOREIGN KEY (request_id) REFERENCES certificate_request(id) ON DELETE CASCADE
);

CREATE INDEX idx_certificate_attachment_request_id ON certificate_attachment(request_id);