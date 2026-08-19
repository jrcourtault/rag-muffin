# Installation Guide — Development

## Prerequisites

- Docker Desktop (with Docker Model Runner enabled)
- Java 25
- Maven

### Docker Desktop RAM

If Docker doesn't have enough RAM available, here's how to increase the limit:

**On macOS:** Docker Desktop > Settings > Resources > Memory → increase to 16 GB → Apply & Restart.

**On Windows (WSL2):** Create or edit `%USERPROFILE%\.wslconfig`:

```ini
[wsl2]
memory=16GB
```

Then restart WSL:

```powershell
wsl --shutdown
```

**On Linux:** Nothing to do — Docker runs natively and uses all available RAM.

## Docker Model Runner

The LLM runs via Docker Model Runner (built into Docker Desktop). It is **not** declared in `docker-compose-dev.yml` —
it is managed separately with `docker model` commands.

### 1. Enable TCP port (required, one-time)

The Spring Boot backend (running outside Docker) communicates with Model Runner over TCP. Enable TCP port 12434:

```bash
docker desktop enable model-runner --tcp=12434
```

Verify:

```bash
curl http://localhost:12434/engines/v1/models
# → should return {"object":"list","data":[]} — empty list is fine, TCP port is working
```

### 2. On PC with RTX GPU — enable CUDA backend (one-time)

```bash
docker desktop enable model-runner --gpu enable
docker model status
# → should display backend "cuda"
```

On Mac M3, nothing to do — Metal is detected automatically.

### 3. Pull and configure the model (one-time)

```bash
# Download Llama
docker model pull ai/llama3.2
docker model package --from ai/llama3.2 --context-size 16384 llama3.2:16k

# Download Mistral 7B (~4 Go VRAM)
docker model pull ai/mistral 
docker model package --from ai/mistral --context-size 16384 mistral-7b:16k

# Or download the Mistral Nemo 12B model (~7 Go VRAM)
docker model pull ai/mistral-nemo                                                                                                                                                                                                 
docker model package --from ai/mistral-nemo --context-size 32768 mistral-nemo:32k
### Modify application-dev.yml -> app.llm.model: mistral-nemo:32k
```

### 4. Test the LLM

```bash
# Llama
docker model run llama3.2:16k "Hello, does this work?"

# Mistral 7B
docker model run mistral-7b:16k "Hello, does this work?"

# Mistral Nemo
docker model run mistral-nemo:32k "Salut, comment ça va ?"
```

> **If you get `Failed to generate a response: error response: status=500 body=unable to load runner`**, restart Docker
> Desktop.

Or via the API:

```bash
curl http://localhost:12434/engines/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama3.2:16k",
    "messages": [{"role": "user", "content": "Hello, does this work?"}]
  }'
```

## Start the Dev Stack

```bash
docker compose -f backend/docker-compose-dev.yml up -d
```

**Note: wait for the embedding server to finish loading its models before making requests.**

> **First start:** the reranker downloads BAAI/bge-reranker-v2-m3 on first launch. It may take a few minutes.
> Check readiness with:
> ```bash
> curl -v http://localhost:8091/health
> # → HTTP/1.1 200 OK  (empty body — 200 means ready)
> ```

## Running embeddings and reranker on CPU (no NVIDIA GPU)

If your machine has no NVIDIA GPU, both the embeddings server and the reranker need to run on CPU.

### Embeddings (BGE-M3)

See `embeddings/README.md` (section **GPU / CPU**) for instructions on switching to CPU.

### Reranker (BGE-Reranker-v2-M3)

The TEI Docker image `ghcr.io/huggingface/text-embeddings-inference:86-1.9` requires an NVIDIA GPU.
Two options:

**Option A — Disable the reranker (simplest)**

The reranker improves precision but is not required. Disable it in the Workspace settings,
then comment out the `reranker` service in `docker-compose-dev.yml`.

**Option B — Run TEI via the CPU image**

Replace the reranker image in `docker-compose-dev.yml`:

```yaml
reranker:
  image: ghcr.io/huggingface/text-embeddings-inference:cpu-1.9
  command: --model-id BAAI/bge-reranker-v2-m3
  # remove the deploy block
```

Inference will be significantly slower on CPU (~1-3 s/request vs ~50 ms on GPU).

## Manage the LLM Models

### List installed models

```bash
docker model list
```

### Remove a model

```bash
docker model rm ai/mistral-nemo
```

### Install a different model (optional)

To use a different model, pull and configure it manually, then update the application config:

```bash
# Pull and package with 16k context window
docker model pull ai/llama3.2

# Create a package with 16k context window
docker model package --from ai/llama3.2 --context-size 16384 llama3.2:16k

# Update application-dev.yaml
app.llm.model: llama3.2:16k
```

Browse available models: `docker model list` after pulling, or check Docker Hub.

## Start the Backend

```bash
cd backend && mvn spring-boot:run
```

## First admin user

`keycloak/realm-export-dev.json` already seeds a first user with the `ADMIN` role in the `rag-muffin` realm, so
there's nothing to set up manually:

- Email / username: `admin@rag-muffin.fr`
- Password: `admin`

This user can log into the application and manage workspaces and verticals. The Keycloak admin console itself
(distinct from this application user) is available at [http://localhost:8081](http://localhost:8081), login
`admin` / `admin` (credentials defined in `docker-compose-dev.yml`).

## Start the Frontend

```bash
# Install dependencies and generate the API client (first time only)
cd frontend && npm install && npm run generate:api

# Start the dev server (hot reload)
cd frontend && ng serve
```

The frontend runs outside Docker with hot reload. Open [http://localhost:4200](http://localhost:4200).

## Service URLs

### Web UIs

| Service                   | URL                               |
|---------------------------|-----------------------------------|
| Frontend                  | http://localhost:4200             |
| Backend                   | http://localhost:8080             |
| Keycloak                  | http://localhost:8081             |
| Qdrant dashboard (Web UI) | http://localhost:6333/dashboard   |
| Qdrant dashboard          | http://localhost:6334             |
| ActiveMQ Artemis (Web UI) | http://localhost:8161             |
| ActiveMQ Artemis          | http://localhost:61616            |
| Mailpit (Web UI)          | http://localhost:8025             |
| Embeddings (BGE-M3)       | http://localhost:8090             |
| Reranker (TEI)            | http://localhost:8091             |
| LLM (Docker Model Runner) | http://localhost:12434/engines/v1 |
