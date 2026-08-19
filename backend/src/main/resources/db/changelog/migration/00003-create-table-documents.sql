--liquibase formatted sql

--changeset ragmuffin:003-create-table-documents
CREATE TABLE documents
(
    id           UUID                                           DEFAULT gen_random_uuid() PRIMARY KEY,
    workspace_id UUID                                  NOT NULL,
    name         VARCHAR(255) COLLATE case_insensitive NOT NULL,
    extension    VARCHAR(20)                           NOT NULL,
    content_type VARCHAR(255)                          NOT NULL,
    size_bytes   BIGINT                                NOT NULL,
    status       VARCHAR(20)                           NOT NULL DEFAULT 'PENDING',
    chunk_count  INTEGER,
    created_at   TIMESTAMP WITH TIME ZONE              NOT NULL DEFAULT now(),
    modified_at  TIMESTAMP WITH TIME ZONE              NOT NULL DEFAULT now()
);

ALTER TABLE documents
    ADD CONSTRAINT fk_documents_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE CASCADE;

CREATE INDEX idx_documents_workspace_id ON documents (workspace_id);

ALTER TABLE documents
    ADD CONSTRAINT ck_documents_status CHECK (status IN ('PENDING', 'INDEXED', 'ERROR'));
