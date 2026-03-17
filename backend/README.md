# Backend Quick Start

## Install
```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Run locally
```bash
uvicorn backend.main:app --reload --host 0.0.0.0 --port 8000
```

Then open `http://localhost:8000`.

## API endpoints
- `POST /api/upload` — upload GeoJSON.
- `GET /api/spatial-query` — run `buffer` or `nearest` on an uploaded dataset.
- `GET /health` — service health check.

## AWS adaptation points
- `backend/services/storage_service.py` for S3 upload implementation.
- `backend/lambda_handler.py` for Lambda deployment integration.
- `cloud/configs/env.example` for environment variable contract.
