# Backend

API REST du projet RAG-Muffin.

## Lancement

### 1. Démarrer les services Docker

```bash
docker compose -f backend/docker-compose-dev.yml up -d
```

### 2. Lancer le backend

```bash
cd backend
mvn spring-boot:run
```

L'API est accessible sur `http://localhost:8080`.

## URLs utiles (dev uniquement)

| URL                                         | Description                                                         |
|---------------------------------------------|---------------------------------------------------------------------|
| http://localhost:8080/swagger-ui/index.html | Swagger UI — documentation interactive de l'API                     |
| http://localhost:8080/v3/api-docs           | Spec OpenAPI 3 (JSON) — utilisée par `ng-openapi-gen` côté frontend |
| http://localhost:8080/actuator/health       | Health check                                                        |
| http://localhost:8161                       | Console ActiveMQ Artemis (admin/admin)                              |
| http://localhost:6333/dashboard             | Dashboard Qdrant                                                    |
| http://localhost:9998                       | Tika Server — extraction texte + OCR (Tesseract inclus)             |

Swagger UI et `/v3/api-docs` sont désactivés en production (voir `application-prod.yaml`).

