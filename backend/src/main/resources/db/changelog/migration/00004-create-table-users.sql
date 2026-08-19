--liquibase formatted sql

--changeset ragmuffin:004-create-table-users
CREATE TABLE users
(
    id           UUID                                           DEFAULT gen_random_uuid() PRIMARY KEY,
    idp_id       UUID                                  NOT NULL,
    workspace_id UUID                                  NOT NULL,
    role         VARCHAR(20)                           NOT NULL,
    email        VARCHAR(255) COLLATE case_insensitive NOT NULL,
    first_name   VARCHAR(255) COLLATE case_insensitive NOT NULL,
    last_name    VARCHAR(255) COLLATE case_insensitive NOT NULL,
    langue       VARCHAR(5)                            NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE              NOT NULL DEFAULT now(),
    modified_at  TIMESTAMP WITH TIME ZONE              NOT NULL DEFAULT now()
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE CASCADE;

ALTER TABLE users
    ADD CONSTRAINT uq_users_user_workspace UNIQUE (idp_id, workspace_id);

ALTER TABLE users
    ADD CONSTRAINT ck_users_role CHECK (role IN ('OWNER', 'EDITOR', 'VIEWER'));

ALTER TABLE users
    ADD CONSTRAINT ck_users_langue CHECK (langue IN ('fr', 'en'));

CREATE INDEX idx_users_idp_id ON users (idp_id);
CREATE INDEX idx_users_workspace_id ON users (workspace_id);

CREATE UNIQUE INDEX uq_one_owner_per_workspace
    ON users (workspace_id)
    WHERE role = 'OWNER';
