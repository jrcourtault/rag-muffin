# Serveur d'embeddings BGE-M3

Serveur FastAPI qui charge le modèle [BAAI/bge-m3](https://huggingface.co/BAAI/bge-m3) une seule fois et expose des
embeddings **dense** et **sparse** via deux endpoints HTTP.

## Pourquoi ce serveur ?

HuggingFace TEI (Text Embeddings Inference) ne supporte pas le mode SPLADE pour les modèles CamemBERT (tensor
`lm_head.decoder.weight` absent dans les safetensors à cause des poids liés). Plutôt que d'utiliser deux conteneurs TEI
séparés (un pour le dense, un pour le sparse avec un modèle BERT anglais inadapté au français), ce serveur unique charge
BGE-M3 et sert les deux types de vecteurs depuis le même modèle.

Avantages :

- **Un seul modèle** au lieu de deux (~2 Go au lieu de ~4 Go)
- **Sparse en français** : BGE-M3 produit des lexical weights de qualité en français, contrairement aux modèles SPLADE
  basés sur BERT anglais
- **Dense + sparse cohérents** : les deux représentations viennent du même modèle, entraîné conjointement

## Endpoints

| Méthode | URL             | Description                                       |
|---------|-----------------|---------------------------------------------------|
| POST    | `/embed_dense`  | Vecteurs denses (1024 dimensions, float32)        |
| POST    | `/embed_sparse` | Vecteurs sparse (lexical weights, indices+values) |
| GET     | `/health`       | Health check                                      |

### Format des requêtes

```json
{
  "inputs": [
    "texte 1",
    "texte 2"
  ]
}
```

### Format des réponses

**Dense** — liste de vecteurs (un par input) :

```json
[
  [
    0.123,
    -0.456
  ],
  [
    0.789,
    -0.012
  ]
]
```

**Sparse** — liste de listes d'entrées `{index, value}` (un par input) :

```json
[
  [
    {
      "index": 42,
      "value": 0.85
    },
    {
      "index": 1337,
      "value": 0.42
    }
  ]
]
```

## Lancement local (hors Docker)

```bash
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8090
```

Le premier lancement télécharge le modèle BGE-M3 (~2 Go).

## Lancement via Docker Compose

Depuis la racine du projet :

```bash
docker build -t rag-muffin-embeddings .
docker run -d -p 8090:80 -v ./volumes/embeddings:/data rag-muffin-embeddings
```

Le serveur est accessible sur `http://localhost:8090`.

## Test rapide

```bash
# Dense
curl -s http://localhost:8090/embed_dense \
  -H 'Content-Type: application/json' \
  -d '{"inputs":["Quelles sont les conditions pour un saut en tandem ?"]}'

# Sparse
curl -s http://localhost:8090/embed_sparse \
  -H 'Content-Type: application/json' \
  -d '{"inputs":["Quelles sont les conditions pour un saut en tandem ?"]}'

# Health
curl -s http://localhost:8090/health
```

## GPU / CPU

Par défaut, le `Dockerfile` installe **PyTorch CUDA 12.8** (GPU NVIDIA). Compatible RTX 3070 et supérieur
(Ampere, Ada, Blackwell). Sur machine sans GPU NVIDIA, il faut repasser en CPU.

**Repasser en CPU** — modifier `Dockerfile` :

```dockerfile
# Remplacer :
pip install --no-cache-dir torch --index-url https://download.pytorch.org/whl/cu128
# Par :
pip install --no-cache-dir torch --index-url https://download.pytorch.org/whl/cpu
```

## Stack

- **Python 3.12**
- **FastAPI** — serveur HTTP
- **FlagEmbedding** (`BGEM3FlagModel`) — chargement et inférence du modèle BGE-M3
- **uvicorn** — serveur ASGI (1 worker, le modèle occupe ~2-3 Go de RAM)
