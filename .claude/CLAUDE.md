# CLAUDE.md — Guide de développement du projet RAG multi-verticale

## Qu'est-ce que ce projet ?

Un **assistant IA pour PME françaises** qui répond aux questions des utilisateurs en s'appuyant sur leurs propres
documents. L'utilisateur uploade ses fichiers (PDF, Word, emails…), le système les découpe, les indexe, et quand on pose
une question, il retrouve les passages pertinents puis rédige une réponse sourcée.

Le système dessert **5 verticales métier** (= 5 marchés spécialisés) avec un socle technique commun :

1. **Parachutisme** *(première verticale développée)* — base de connaissances pour les clubs et écoles de parachutisme (
   réglementation DGAC, manuels techniques, procédures de sécurité, formation)
2. **Avocats / Notaires** — recherche dans la jurisprudence, les codes de loi (Légifrance), les contrats du cabinet
3. **Experts-comptables** — analyse de documents fiscaux (BOFIP, CGI), détection d'anomalies comptables
4. **Knowledge management PME** — capturer et rendre interrogeables les savoirs internes d'une entreprise
5. **Intelligence administrative** — agréger emails, documents, scans et les rendre exploitables

Chaque client ne voit que ses propres données (multi-workspace). Le corpus public (lois, normes comptables) est partagé
entre tous les clients d'une même verticale.

---

## Stack technique

