CREATE TABLE IF NOT EXISTS client_access_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id VARCHAR(255) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    decision VARCHAR(50) NOT NULL DEFAULT 'ALLOW',
    purpose VARCHAR(255),
    correlation_id VARCHAR(255),
    ip_address VARCHAR(100),
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    accessed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_client_access_audit_actor_id ON client_access_audit(actor_id);
CREATE INDEX IF NOT EXISTS idx_client_access_audit_resource ON client_access_audit(resource_type, resource_id);
CREATE INDEX IF NOT EXISTS idx_client_access_audit_action ON client_access_audit(action);
CREATE INDEX IF NOT EXISTS idx_client_access_audit_accessed_at ON client_access_audit(accessed_at);
