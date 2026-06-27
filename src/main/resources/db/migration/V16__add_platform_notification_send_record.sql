CREATE TABLE IF NOT EXISTS platform_notification_send_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    title VARCHAR(200) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_description VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    recipient_count INTEGER NOT NULL,
    triggered_by VARCHAR(64) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    extension_channels VARCHAR(255)
);
