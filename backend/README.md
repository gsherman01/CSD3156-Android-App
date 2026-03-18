# Backend Quick Start

## Recommended Python version
Use **Python 3.12** for Lambda compatibility and full GIS support (GeoPandas + Shapely).

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
- `POST /api/upload` — upload GeoJSON and record lightweight dataset metadata.
- `GET /api/spatial-query` — run `buffer` or `nearest` on an uploaded dataset.
- `GET /api/analyze` — run `buffer`, `intersection`, or `nearest`, return GeoJSON, and save a result file.
- `GET /health` — service health check.
- `GET /docs` — FastAPI interactive API documentation.

## Environment config
Use `cloud/configs/env.example` as the source of truth for runtime variables.

## Local vs Cloud (simple explanation)
- **Local mode:** you start the server from your terminal, open localhost, and files are saved in `data/uploads`.
- **Render mode:** Render starts the same app command automatically, users open a Render URL, and env vars are set in the Render dashboard.
- **AWS target mode:** API runs behind API Gateway + Lambda, frontend should be hosted separately (for example S3), and storage moves to S3 while routes stay the same.

## Production notes
- For AWS Lambda, do **not** rely on FastAPI serving `frontend/`; host frontend separately.
- For S3-hosted frontend + API Gateway backend, replace `YOUR_API_GATEWAY_URL_HERE` in `frontend/app.js`.
- Tighten CORS to your actual frontend URL before production deployment.
- Recommended Lambda baseline:
  - Runtime: Python 3.12
  - Memory: 512 MB minimum
  - Timeout: 60 seconds minimum

## AWS adaptation points
- `backend/services/storage_service.py` for S3/local storage switching.
- `backend/lambda_handler.py` for Lambda deployment integration.
- `backend/main.py` for request logging, request IDs, and centralized error handling.
- `cloud/deployment.md` for step-by-step migration strategy.


## Demo dataset
- Use `data/demo/singapore_demo_points.geojson` as a small Singapore sample dataset for upload/testing.
- Coordinates were adapted from the public `horensen/sg-areas` README examples for Singapore planning areas/subzones.
