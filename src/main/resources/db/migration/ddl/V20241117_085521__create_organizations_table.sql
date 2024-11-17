CREATE TABLE organizations (
        organization_id VARCHAR(100) PRIMARY KEY,
        owner_id VARCHAR(100),
        version INTEGER DEFAULT 0 NOT NULL,
        name VARCHAR(255) NOT NULL,
        plan VARCHAR(50) NOT NULL,
        site_url TEXT,
        created_at date NOT NULL,
        created_by varchar(50) NOT NULL,
        updated_at date DEFAULT NULL,
        updated_by varchar(50) DEFAULT NULL
);

CREATE INDEX organizations_owner_id_index ON organizations (owner_id);