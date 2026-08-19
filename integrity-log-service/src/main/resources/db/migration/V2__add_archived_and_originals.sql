ALTER TABLE audit_event
  ADD COLUMN archived_at TIMESTAMPTZ NULL;

CREATE TABLE audit_event_originals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  event_id UUID NOT NULL REFERENCES audit_event(id) ON DELETE CASCADE,
  encrypted_payload BYTEA NOT NULL,
  original_content_hash VARCHAR(64) NOT NULL,
  key_id VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  redacted_by VARCHAR(255)
);

CREATE INDEX idx_audit_event_archived_at ON audit_event (archived_at);
CREATE INDEX idx_audit_event_originals_event_id ON audit_event_originals(event_id);
CREATE INDEX idx_audit_event_originals_original_hash ON audit_event_originals(original_content_hash);