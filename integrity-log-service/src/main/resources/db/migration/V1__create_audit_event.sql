CREATE TABLE audit_event (
id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
sequence_number BIGSERIAL NOT NULL UNIQUE,
event_type VARCHAR(100) NOT NULL,
actor_id VARCHAR(255) NOT NULL,
resource_type VARCHAR(100) NOT NULL,
resource_id VARCHAR(255) NOT NULL,
payload TEXT,
payload_digest VARCHAR(64),
content_hash VARCHAR(64) NOT NULL,
previous_hash VARCHAR(64) NOT NULL,
record_hash VARCHAR(64) NOT NULL,
created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_event_resource ON audit_event (resource_type, resource_id);
CREATE INDEX idx_audit_event_created_at ON audit_event (created_at);