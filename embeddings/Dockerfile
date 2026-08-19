FROM python:3.12-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends gcc libc6-dev zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --upgrade pip \
    && pip install --no-cache-dir torch==2.7.0 --index-url https://download.pytorch.org/whl/cu128 \
    && pip install --no-cache-dir -r requirements.txt

COPY main.py .

ENV HF_HOME=/data

EXPOSE 80

ENV UVICORN_WORKERS=1

CMD uvicorn main:app --host 0.0.0.0 --port 80 --workers $UVICORN_WORKERS --no-access-log
