-- Jean Martin (idp_id: a1b2...) is OWNER of workspace 1 (Cabinet Martin)
INSERT INTO users (id, idp_id, workspace_id, role, email, first_name, last_name, langue, created_at, modified_at)
VALUES ('d4e5f6a7-b8c9-0123-defa-456789012345',
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'OWNER', 'jean.martin@example.com', 'Jean', 'Martin', 'fr', now(), now());

-- Jean Martin (idp_id: a1b2...) is also EDITOR of workspace 2 (Club Para Bordeaux)
INSERT INTO users (id, idp_id, workspace_id, role, email, first_name, last_name, langue, created_at, modified_at)
VALUES ('f6a7b8c9-d0e1-2345-abcd-678901234567',
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'EDITOR', 'jean.martin@example.com', 'Jean', 'Martin', 'fr', now(), now());

-- Marie Dupont (idp_id: c3d4...) is VIEWER of workspace 1 (Cabinet Martin)
INSERT INTO users (id, idp_id, workspace_id, role, email, first_name, last_name, langue, created_at, modified_at)
VALUES ('e5f6a7b8-c9d0-1234-efab-567890123456',
        'c3d4e5f6-a7b8-9012-cdef-345678901234',
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'VIEWER', 'marie.dupont@example.com', 'Marie', 'Dupont', 'fr', now(), now());
