INSERT INTO workspaces (id, name, vertical_id, active, chunk_size, chunk_overlap, top_k, rerank, prefetch_size,
                       llm_base_url, llm_api_key, llm_model, created_at, modified_at)
VALUES ('dddddddd-0000-0000-0000-000000000001', 'Workspace Juridique', 'aaaaaaaa-0000-0000-0000-000000000001', TRUE,
        512, 77, 5, FALSE, 20, 'http://localhost:12434/engines/v1', 'EMPTY', 'llama3.2:16k', now(), now());