| Rôle                 | Outil                                                                                | Pourquoi                                                                                     |
|----------------------|--------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| Backend API          | **Spring Boot 4** (Java 25)                                                          | Virtual Threads, écosystème mature                                                           |
| Appels LLM           | **RestClient** (Spring) → API OpenAI-compatible                                      | Appels HTTP directs, contrôle total, pas de framework IA                                     |
| Appels Embeddings    | **RestClient** (Spring) → serveur BGE-M3 FastAPI                                     | Appels HTTP directs, pas de dépendance framework                                             |
| Base relationnelle   | **PostgreSQL 18**                                                                    | Métadonnées, workspaces, audit. Row-Level Security pour l'isolation                          |
| Base vectorielle     | **Qdrant 1.17** via **QdrantClient** gRPC natif                                      | Recherche hybride native (dense + sparse BGE-M3), multi-workspace par payload, écrit en Rust |
| Embeddings           | **BGE-M3** via **FastAPI** + **FlagEmbedding**                                       | Dense 1024 dims + sparse (lexical weights) depuis un seul modèle, français excellent         |
| Reranker             | **BGE-Reranker-v2-M3** via **TEI** (HuggingFace Text Embeddings Inference)           | Cross-encoder multilingue, même famille BGE-M3, ~+12 pts de précision vs hybride seul        |
| LLM (dev local)      | **Docker Model Runner** + Llama 3.2 3B (`llama3.2:16k`)                              | 100% dockerisé, GPU CUDA (RTX 3070+), API OpenAI-compatible                                  |
| LLM (production)     | **Mistral Small 3.2** via API Mistral (`mistral-small-24b-instruct-2506`)            | Hébergé en Europe, RGPD natif, 128k contexte, multimodal, Apache 2.0                         |
| LLM (futur)          | **vLLM** + Mistral Small 3.2 local (`mistralai/Mistral-Small-3.2-24B-Instruct-2506`) | Quand le volume justifie un serveur GPU dédié                                                |
| File d'attente       | **ActiveMQ Artemis**                                                                 | Broker JMS embarqué dans Spring Boot ou conteneur Docker, file d'attente ingestion           |
| Extraction documents | **Apache Tika 2.x**                                                                  | Java natif, 1000+ formats supportés                                                          |
| OCR                  | **Tesseract 5**                                                                      | Gratuit, CPU-only, suffisant pour les scans propres                                          |
| Auth                 | **Keycloak 26**                                                                      | OAuth2/OIDC, multi-workspace via Realms, gratuit                                             |
| Frontend             | **Angular 21** + **PrimeNG**                                                         | Framework structuré, familiarité développeur, écosystème mature                              |
| Reverse proxy        | **Traefik 3**                                                                        | HTTPS auto (Let's Encrypt), discovery Docker, middlewares natifs (rate limit, WAF Coraza)    |
| Conteneurisation     | **Docker Compose**                                                                   | Suffisant pour un développeur solo, pas de Kubernetes                                        |
| CI/CD                | **GitHub Actions**                                                                   | Gratuit (2000 min/mois)                                                                      |
| Monitoring           | **Grafana** + **Prometheus** + **Loki**                                              | Stack observabilité complète : métriques, logs, dashboards                                   |

### Ce qu'on n'utilise PAS (et pourquoi)

- **Kubernetes / Docker Swarm** — complexité inutile à cette échelle. Docker Compose suffit.
- **Microservices** — monolithe modulaire. Un seul JAR Spring Boot.
- **Spring AI / LangChain4j** — retiré. Abstractions trop rigides (pas de recherche hybride Qdrant, pipeline RAG peu
  flexible). Appels HTTP directs via RestClient + QdrantClient gRPC natif.
- **LangChain (Python)** — pas de framework IA Python. Le seul service Python est le serveur BGE-M3 (FastAPI minimal).
- **Vue.js / React / Next.js** — Angular choisi pour la familiarité du développeur. SPA simple, pas besoin de SSR.
- **Fine-tuning** — BGE-M3 out-of-the-box suffit. Fine-tuning quand on aura 10K+ paires de données.
- **GraphRAG** — over-engineering. Recherche hybride classique d'abord.
- **SSE (Server-Sent Events) pour le suivi d'ingestion** — incompatible avec le load balancing multi-instances
  (connexions en mémoire). Remplacé par polling côté frontend.

---

## Architecture des services Docker

### Deux environnements, un seul docker-compose.yml

Le même `docker-compose-dev.yml` sert pour les deux machines de dev. Le LLM (`llama3.2:16k`) tourne via Docker Model
Runner — à packager une fois avec `docker model package --from ai/llama3.2 --context-size 16384 llama3.2:16k`.

**Machines de dev** : deux environnements supportés :

- **Mac M3** — GPU via Metal, Docker Model Runner détecte automatiquement Apple Silicon
- **PC Windows/Linux avec RTX 3070** (8 Go VRAM) — GPU via CUDA, à activer une seule fois :

```bash
docker desktop enable model-runner --gpu enable
```

**Serveur de production** : OVH VPS-6 2026 (24 vCores, 96 Go RAM, 400 Go NVMe, datacenter France, ~42-49 €/mois).

### Prérequis pour le dev local : Docker Model Runner

Le LLM tourne via **Docker Model Runner**, intégré à Docker Desktop. Le backend Spring Boot tourne **hors Docker** et
communique avec Model Runner via TCP — le port 12434 doit être activé une seule fois :

```bash
docker desktop enable model-runner --tcp=12434
```

**Endpoint utilisé par Spring Boot :**

```
http://localhost:12434/engines/v1
```

### Schéma de l'architecture dev locale

```
localhost (Mac M3 ou PC RTX 3070)
   │
   ├── http://localhost:4200     → [Angular dev server]   (frontend, hot reload)
   ├── http://localhost:8080     → [Spring Boot]          (backend)
   │
   │   Réseau Docker interne :
   │   [Spring Boot] ──► [PostgreSQL :5432]
   │                 ──► [Qdrant :6334]
   │                 ──► [ActiveMQ :61616]
   │                 ──► [BGE-M3 FastAPI :80]                          (CPU)
   │                 ──► [TEI Reranker :80]                            (GPU CUDA ou CPU)
   │                 ──► [Docker Model Runner :engines/v1]          (GPU — Metal ou CUDA)
   │
   └── http://localhost:6333/dashboard  → dashboard Qdrant (debug)
```

En dev : pas de Traefik, pas de Keycloak, pas de HTTPS. Le frontend Angular tourne **en dehors de Docker**
avec `ng serve` (hot reload instantané). Le reste tourne dans Docker, y compris le LLM via Docker Model Runner.

### Schéma de l'architecture production (OVH VPS-6)

```
Internet
   │
   ▼
[Traefik] ── ports 80/443 (seul accès externe)
   │
   ├── ancrage.fr        → [Angular :80]         (frontend, Nginx statique)
   ├── api.ancrage.fr    → [Spring Boot :8080]   (backend)
   └── auth.ancrage.fr   → [Keycloak :8080]      (auth)

Réseau interne Docker (aucun accès externe) :
   [Spring Boot] ──► [PostgreSQL :5432]
                ──► [Qdrant :6334]
                ──► [ActiveMQ :61616]
                ──► [BGE-M3 FastAPI :80]
                ──► [TEI Reranker :80]
                ──► [API Mistral] (externe, HTTPS)
```

En prod : Traefik, Keycloak, HTTPS, pas d'Ollama (API Mistral à la place).

Deux réseaux Docker en prod :

- `frontend-net` : Traefik, Vue.js, Spring Boot, Keycloak
- `backend-net` (internal: true) : Spring Boot, PostgreSQL, Qdrant, ActiveMQ, BGE-M3 FastAPI — **pas d'accès internet**

---

## Le flux d'ingestion expliqué simplement

Quand un utilisateur uploade un document, voici ce qui se passe dans l'ordre :

```
1. L'utilisateur uploade un fichier (PDF, DOCX, TXT, PPTX…)
   via POST /api/documents/upload (multipart/form-data)

2. Spring Boot reçoit le fichier avec le workspace_id du client (via JWT Keycloak)

3. Le fichier est sauvegardé sur disque dans /documents/uploads/{workspace_id}/{document_id}.{ext}

4. Une entrée DocumentEntity est créée en base PostgreSQL (status: PENDING)

5. Un message JMS est publié dans la queue ActiveMQ "document-ingestion"
   → Le contrôleur retourne immédiatement l'ID du document au client
   → Le traitement continue en arrière-plan (asynchrone)

6. Un listener Spring (@JmsListener) consomme le message et lance le pipeline :

   6a. Extraction du texte brut avec Apache Tika
       → Tika détecte automatiquement le format (PDF, DOCX, PPTX, TXT, ODT…)
       → Pour les scans/images : OCR via Tesseract 5

   6b. Découpage en chunks avec ChunkingService (jtokkit, tokenizer CL100K_BASE)
       → Taille : 512 tokens, overlap : 77 tokens (15% — recommandation NVIDIA)
       → Chaque chunk conserve ses métadonnées : workspace_id, document_id,
         filename, chunk_index

   6c. Vectorisation de chaque chunk via le serveur BGE-M3
       → Chaque chunk est transformé en vecteur dense de 1024 dimensions
       → + vecteur sparse (lexical weights BGE-M3) pour la recherche hybride

   6d. Stockage des vecteurs dans Qdrant
       → Payload obligatoire : workspace_id (isolation multi-workspace)
       → Payload : document_id, filename, chunk_index, texte original

7. IngestionJmsListener publie le résultat dans la queue "document-status"
   → IngestionResultJmsListener (dans le backend API) consomme le message
      et met à jour le statut en base : PENDING → INDEXED (ou ERROR)
   → Le frontend suit la progression par polling GET /api/documents/{id}
      toutes les 5 secondes tant qu'un document est en PENDING
```

---

## Le flux RAG expliqué simplement

Quand un utilisateur pose une question, voici ce qui se passe dans l'ordre :

```
1. L'utilisateur tape : "Quelles sont les obligations du bailleur en cas de dégât des eaux ?"

2. Spring Boot reçoit la requête avec le workspace_id du client (via JWT Keycloak)

3. La question est envoyée au serveur BGE-M3 pour être transformée en vecteur de 1024 nombres

4. Ce vecteur est envoyé à Qdrant pour chercher les n fiches les plus proches
   → Filtre obligatoire : workspace_id = ce_client OU source = "shared" (corpus Légifrance)
   → Recherche hybride : par sens (dense) + par mots expansés (SPLADE)

5. Les n fiches les plus pertinentes sont sélectionnées

6. Ces fiches + la question sont envoyées au LLM avec un prompt du type :
   "Voici n extraits de documents. Réponds en t'appuyant UNIQUEMENT sur ces extraits.
    Cite tes sources. Dis quand tu ne sais pas."
   → En dev : Docker Model Runner (Llama 3.2 3B, local, gratuit)
   → En prod : API Mistral Small (24B, Europe, payant)

7. Le LLM rédige la réponse, qui est renvoyée à l'utilisateur
```

---

## Structure du projet

```
rag-platform/
├── CLAUDE.md                        ← ce fichier
├── backend/                         ← Projet Spring Boot 4
├── embeddings/                      ← Serveur BGE-M3 (FastAPI + FlagEmbedding)
├── frontend/                        ← Projet Angular 21 + PrimeNG
├── keycloak/                        ← Fichiers de config de Keycloak
└── scripts/                         ← Des scripts
```

---

## Multi-workspace (isolation des données clients)

Chaque client a un `workspace_id` unique (UUID). Ce workspace_id est présent :

- Dans chaque table PostgreSQL (colonne `workspace_id` + Row-Level Security)
- Dans chaque vecteur Qdrant (payload `workspace_id` indexé)
- Dans chaque log applicatif (MDC SLF4J)

### Architecture auth vs authz (séparation authentification / autorisation)

- **Authentification** : Keycloak (ou JWT HS256 en dev). Le JWT contient un `sub` (UUID utilisateur Keycloak), pas de
  `workspace_id`.
- **Autorisation** : table `users` en base PostgreSQL. Associe un `user_id` (= `sub` du JWT) à un ou plusieurs
  `workspace_id` avec un rôle, un prénom et un nom (propres à chaque workspace).
- **AuthService** (`security/AuthService.java`) : extrait le `userId` (UUID) depuis le `sub` du JWT. Plus de
  `getWorkspaceId()` — le workspace est résolu via la table `users`.

### Rôles par workspace

La table `users` associe chaque utilisateur à ses workspaces avec un rôle :

| Rôle       | Description                                                                                                                   |
|------------|-------------------------------------------------------------------------------------------------------------------------------|
| **OWNER**  | Gestion des membres non-owner du workspace (ajout/suppression, modification des rôles). Seul un ADMIN peut modifier un OWNER. |
| **EDITOR** | Upload/suppression de documents + tout VIEWER                                                                                 |
| **VIEWER** | Consultation : RAG (ask, search), lecture des documents                                                                       |

Un utilisateur peut avoir des rôles différents sur des workspaces différents (ex: OWNER sur son cabinet, VIEWER sur
celui
d'un confrère).

**Règle absolue** : aucune requête Qdrant ne peut exister sans filtre `workspace_id`. C'est un invariant de sécurité non
négociable. Le client Dupont ne doit JAMAIS voir les documents du client Martin.

Le corpus partagé (Légifrance, BOFIP) est indexé avec `source: "shared"` et accessible en lecture à tous les workspaces
d'une verticale.

---

## Conventions de code

### Backend (Java)

- Java 25 avec Virtual Threads activés
- Spring Boot 4.0.3
- Records Java pour les DTOs, Lombok pour les entities JPA et le logging (@Slf4j), MapStruct pour le mapping entité →
  DTO
- Mappers MapStruct avec pattern `INSTANCE` statique (`Mappers.getMapper(...)`, pas de `componentModel = "spring"`) —
  pas d'injection Spring, appel via `WorkspaceMapper.INSTANCE.toResponse(...)`
- Toujours `unmappedTargetPolicy = ReportingPolicy.ERROR` sur `@Mapper` — force à gérer explicitement chaque propriété
  cible
- Les DTOs et les mappers MapStruct sont dans un sous-package `dto/` de chaque module (ex:
  `workspace/dto/WorkspaceResponse.java`, `workspace/dto/WorkspaceMapper.java`)
- Les DTOs Response n'exposent jamais `createdAt` ni `modifiedAt` — ces champs d'audit restent internes.
  Exception : `DocumentResponse` expose `createdAt` (date d'upload visible par l'utilisateur)
- Injection par constructeur via `@RequiredArgsConstructor` (Lombok) sur les contrôleurs, services et composants Spring
  — pas de constructeur explicite, pas de `@Autowired`
- Entités JPA immutables par défaut : `@Getter` + `@NoArgsConstructor(access = PROTECTED)` + `@Builder` sur un
  constructeur privé (pas de `@Setter`). Mutations via des méthodes de domaine explicites.
- `Optional` plutôt que `null`
- Logging via SLF4J avec MDC pour le workspace_id
- Tests : JUnit 5 + Testcontainers PostgreSQL (tous les tests tournent contre un vrai PostgreSQL via Testcontainers)
- Approche **database-first** : le schéma SQL est la source de vérité. On écrit d'abord la migration Liquibase, puis
  l'entité JPA qui se valide contre le schéma. `ddl-auto: validate` uniquement (jamais `update` ou `create`). Migrations
  dans `backend/src/main/resources/db/changelog/migration/`
- Toutes les entités JPA ont un `createdAt` et un `modifiedAt` annotés `@CreatedDate` et `@LastModifiedDate` (Spring
  Data JPA Auditing). Type Java : `OffsetDateTime` (pas `Instant`) — rend l'offset explicite dans les logs, le JSON et
  le debug
- En SQL, toujours utiliser `TIMESTAMP WITH TIME ZONE` (jamais `TIMESTAMP` sans timezone) — stockage en UTC, pas
  d'ambiguïté de fuseau
- Toutes les clés étrangères sont `ON DELETE CASCADE` — la suppression d'un parent supprime automatiquement les enfants.
  Les FK sont toujours définies par `ALTER TABLE ... ADD CONSTRAINT fk_... FOREIGN KEY ...` (jamais inline dans le
  `CREATE TABLE`)
- Tous les contrôleurs sont annotés `@Transactional(rollbackFor = Exception.class)` au niveau de la classe. Les méthodes
  `@GetMapping` sont annotées `@Transactional(readOnly = true)` pour désactiver le dirty checking Hibernate et permettre
  les optimisations DB en lecture.
- Les endpoints retournent directement le DTO (pas de `ResponseEntity`). Ne pas utiliser `@ResponseStatus` pour les
  codes HTTP non-200 (uniquement des codes 200)
- Pas d'exceptions checked custom — utiliser `ResponseStatusException` de Spring
- Organisation des services dans chaque module :
    - `service/` : services de logique métier (classes concrètes, pas d'interface si une seule implémentation)
    - `service/external/` : services délégués à des systèmes externes (filesystem, broker JMS, Qdrant…).
      Ceux-ci utilisent une **interface** avec deux implémentations : `XxxServiceImpl` (`@Profile("!test")`) pour
      dev/prod
      et `XxxServiceMock` (`@Profile("test")`) qui retourne des réponses vides.
      Pas de `@ConditionalOnProperty` ni `@ConditionalOnBean` (ce dernier est réservé aux auto-configurations Spring
      Boot)
- Formatage : style Google Java Format
- Pour chaque contrôleur créé, créer un fichier `.http` (IntelliJ HTTP Client) dans `backend/src/test/http/` pour les
  tests manuels. Un fichier par contrôleur (ex: `workspace.http`), avec un appel par endpoint. Seule variable
  autorisée :
  `{{access_token}}` (définie dans `http-client.env.json`). Toutes les requêtes incluent
  `Authorization: Bearer {{access_token}}`.
- Pour chaque endpoint, écrire des tests MockMvc couvrant plusieurs cas (nominal, erreurs, cas limites). Un fichier de
  test par endpoint (ex: `CreateWorkspaceTest.java`, `GetWorkspaceTest.java`), regroupant tous les cas de cet endpoint.
- Dans les tests MockMvc, utiliser des **text blocks JSON** (`"""..."""`) pour les request bodies — teste le vrai
  contrat HTTP de désérialisation, pas la sérialisation Java. Pour les cas de champ absent (test `@NotNull`), omettre le
  champ du JSON plutôt que d'envoyer `null`.
- Les données de test sont dans des scripts SQL dans `src/test/resources/sql/` (un fichier par entité, ex:
  `workspaces.sql`). Chaque méthode de test est annotée `@Sql(scripts = {"/sql/workspaces.sql"})` — pas d'insertion
  programmatique via les repositories.
- Spring Boot 4 utilise Jackson 3 : le package est `tools.jackson.*` (pas `com.fasterxml.*`), et le bean auto-configuré
  est `JsonMapper` (pas `ObjectMapper`)

### Workflow de test

- **Ne jamais lancer les tests automatiquement** — l'utilisateur les lance lui-même
- À chaque étape, indiquer la commande ou la procédure pour valider (ex: `mvn test`, appel HTTP, vérification dashboard)

### Frontend (Angular)

- Angular 21 avec standalone components (pas de NgModules)
- TypeScript strict obligatoire
- Angular CLI comme toolchain (ng serve, ng build, ng generate)
- Angular Router pour la navigation (routes déclarées dans `app.routes.ts`)
- Signals pour le state management réactif (pas de RxJS sauf pour les appels HTTP)
- **PrimeNG** comme bibliothèque de composants UI
- `HttpClient` natif Angular pour les appels API (dans des services injectables `/services/api.service.ts`)
- Pas de SSR (Angular Universal) — c'est une SPA simple
- **Transloco** pour l'internationalisation (i18n) — fichiers de traduction dans `frontend/public/assets/i18n/` (
  fr.json, en.json). Toujours traduire les textes visibles par l'utilisateur.
- Après modification des traductions, lancer `npm run check:i18n` pour vérifier la cohérence (clés manquantes,
  orphelines, incohérences entre langues)
- **Imports avec alias `@/`** — utiliser `@/` (alias vers `src/`) pour tous les imports internes au projet
  (ex: `import { AuthService } from '@/services/auth.service'`). Seuls les imports `./` (même dossier) restent en
  relatif. Ne pas modifier les fichiers générés (`src/api/backend/`).
- **Pas de lifecycle hooks Angular** — utiliser les APIs modernes à la place :
    - Pas de `ngOnInit` → initialisation dans le `constructor` + `resource()` pour les données async
    - Pas de `ngOnChanges` → `effect()` ou `computed()` sur les signaux
    - Pas de `ngOnDestroy` → `DestroyRef` (injecté via `inject(DestroyRef)`)

---

## Variables d'environnement requises

```env
# ── Profil Spring ──
# dev  = Docker Model Runner local, pas de Keycloak, pas de HTTPS
# prod = API Mistral, Keycloak, Traefik HTTPS
SPRING_PROFILES_ACTIVE=dev

# PostgreSQL
POSTGRES_USER=ragadmin
POSTGRES_PASSWORD=<mot_de_passe_fort>
POSTGRES_DB=ragapp

# Qdrant
QDRANT_API_KEY=<cle_min_25_caracteres>

# Mistral (prod uniquement)
MISTRAL_API_KEY=<cle_api_mistral>

# Keycloak (prod uniquement)
KC_ADMIN_USER=admin
KC_ADMIN_PASSWORD=<mot_de_passe_keycloak>
KC_REALM=rag-muffin

# HuggingFace (optionnel)
HF_TOKEN=
```

---

## Plan de développement par micro-étapes

Chaque étape est une tâche isolée, testable, qui ne devrait pas prendre plus de 1-3 heures. Ne jamais passer à l'étape
suivante sans avoir validé la précédente.

### PHASE 1 — Squelette ✅ TERMINÉE

### PHASE 2 — Base de données et workspaces ✅ TERMINÉE

### PHASE 3 — Serveur d'embeddings ✅ TERMINÉE

### PHASE 4 — Qdrant ✅ TERMINÉE

### PHASE 5 — Pipeline RAG complet ✅ TERMINÉE

### PHASE 6 — Ingestion de documents réels ✅ TERMINÉE

### PHASE 7 — Authentification ✅ TERMINÉE

### PHASE 8 — Frontend minimal ✅ TERMINÉE

### PHASE 9 — Optimisation du pipeline RAG ✅ TERMINÉE

### PHASE 10 — Optimisations 2 ✅ TERMINÉE

### PHASE 11 — Traefik, HTTPS et sécurité (jours 38-41)

```
ÉTAPE 11.0 — HTTP Interfaces Spring Boot 4 (à la place de RestClient)
  - Utiliser les HTTP Interfaces natives Spring Boot 4 (@HttpExchange) pour les services :
    EmbeddingServiceImpl, IdpServiceImpl, RerankerServiceImpl, TikaServiceImpl et LlmServiceImpl
  - Approche : @HttpExchange sur une interface + RestClient.builder() pour construire le proxy
  - Pas de dépendance Feign — natif Spring Boot 4, intégration parfaite avec Jackson 3
  - Mettre en place un timeout via RestClient / ClientHttpRequestFactory
  - Tester le timeout avec (powershell) :
      $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Any,8090)
      $listener.Start()
      $client = $listener.AcceptTcpClient()
  🎯 Validé quand : mvn clean install passe et les timeouts sont actifs
      
ÉTAPE 11.1 — Dockerfile pour la production
  - Multi-stage : node pour le build, nginx:alpine pour servir les fichiers statiques
  - ng build → copier dist/frontend/browser/ dans Nginx
  - Ajouter au docker-compose avec le profil "prod"
  - Le frontend dev (ng serve) reste EN DEHORS de Docker
  🎯 Validé quand : docker build réussit et Nginx sert les fichiers statiques
  
ÉTAPE 11.2 — Ajouter Traefik au docker-compose
  - Image : traefik:v3
  - Ports : 80, 443
  - Volumes : /var/run/docker.sock (discovery), traefik_certs (Let's Encrypt)
  - Réseau : frontend-net
  - Activer le provider Docker et le resolver Let's Encrypt (ACME)
  🎯 Validé quand : Traefik démarre et le dashboard est accessible (temporairement)

ÉTAPE 11.3 — Configurer le routage via labels Docker
  - ancrage.fr → frontend:80 (Nginx servant les fichiers Vue.js buildés)
  - api.ancrage.fr → backend:8080
  - auth.ancrage.fr → keycloak:8080
  - Middlewares : headers de sécurité (X-Content-Type-Options, X-Frame-Options, HSTS),
    rate limiting, redirect HTTPS
  🎯 Validé quand : HTTPS fonctionne sur les 3 domaines avec certificats Let's Encrypt

ÉTAPE 11.4 — Sécuriser le serveur
  - SSH : port 2222, PermitRootLogin no, PasswordAuthentication no
  - UFW : allow 2222, 80, 443 uniquement + correction du bypass Docker
  - fail2ban : ban après 3 tentatives SSH échouées
  - unattended-upgrades : mises à jour de sécurité automatiques
  🎯 Validé quand : nmap depuis l'extérieur ne montre que 3 ports ouverts

ÉTAPE 11.5 — Retirer tous les ports exposés temporaires
  - Dans docker-compose : remplacer tous les "ports:" par "expose:"
    sauf Traefik (80/443)
  - Désactiver le dashboard Traefik en prod
  - Vérifier que le dashboard Qdrant, la console Keycloak, etc. ne sont plus
    accessibles directement depuis l'extérieur
  🎯 Validé quand : seuls ancrage.fr, api.ancrage.fr et auth.ancrage.fr sont accessibles
```

### PHASE 12 — Backups et monitoring (jours 42-44)

```
ÉTAPE 12.1 — Script de backup PostgreSQL
  - pg_dump quotidien dans /opt/backups/postgres/
  - Rotation : suppression des backups > 7 jours
  - Cron à 2h du matin
  🎯 Validé quand : un fichier .sql.gz est créé automatiquement chaque nuit

ÉTAPE 12.2 — Script de backup Qdrant
  - Snapshot de chaque collection via API REST
  - Stockage dans /opt/backups/qdrant/
  - Cron à 3h du matin
  🎯 Validé quand : les snapshots sont créés automatiquement

ÉTAPE 12.3 — Tester la restauration
  - Supprimer la base PostgreSQL de test, restaurer depuis le backup
  - Supprimer une collection Qdrant, restaurer depuis le snapshot
  - Documenter la procédure dans docs/RESTORE.md
  🎯 Validé quand : les données sont restaurées correctement

ÉTAPE 12.4 — Stack monitoring Grafana + Prometheus + Loki
  - Ajouter Prometheus au docker-compose : scrape Spring Boot (/actuator/prometheus) et Traefik
  - Ajouter Loki + Promtail : collecte des logs Docker de tous les conteneurs
  - Ajouter Grafana : dashboards pour métriques (Prometheus) et logs (Loki)
  - Configurer des alertes Grafana : conteneur down, espace disque, erreurs 5xx
  🎯 Validé quand : Grafana affiche les métriques Spring Boot et les logs applicatifs
```

### PHASE 13 — verticale parachutisme (jours 45-55)

```
ÉTAPE 13.1 — Implémenter le SkydivePlugin (première verticale)
  - 3 prompts par défaut créés à la création d'un workspace SKYDIVE :
    → "Formation" : cursus de progression (PAC, traditionnelle), brevets (A, B, C, D, BI),
      qualifications (vidéo, tandem, moniteur), pédagogie
    → "Matériel" : documentation technique, maintenance voilures/harnais/AAD,
      conformité, carnets de maintenance, inspections
    → "Législation" : réglementation DGAC, arrêtés, procédures de sécurité,
      espaces aériens, règles de largage, conditions météo
  - Sources partagées : documents FFP, réglementation DGAC
  🎯 Validé quand : un workspace SKYDIVE est créé avec ses 3 prompts par défaut

ÉTAPE 13.2 — Ingérer un corpus de test parachutisme
  - Ingérer 5-10 documents réels : extraits du MUP (Manuel d'Utilisation des Parachutes),
    réglementation DGAC, documents FFP, fiches techniques constructeurs
  - Les indexer comme corpus partagé (source: "shared", vertical: "SKYDIVE")
  - Tester avec les 3 prompts : question formation, question matériel, question législation
  🎯 Validé quand : le RAG répond correctement avec le bon ton selon le prompt choisi
```

### PHASE 14 — Améliorations

```
ÉTAPE 14.1 — Chiffrer les API key des LLM
- Ne pas stocker les API key des LLM en clair
  🎯 Validé quand : les API key des LLM ne sont pas stockées en clair
```


### PHASE 15 — Qualité RAG et évaluation (inspirée de la thèse Louis 2025, Maastricht University)

> Améliorations issues de l'analyse de la thèse "Machine Learning Solutions for Improving Access to Law"
> (Antoine Louis, Maastricht University, 2025). Applicable à toutes les verticales, critique pour la verticale
> juridique. Référence : https://cris.maastrichtuniversity.nl/ws/portalfiles/portal/257720757/c8717.pdf

```
ÉTAPE 15.1 — Framework d'évaluation structuré
  - Créer un dataset de test annoté dans backend/src/test/resources/eval/ :
    → Fichier JSON/YAML avec des paires (question → liste de chunk_ids pertinents attendus → réponse de référence)
    → Au moins 50 questions par verticale, annotées manuellement
  - Implémenter un EvaluationService qui calcule des métriques de retrieval :
    → Recall@k (k = 5, 10, 20) : proportion de chunks pertinents retrouvés dans le top-k
    → R-Precision : nombre de chunks pertinents dans le top-N / N (où N = nombre total de chunks pertinents)
    → MRR (Mean Reciprocal Rank) : inverse du rang du premier chunk pertinent
  - Créer un endpoint GET /api/eval/run?workspaceId=... (ADMIN uniquement) qui exécute
    l'évaluation sur le dataset de test et retourne les métriques
  - Ce framework servira de baseline pour mesurer l'impact de toutes les améliorations suivantes
  🎯 Validé quand : on peut lancer une évaluation automatique et obtenir des métriques chiffrées
    (Recall@10, R-Precision, MRR) sur le jeu de test

ÉTAPE 15.2 — Vérification post-génération des sources citées
  - Après chaque réponse LLM dans RagServiceImpl, parser les sources/références citées dans la réponse
  - Vérifier que chaque source citée correspond à un chunk réellement fourni dans le contexte
  - Ajouter un champ `verified_sources` (List<String>) et `hallucinated_sources` (List<String>)
    dans AskResponse pour signaler les sources vérifiées vs inventées
  - Si des sources hallucées sont détectées, ajouter un avertissement dans la réponse
  - Adapter PromptTemplates pour demander au LLM de citer les sources avec un format parsable
    (ex: [SOURCE:chunk_id] ou [DOC:filename§chunk_index])
  - La thèse Louis montre que même GPT-4 hallucine des sources dans ~35% des cas en domaine juridique
  🎯 Validé quand : une réponse citant une source inventée est signalée automatiquement dans la réponse

ÉTAPE 15.3 — Chunking structurel pour textes juridiques
  - Créer un StructuralChunkingService en complément du ChunkingService uniforme existant
  - Détection de patterns structurels dans le texte extrait :
    → Articles de loi : "Article L.xxx-xxx", "Art. xxx", "Article premier"
    → Sections/chapitres : "Chapitre X", "Titre X", "Section X"
  - Si des patterns sont détectés, découper par article/section (un chunk = un article entier)
    plutôt que par nombre de tokens
  - Conserver le chunking uniforme 512 tokens comme fallback pour les documents non structurés
  - Le choix de la stratégie de chunking peut être automatique (détection de patterns)
    ou configurable par workspace (cf. étape 10.2)
  - La thèse Louis montre que les articles de loi coupés en deux perdent leur sens sémantique
  🎯 Validé quand : un Code civil indexé est découpé par article (pas par blocs de 512 tokens),
    et les résultats de recherche juridique s'améliorent mesurés par le framework d'évaluation (15.1)

ÉTAPE 15.4 — Métadonnées structurelles dans Qdrant
  - Enrichir le payload Qdrant de chaque chunk avec des métadonnées structurelles :
    → `section_path` (String) : chemin hiérarchique complet (ex: "Code civil > Livre III > Titre VI > Art. 1240")
    → `section_level` (Int) : profondeur dans la hiérarchie (0 = document, 1 = livre, 2 = titre, etc.)
    → `parent_section` (String) : nom de la section parente directe
  - Extraire ces métadonnées lors du chunking structurel (étape 15.3)
  - Indexer `section_path` comme keyword index dans Qdrant pour permettre le filtrage par section
  - La thèse Louis (Chapitre 5) montre que les articles proches dans la hiérarchie partagent
    des concepts juridiques similaires — cette proximité structurelle améliore le retrieval
  🎯 Validé quand : un chunk dans Qdrant porte son chemin hiérarchique complet, et on peut
    filtrer les résultats par section (ex: "Code civil > Livre III")

ÉTAPE 15.5 — Poids RRF configurables pour la recherche hybride
  - Rendre le paramètre k de RRF configurable (défaut : 60, cf. Cormack et al., 2009)
  - Ajouter des poids relatifs dense_weight / sparse_weight dans la requête Qdrant
    (défaut : 1.0 / 1.0 = poids égaux, modifiable par workspace)
  - Ajouter ces paramètres comme colonnes dans la table workspaces (migration Liquibase)
    → rrf_k (INT, défaut 60), dense_weight (DOUBLE, défaut 1.0), sparse_weight (DOUBLE, défaut 1.0)
  - La thèse Louis (Chapitre 4) montre que les poids égaux sont corrects en zero-shot (notre cas actuel),
    mais que des poids calibrés améliorent les performances quand on a des données d'évaluation
  - Utiliser le framework d'évaluation (15.1) pour trouver les poids optimaux par verticale
  🎯 Validé quand : on peut modifier les poids RRF par workspace, et on a identifié les poids
    optimaux pour au moins une verticale via le framework d'évaluation

ÉTAPE 15.6 — Authority level pour le scoring juridique
  - Ajouter un champ `authority_level` (String, enum) dans le payload Qdrant des chunks :
    → EU_REGULATION, EU_DIRECTIVE, CONSTITUTION, NATIONAL_LAW, DECREE, ORDER, CASE_LAW, DOCTRINE, OTHER
  - Ce champ est renseigné à l'ingestion, soit automatiquement (détection par nom de fichier/source),
    soit manuellement par le workspace lors de l'upload
  - Utiliser l'authority_level comme facteur de boost dans le re-ranking :
    un article du Code civil ranké #5 avec authority_level=NATIONAL_LAW devrait être boosté
    par rapport à un arrêté municipal ranké #3 avec authority_level=ORDER
  - Ce boost est optionnel et configurable par workspace (activé par défaut pour la verticale juridique)
  - La thèse Louis (Section 6.2) souligne que ignorer la hiérarchie des normes peut conduire
    à des recommandations sémantiquement correctes mais juridiquement inadéquates
  🎯 Validé quand : pour une question de droit, un article de loi nationale est boosté
    par rapport à un arrêté local de pertinence sémantique équivalente

ÉTAPE 15.7 — Rationales au niveau paragraphe
  - Adapter le prompt RAG pour demander au LLM de citer les passages précis (paragraphes)
    des chunks qui fondent sa réponse, pas seulement le document entier
  - Appliquer la technique "Ground responses in quotes" recommandée par Anthropic :
    demander explicitement au LLM d'extraire d'abord les passages pertinents dans des
    balises <quotes> AVANT de rédiger sa réponse. Cela force le modèle à s'appuyer sur
    le texte source plutôt que sur ses connaissances paramétriques, et réduit les
    hallucinations. Référence : https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/use-xml-tags
    (section "Long context prompting" → "Ground responses in quotes")
  - Ajouter un champ `rationales` dans AskResponse : liste de (chunk_id, extrait de texte, confiance)
    → parser les balises <quotes> de la réponse LLM pour alimenter ce champ
  - Côté frontend (quand disponible) : surligner les passages cités dans les chunks source
  - La thèse Louis (Chapitre 3) montre que les rationales au niveau paragraphe sont plus utiles
    aux non-experts que les références à des articles entiers (souvent longs et difficiles à lire)
  🎯 Validé quand : la réponse RAG inclut des extraits précis des chunks sources,
    pas seulement des références aux documents entiers

ÉTAPE 15.8 — Préparation au fine-tuning domain-specific de BGE-M3
  - Créer un pipeline de collecte de paires (question, passage pertinent) à partir des interactions
    utilisateurs : quand un utilisateur pose une question et que la réponse est satisfaisante,
    sauvegarder la paire (question, top chunks utilisés) comme donnée d'entraînement
  - Stocker ces paires dans une table PostgreSQL `training_pairs` :
    → id UUID PK, workspace_id UUID, question TEXT, positive_chunk_text TEXT, negative_chunk_text TEXT,
       vertical VARCHAR, created_at TIMESTAMP WITH TIME ZONE
  - Créer un endpoint GET /api/eval/training-data?vertical=... (ADMIN)
    pour exporter les paires au format requis par FlagEmbedding pour le fine-tuning
  - Le fine-tuning effectif de BGE-M3 est hors scope (fait manuellement via un script Python
    quand on atteint ~500+ paires par verticale) — cette étape prépare seulement la collecte
  - La thèse Louis (Chapitres 2, 4) montre que même 200-500 paires suffisent pour un gain
    de +20-30% en recall avec un fine-tuning domain-specific
  🎯 Validé quand : les paires d'entraînement sont collectées automatiquement à partir
    des interactions et exportables au format FlagEmbedding
    
  ÉTAPE 15.9 — Stocker les questions/réponses RAG en base
    - Migration Liquibase dans db/changelog/migration/ :
      → CREATE TABLE rag_interactions (id UUID PK, workspace_id UUID NOT NULL REFERENCES workspaces(id),
         question TEXT NOT NULL, answer TEXT NOT NULL, chunk_ids UUID[] NOT NULL,
         created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now())
      → Index sur workspace_id
      → FK workspace_id ON DELETE CASCADE
    - Entité JPA RagInteraction + Repository (ddl-auto: validate)
    - Sauvegarder automatiquement chaque Q/R après un appel POST /api/rag/ask
    - Pas d'endpoint de lecture pour l'instant — les données servent à l'analytics
      et à la collecte de paires d'entraînement (étape 15.8)
    🎯 Validé quand : chaque question posée via le RAG est enregistrée en base avec sa réponse
      et les chunk_ids utilisés
      
  ÉTAPE 15.10 — Récupération adaptative en temps réel (taille de chunks variable)
  - Contexte : les chunks de taille fixe (512 tokens) sont un compromis imparfait.
    Une question factuelle n'a besoin que d'une phrase ; une question analytique nécessite
    parfois plusieurs paragraphes contigus. Cette étape introduit la stratégie Small-to-Big :
    indexer à granularité fine, récupérer à granularité adaptée.
  - Modifier le pipeline d'ingestion (ChunkingService) pour produire deux niveaux de chunks :
    → Chunks "enfants" (128 tokens, overlap 20) : unités d'indexation dans Qdrant
    → Chunks "parents" (512 tokens, les blocs actuels) : unités de contexte envoyées au LLM
    → Chaque chunk enfant stocke dans son payload Qdrant un `parent_chunk_id` (UUID)
      et le texte complet du chunk parent (`parent_text`)
  - Modifier RagServiceImpl pour utiliser les chunks parents au moment de la génération :
    → Qdrant recherche parmi les chunks enfants (plus précis sémantiquement)
    → Pour chaque chunk enfant retenu, récupérer son `parent_text` depuis le payload
    → Dédupliquer les parents (plusieurs enfants peuvent pointer vers le même parent)
    → Envoyer les textes parents au LLM (contexte plus complet)
  - Migration Liquibase : aucune (les deux niveaux vivent uniquement dans Qdrant via le payload)
  - Rendre le niveau de récupération configurable via une propriété app.rag.retrieval-level
    (valeurs : CHILD pour le comportement actuel, PARENT pour Small-to-Big ; défaut : PARENT)
  - Impact attendu : meilleure cohérence du contexte fourni au LLM sur les questions
    analytiques, sans sacrifier la précision du retrieval sur les questions factuelles
  - Particulièrement utile pour la verticale juridique (phase 17) : un article de loi de
    50 tokens coupé en chunk enfant reste compréhensible grâce au parent qui contient
    l'alinéa suivant. Complémentaire à l'étape 15.3 (chunking structurel par article).
  - Voir aussi : https://claude.ai/chat/f2f19310-1c11-4b67-a49b-ae1c1d5883f2
  🎯 Validé quand : une question analytique ("explique le processus de formation PAC")
    retourne des blocs de contexte cohérents et non tronqués, mesurés par le framework
    d'évaluation (15.1) avec un gain de Recall@5 par rapport au mode CHILD
```

### PHASE 16 — Verticale juridique (jours 56-62)

```
ÉTAPE 16.1 — Implémenter le LegalPlugin
  - System prompt spécialisé : vocabulaire juridique, citations d'articles,
    avertissement "consultez un avocat"
  - Chunking par article/section (détection de patterns "Article L.xxx-xxx")
  - Sources partagées : Légifrance
  🎯 Validé quand : les réponses utilisent le vocabulaire juridique et citent des articles

ÉTAPE 16.2 — Ingérer un corpus de test Légifrance
  - Télécharger manuellement 5-10 articles du Code civil (bail, propriété, etc.)
  - Les ingérer comme corpus partagé (source: "shared", vertical: "LEGAL")
  - Tester : poser des questions de droit immobilier
  🎯 Validé quand : le RAG répond en s'appuyant sur les vrais articles du Code civil

ÉTAPE 16.3 — Brancher l'API Légifrance (PISTE)
  - S'inscrire sur https://piste.gouv.fr/ pour obtenir les identifiants API
  - Implémenter un LegiFranceClient qui interroge l'API pour chercher des articles
  - Indexer automatiquement les résultats dans Qdrant (corpus partagé)
  🎯 Validé quand : on peut chercher un article de loi via l'API et l'indexer automatiquement

ÉTAPE 16.4 — Pseudonymisation avant appel LLM
  - Créer un PseudonymizationService qui remplace les noms propres, adresses,
    numéros de dossier par des tokens ([PERSONNE_1], [ADRESSE_1])
  - Appliquer avant chaque appel à l'API Mistral
  - Dé-pseudonymiser la réponse côté client
  - IMPORTANT : pour les avocats, c'est une obligation légale (secret professionnel)
  🎯 Validé quand : les noms propres n'apparaissent jamais dans les logs d'appels API
```

---

## Trois profils Spring, zéro changement de code

La bascule entre Docker Model Runner (dev), Mistral API (prod) et vLLM (futur) se fait **uniquement par configuration
** :

```yaml
# ── application-dev.yml (dev local — Docker Model Runner) ──
app.llm.base-url: http://localhost:12434/engines/v1
app.llm.api-key: EMPTY
app.llm.model: llama3.2:16k
# Réponse en ~3-8 secondes. Gratuit. Suffisant pour itérer.
```

```yaml
# ── application-prod.yml (production — API Mistral) ──
app.llm.base-url: https://api.mistral.ai/v1
app.llm.api-key: ${MISTRAL_API_KEY}
app.llm.model: mistral-small-latest
# Réponse en ~2-5 secondes. ~2-39 €/mois. Qualité supérieure (24B).
```

```yaml
# ── application-local-gpu.yml (futur — vLLM sur serveur GPU dédié) ──
app.llm.base-url: http://10.0.0.X:8000/v1
app.llm.api-key: EMPTY
app.llm.model: mistralai/Mistral-Small-3.1-24B-Instruct-2503
# Quand le volume justifie un serveur GPU. Même qualité que l'API, coût fixe.
```

Basculer avec `SPRING_PROFILES_ACTIVE=dev`, `prod` ou `local-gpu`.

Docker Model Runner, Mistral API et vLLM exposent tous les trois une API compatible OpenAI (`/v1/chat/completions`).
`LlmService` utilise `RestClient` pour appeler ces endpoints — pas de framework IA, appels HTTP directs.

**Point d'attention** : les modèles sont différents (Llama 3.2 3B en dev vs Mistral Small 24B en prod). Les prompts
peuvent donner des résultats légèrement différents. Toujours **rejouer le jeu de test de 50 questions** quand on change
de profil.

---

## Commandes utiles

```bash
# ── DEV LOCAL ──

# Lancer la stack de dev (télécharge ai/mistral-nemo automatiquement au premier lancement)
docker compose -f backend/docker-compose-dev.yml up -d

# Lancer le backend Spring Boot (hors Docker)
cd backend && mvn spring-boot:run

# Lancer le frontend Angular en dehors de Docker (hot reload)
cd frontend && ng serve
# → http://localhost:4200

# Tester le LLM directement
docker model run llama3.2:16k "Résume l'article 1240 du Code civil"

# Voir les logs d'un service
docker compose logs -f backend

# Reconstruire le backend après un changement de code
docker compose build backend && docker compose up -d backend

# Accéder au shell PostgreSQL
docker exec -it rag-postgres psql -U ragadmin ragapp

# Voir les collections Qdrant
curl http://localhost:6333/collections

# Tester le serveur BGE-M3
curl -s http://localhost:8090/embed_dense \
  -H 'Content-Type: application/json' -d '{"inputs":["test"]}'

# Vérifier que le GPU est bien utilisé par Docker Model Runner
docker model status

# Surveiller les ressources (RAM, CPU, GPU)
docker stats

# ── PRODUCTION ──

# Déployer sur le VPS OVH
SPRING_PROFILES_ACTIVE=prod docker compose --profile prod up -d

# Backup manuel
./scripts/pg_backup.sh
./scripts/qdrant_backup.sh
```

---

## Pièges à éviter

1. **Ne pas ajouter Keycloak trop tôt.** L'auth complique le développement. D'abord valider le pipeline RAG avec un
   simple workspaceId dans l'URL, puis ajouter Keycloak en phase 8.

2. **Ne pas exposer les ports Docker directement en prod.** Tout passe par Traefik. Les bases de données ne doivent
   JAMAIS être accessibles depuis Internet.

3. **Ne pas oublier le filtre workspace_id.** Chaque requête Qdrant DOIT filtrer par workspace_id. Un oubli = fuite de
   données
   entre clients.

4. **Docker Model Runner sans backend GPU = CPU = inutilisable.** Vérifier avec `docker model status` que le backend
   affiché est `cuda` (RTX 3070) ou `metal` (Mac M3). Sur Windows, activer avec
   `docker desktop enable model-runner --gpu enable`.

5. **Ne pas paniquer si le LLM est lent.** Llama 3.2 3B est rapide (~3-8 secondes sur RTX 3070). En prod, l'API Mistral
   Small répondra en 2-5 secondes mais avec une meilleure qualité (24B).

6. **Ne pas faire de chunking uniforme sur du texte juridique.** Les articles de loi doivent être découpés par article,
   pas par nombre de mots. Un article coupé en deux perd son sens.

7. **Tester la restauration des backups.** Un backup qu'on ne peut pas restaurer ne sert à rien. Tester chaque mois.

8. **Ne pas envoyer de données nominatives à l'API LLM.** Pseudonymiser AVANT l'appel. Pour les professions
   réglementées (avocats, notaires, experts-comptables), c'est une obligation pénale.

9. **Ne pas utiliser ddl-auto: update en production.** Passer à Flyway ou Liquibase avant le premier vrai client.

10. **Le premier lancement du serveur BGE-M3 prend 3-5 minutes** (téléchargement du modèle ~2 Go). La commande
    `docker model package` pour créer `llama3.2:16k` prend 5-15 minutes (réécriture du fichier GGUF). Ne pas croire que
    c'est planté.

11. **Docker contourne UFW par défaut.** Appliquer le fix iptables documenté dans la phase 9, sinon les ports Docker
    sont ouverts au monde entier malgré le firewall.

12. **Le frontend Angular tourne EN DEHORS de Docker en dev.** Avec `ng serve` directement sur ta machine, pas dans un
    conteneur. Le hot reload est instantané. Le Dockerfile Angular ne sert qu'en production (build statique + Nginx).

13. **Rejouer les tests quand on change de profil LLM.** Llama 3.2 3B (dev) et Mistral Small 24B (prod) ne réagissent
    pas exactement pareil aux mêmes prompts. Toujours valider sur les deux.

# Instructions pour Claude

- **Marquer les étapes comme réalisées dans ce fichier** dès qu'une étape du plan de développement est validée, en
  ajoutant `✅ RÉALISÉ` ou `✅ IGNORÉ` (avec justification) à côté du titre de l'étape.
- **Nettoyer les phases terminées** : quand toutes les étapes d'une phase sont `✅ RÉALISÉ` ou `✅ IGNORÉ`, remplacer le
  détail par une seule ligne résumé (ex: `PHASE 1 — Squelette ✅ TERMINÉE (étapes 1.1 à 1.5)`). Cela réduit la taille du
  fichier et libère du contexte.
