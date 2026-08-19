INSERT INTO workspaces (id, name, vertical_id, active, chunk_size, chunk_overlap, top_k, rerank, prefetch_size,
                       llm_base_url, llm_api_key, llm_model, created_at, modified_at)
VALUES ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Cabinet Martin', '00000000-0000-0000-0000-000000000001', TRUE, 512,
        77, 10, FALSE, 20, 'http://localhost:12434/engines/v1', 'EMPTY', 'llama3.2:16k', now(), now());

INSERT INTO workspaces (id, name, vertical_id, active, chunk_size, chunk_overlap, top_k, rerank, prefetch_size,
                       llm_base_url, llm_api_key, llm_model, created_at, modified_at)
VALUES ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Club Para Bordeaux', '00000000-0000-0000-0000-000000000001', TRUE,
        512, 77, 10, FALSE, 20, 'http://localhost:12434/engines/v1', 'EMPTY', 'llama3.2:16k', now(), now());

INSERT INTO workspaces (id, name, vertical_id, active, chunk_size, chunk_overlap, top_k, rerank, prefetch_size,
                       llm_base_url, llm_api_key, llm_model, created_at, modified_at)
VALUES ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'Ancien Cabinet', '00000000-0000-0000-0000-000000000001', FALSE, 512,
        77, 10, FALSE, 20, 'http://localhost:12434/engines/v1', 'EMPTY', 'llama3.2:16k', now(), now());
