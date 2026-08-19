DELETE FROM users;
DELETE FROM documents;
DELETE FROM workspaces;
DELETE FROM verticals WHERE locked = false;
