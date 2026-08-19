INSERT INTO documents (id, workspace_id, name, extension, content_type, size_bytes, status, chunk_count, created_at, modified_at)
VALUES ('c3d4e5f6-a7b8-9012-cdef-345678901234',
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'Contrat de bail', 'pdf', 'application/pdf', 125000, 'INDEXED', 15, now(), now());

INSERT INTO documents (id, workspace_id, name, extension, content_type, size_bytes, status, created_at, modified_at)
VALUES ('d4e5f6a7-b8c9-0123-defa-456789012345',
        'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'Manuel parachute', 'pdf', 'application/pdf', 350000, 'PENDING', now(), now());

INSERT INTO documents (id, workspace_id, name, extension, content_type, size_bytes, status, created_at, modified_at)
VALUES ('e5f6a7b8-c9d0-1234-efab-567890123456',
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'Reglement copropriete', 'docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 98000, 'ERROR', now(), now());
