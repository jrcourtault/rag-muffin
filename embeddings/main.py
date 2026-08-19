import logging
import os
from contextlib import asynccontextmanager

os.environ["HF_HUB_DISABLE_TELEMETRY"] = "1"

from FlagEmbedding import BGEM3FlagModel
from fastapi import FastAPI
from pydantic import BaseModel

logger = logging.getLogger("uvicorn")

model: BGEM3FlagModel | None = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    global model
    logger.info("Loading BGE-M3 model (this may take a few minutes on first launch)...")
    model = BGEM3FlagModel("BAAI/bge-m3", use_fp16=False)
    os.environ["HF_HUB_OFFLINE"] = "1"
    logger.info("BGE-M3 model loaded successfully (HuggingFace Hub now offline)")
    yield


app = FastAPI(lifespan=lifespan)


class EmbedRequest(BaseModel):
    inputs: list[str]


@app.post("/embed_dense")
def embed_dense(request: EmbedRequest):
    result = model.encode(request.inputs, return_dense=True, return_sparse=False)
    return result["dense_vecs"].tolist()


@app.post("/embed_sparse")
def embed_sparse(request: EmbedRequest):
    result = model.encode(request.inputs, return_dense=False, return_sparse=True)
    sparse_vecs = []
    for vec in result["lexical_weights"]:
        entries = [{"index": int(idx), "value": float(val)} for idx, val in vec.items()]
        sparse_vecs.append(entries)
    return sparse_vecs


@app.get("/health")
def health():
    return {"status": "ok", "model": "BAAI/bge-m3"}
