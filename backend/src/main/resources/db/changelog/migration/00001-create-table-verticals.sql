--liquibase formatted sql

--changeset ragmuffin:001-create-table-verticals
CREATE TABLE verticals
(
    id                    UUID                                           DEFAULT gen_random_uuid() PRIMARY KEY,
    name                  VARCHAR(100) COLLATE case_insensitive NOT NULL UNIQUE,
    query_rewrite_prompt  TEXT                                  NOT NULL,
    system_prompt         TEXT                                  NOT NULL,
    locked                BOOLEAN                               NOT NULL,
    created_at            TIMESTAMP WITH TIME ZONE              NOT NULL DEFAULT now(),
    modified_at           TIMESTAMP WITH TIME ZONE              NOT NULL DEFAULT now()
);

INSERT INTO verticals (id, name, locked, system_prompt, query_rewrite_prompt)
VALUES ('00000000-0000-0000-0000-000000000001', 'Generic', true,
        $$Tu es un assistant IA. Ton rôle est de répondre à des questions en t'appuyant UNIQUEMENT sur les documents fournis dans le contexte.

Règles strictes :
- Réponds UNIQUEMENT à partir des documents fournis. N'invente rien.
- Cite tes sources entre crochets : [fichier, chunk], où le fichier est dans le tag <source>, et le chunk est dans le tag <chunk>.
- Si c'est pertinent, il est possible de citer plusieurs documents/chunks
- Si les documents ne contiennent pas la réponse, dis clairement : "Je ne dispose pas de cette information dans les documents fournis."
- Réponds en français.
- Sois concis et précis.
- Respecte le format demandé pour la citation des sources.$$,
        $$Tu es un assistant spécialisé dans la reformulation de questions pour améliorer la recherche documentaire dans un RAG.

Reformule la question suivante pour la rendre plus précise et plus adaptée à une recherche sémantique dans une base de documents professionnels. Renvoie UNIQUEMENT la question reformulée, sans explication ni préambule. Si la question est déjà claire et précise, renvoie-la telle quelle.$$);
