CREATE TABLE organization_user (
        organization_user_id VARCHAR(100) PRIMARY KEY,
        organization_id VARCHAR(100) NOT NULL,
        user_id VARCHAR(100) NOT NULL,
        version INTEGER DEFAULT 0 NOT NULL,
        created_at date NOT NULL,
        created_by varchar(50) NOT NULL,
        updated_at date DEFAULT NULL,
        updated_by varchar(50) DEFAULT NULL
);

CREATE INDEX organization_user_organization_id_index ON organization_user (organization_id);
CREATE INDEX organization_user_user_id_index ON organization_user (user_id);
CREATE UNIQUE INDEX organization_user_organization_id_user_id_index ON organization_user (organization_id, user_id);

ALTER TABLE organization_user ADD CONSTRAINT organization_user_organization_id_fk FOREIGN KEY (organization_id) REFERENCES organizations (organization_id) ON DELETE CASCADE;