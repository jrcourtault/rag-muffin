--liquibase formatted sql

--changeset ragmuffin:002-create-table-workspaces
CREATE TABLE workspaces
(
    id            UUID                                           DEFAULT gen_random_uuid() PRIMARY KEY,
    name          VARCHAR(255) COLLATE case_insensitive NOT NULL,
    vertical_id   UUID                                  NOT NULL,
    active        BOOLEAN                               NOT NULL,
    chunk_size    INT                                   NOT NULL,
    chunk_overlap INT                                   NOT NULL,
    prefetch_size INT                                   NOT NULL,
    rerank        BOOLEAN                               NOT NULL,
    top_k         INT                                   NOT NULL,
    llm_base_url  VARCHAR(255)                          NOT NULL,
    llm_api_key   VARCHAR(255),
    llm_model     VARCHAR(255)                          NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE              NOT NULL DEFAULT now(),
    modified_at   TIMESTAMP WITH TIME ZONE              NOT NULL DEFAULT now()
);

ALTER TABLE workspaces
    ADD CONSTRAINT fk_workspaces_vertical FOREIGN KEY (vertical_id) REFERENCES verticals (id);
