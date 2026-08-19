# RAG-Muffin

> AI-powered document assistant for French SMEs — ask questions, get sourced answers from your own files.

![Status](https://img.shields.io/badge/status-active%20development-yellow)
![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-green)
![Angular](https://img.shields.io/badge/Angular-21-red)
![License](https://img.shields.io/badge/license-AGPL%20v3-blue)

---

## What is this?

RAG-Muffin is a **multi-tenant, multi-vertical Retrieval-Augmented Generation (RAG) platform** that lets businesses
query their own documents using natural language. Users upload files (PDFs, Word documents, emails…), the system indexes
them, and when a question is asked, it retrieves the most relevant passages and generates a sourced answer.

**Multi-tenant** — each client operates in an isolated **workspace** (strict data separation; one client never sees
another's documents). **Multi-vertical** — the same technical core powers several business domains, each with its own
default prompts and RAG configuration.

The platform currently serves **6 verticals**:

| Vertical                        | Use case                                                            |
|---------------------------------|---------------------------------------------------------------------|
| **Generic**                     | Built-in general-purpose vertical, available out of the box         |
| **Parachutisme**                | DGAC regulations, technical manuals, safety procedures, training    |
| **Avocats / Notaires**          | Case law, statutes (Légifrance), contracts, legal deeds             |
| **Experts-comptables**          | Tax documents (BOFIP, CGI), accounting standards, anomaly detection |
| **Knowledge Management PME**    | Capture and query internal company knowledge                        |
| **Intelligence Administrative** | Aggregate emails, documents, scans and make them queryable          |

New verticals can be created by a super-admin, with custom prompts and RAG settings.

---

## Key Features

- **Flexible LLM backend** — compatible with any OpenAI-compatible API: run fully local (Docker Model Runner, Ollama,
  LM Studio, vLLM) or use any cloud provider (Mistral AI, OpenAI, Groq, Together.ai…); switched by config with zero
  code changes. Default recommendation: [Mistral AI](https://mistral.ai) (French company, EU-hosted, GDPR-compliant).
- **Local vector storage** — document embeddings are stored in a self-hosted [Qdrant](https://qdrant.tech) instance,
  never in a third-party cloud
- **Data sovereignty (by default)** — files, database, vectors and embeddings always stay on your infrastructure. The
  only **optional** external call is the LLM API (cloud providers only — fully avoidable with a local model).
- **Hybrid search** — dense (semantic) + sparse (lexical) via BGE-M3 and Qdrant, fused with RRF
- **Cross-encoder reranking** — BGE-Reranker-v2-M3 re-scores candidates after retrieval
- **Query rewriting** — optional LLM pre-step to reformulate vague questions before retrieval
- **Multi-tenant** — each client gets an isolated workspace; Qdrant payload filtering enforces strict data separation at
  the vector level
- **Multi-vertical** — pluggable business domains (legal, accounting…), each with dedicated default prompts
  and RAG settings; shared public corpora (regulations, standards) are served across all workspaces of a vertical
- **Async ingestion pipeline** — upload returns immediately; processing (extraction → chunking → embedding → indexing)
  happens in the background via ActiveMQ
- **Multi-format support** — PDFs, Word, PowerPoint, plain text, and more via Apache Tika
- **French-first** — BGE-M3 embeddings excel at French

---

## Architecture Overview

**RAG query pipeline:**

```
User question
 │
 ▼
LLM — query rewriting (optional) → reformulated question
 │
 ▼
BGE-M3 — embed question (dense + sparse)
 │
 ▼
Qdrant — hybrid search (RRF fusion) → top 50 candidates
 │
 ▼
BGE-Reranker-v2-M3 (TEI) — cross-encoder rescoring → top 10
 │
 ▼
LLM — generate sourced answer (local or cloud)
```

**Infrastructure:**

```
Spring Boot 4 (REST API)
 ├── PostgreSQL 18     — metadata, workspaces, users
 ├── Qdrant 1.17       — vector store (hybrid dense + sparse)
 ├── BGE-M3 (FastAPI)  — embeddings server
 ├── TEI (HuggingFace) — cross-encoder reranker
 ├── ActiveMQ Artemis  — async ingestion queue
 ├── Keycloak 26       — authentication (OAuth2/OIDC)
 └── LLM backend       — any OpenAI-compatible API (local or cloud)
```

---

## Tech Stack

| Layer            | Technology                                          |
|------------------|-----------------------------------------------------|
| Backend          | Spring Boot 4, Java 25, Virtual Threads             |
| Frontend         | Angular 21, PrimeNG, Signals                        |
| Vector DB        | Qdrant 1.17 (gRPC)                                  |
| Embeddings       | BGE-M3 via FastAPI + FlagEmbedding                  |
| Reranker         | BGE-Reranker-v2-M3 via HuggingFace TEI              |
| Relational DB    | PostgreSQL 18                                       |
| Message broker   | ActiveMQ Artemis                                    |
| Document parsing | Apache Tika 2.x                                     |
| Auth             | Keycloak 26 (prod) / JWT HS256 (dev)                |
| LLM (dev)        | Docker Model Runner + Llama 3.2 3B (`llama3.2:16k`) |
| LLM (prod)       | Configurable per workspace                          |
| Reverse proxy    | Traefik 3 (prod)                                    |
| Monitoring       | Grafana + Prometheus + Loki                         |

---

## Getting Started

See **[INSTALL-dev.md](INSTALL-dev.md)** for development setup and **[INSTALL-prod.md](INSTALL-prod.md)** for
production.

**Quick start (dev):**

```bash
# 1. Start the stack (downloads the LLM automatically on first run)
docker compose -f backend/docker-compose-dev.yml up -d

# 2. Start the backend
cd backend && mvn spring-boot:run

# 3. Install frontend dependencies and generate the API client (first time only)
cd frontend && npm install && npm run generate:api

# 4. Start the frontend (outside Docker, hot reload)
cd frontend && ng serve
```

Then open [http://localhost:4200](http://localhost:4200).

---

