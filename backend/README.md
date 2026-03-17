# Backend Quick Start

## Recommended Python version
Use **Python 3.11 or 3.12** for full GIS support (GeoPandas + Shapely).

If you use Python 3.14 on Windows CMD, Shapely may fail to build because GEOS headers/tools are missing.
This project now installs core API dependencies on 3.14, but GIS endpoints will return a clear 503 until GIS deps are available.

## Install (WSL recommended)
```bash
python3.12 -m venv .venv
source .venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt
```

## Install (Windows CMD)
```bat
py -3.12 -m venv .venv
.venv\Scripts\activate
python -m pip install --upgrade pip
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
