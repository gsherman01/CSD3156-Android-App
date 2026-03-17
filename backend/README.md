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
uvicorn backend.main:app --reload --host 0.0.0.0 --port ${PORT:-8000}
```

Then open `http://localhost:8000`.

## Render start command
```bash
uvicorn backend.main:app --host 0.0.0.0 --port ${PORT:-8000}
```

## API endpoints
- `POST /api/upload` — upload GeoJSON.
- `GET /api/spatial-query` — run `buffer` or `nearest` on an uploaded dataset.
- `GET /api/analyze` — run `buffer`, `intersection`, or `nearest` and return GeoJSON.
- `GET /health` — service health check.

## Environment config
Use `cloud/configs/env.example` as the source of truth for runtime variables.

## Local vs Cloud (simple explanation)
- **Local mode:** you start the server from your terminal, open localhost, and files are saved in `data/uploads`.
- **Render mode:** Render starts the same app command automatically, users open a Render URL, and env vars are set in the Render dashboard.
- **AWS target mode:** API runs behind API Gateway + Lambda, storage moves to S3, but routes and frontend workflow remain the same.

## AWS adaptation points
- `backend/services/storage_service.py` for S3/local storage switching.
- `backend/lambda_handler.py` for Lambda deployment integration.
- `backend/main.py` for request logging and centralized error handling.
- `cloud/deployment.md` for step-by-step migration strategy.
