-- Add redaction metadata to audit_event
ALTER TABLE audit_event
  ADD COLUMN IF NOT EXISTS redacted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE audit_event
  ADD COLUMN IF NOT EXISTS redaction_proof_id UUID NULL;

-- Add FK to audit_event_originals (if table exists)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = 'audit_event_originals') THEN
    IF NOT EXISTS (
      SELECT 1 FROM pg_constraint WHERE conname = 'fk_audit_redaction_proof'
    ) THEN
      EXECUTE 'ALTER TABLE audit_event
        ADD CONSTRAINT fk_audit_redaction_proof
        FOREIGN KEY (redaction_proof_id)
        REFERENCES audit_event_originals(id)
        ON DELETE SET NULL';
    END IF;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_audit_redacted ON audit_event(redacted);
CREATE INDEX IF NOT EXISTS idx_audit_redaction_proof_id ON audit_event(redaction_proof_id);
