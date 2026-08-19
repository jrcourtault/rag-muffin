#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="backend/docker-compose-dev.yml"
SERVICE="embeddings"

echo "=== Audit des dependances Python ($SERVICE) ==="
docker compose -f "$COMPOSE_FILE" exec "$SERVICE" pip-audit

echo ""
echo "=== Audit termine ==="
