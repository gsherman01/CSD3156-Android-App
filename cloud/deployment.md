# Deployment Strategy: Local → Render → AWS

This guide is intentionally beginner-friendly and practical.

---

## 1) Development Flow (Local First)

### Where developers work
- Write code locally in VS Code (or any IDE).
- Use terminal (CMD/PowerShell/WSL) for setup and running the app.

### Run backend locally
1. Create virtual environment and install dependencies.
2. Start FastAPI with Uvicorn:
   ```bash
   uvicorn backend.main:app --reload --host 0.0.0.0 --port ${PORT:-8000}
   ```
3. Backend is available at `http://localhost:8000`.
4. API docs are available at `http://localhost:8000/docs`.

### Run frontend locally
- Frontend is served by FastAPI static mount.
- Open `http://localhost:8000` in browser.

### Local file storage behavior
- Keep `STORAGE_PROVIDER=local`.
- Uploaded files are written to `UPLOAD_DIR` (default: `data/uploads`).
- Analysis results are written separately to `RESULTS_DIR` (default: `data/results`).
- Lightweight dataset metadata is tracked in `METADATA_REGISTRY_PATH` for demo/Render workflows.

---

## 2) Render Deployment (Intermediate Step)

### How code is deployed from GitHub to Render
1. Push code to GitHub repository.
2. In Render dashboard, create a new **Web Service** from that GitHub repo.
3. Select branch (for example `main` or your working branch).

### Backend hosting on Render
- Runtime: Python 3.12 recommended.
- Add `runtime.txt` with a Python 3.12 value so Render does not default to a newer runtime that skips GeoPandas.
- Build command:
  ```bash
  pip install -r requirements.txt
  ```
- Start command:
  ```bash
  uvicorn backend.main:app --host 0.0.0.0 --port ${PORT:-8000}
  ```

### Frontend serving on Render
- No separate frontend service is required in this intermediate architecture.
- FastAPI serves frontend files directly at the root path (`/`).

### Required Render configuration
- Render injects `PORT` automatically.
- Important env vars:
  - `APP_ENV=render`
  - `LOG_LEVEL=INFO`
  - `STORAGE_PROVIDER=local` (or `aws` if testing S3 mode)
  - `UPLOAD_DIR=data/uploads`
- Optional S3 mode on Render:
  - `AWS_REGION`
  - `S3_BUCKET_NAME`
  - `AWS_ACCESS_KEY_ID`
  - `AWS_SECRET_ACCESS_KEY`
  - `AWS_SESSION_TOKEN` (optional)

---

## 3) AWS Migration Plan (Final Target)

### Backend to AWS Lambda
- Keep service logic in `backend/services/*` so route logic can be reused.
- Use `backend/lambda_handler.py` (Mangum adapter) as Lambda entrypoint.
- Recommended Lambda baseline:
  - Runtime: Python 3.12
  - Memory: 512 MB minimum
  - Timeout: 60 seconds minimum
  - Increase timeout to 300 seconds for heavier GIS tasks if needed

### Important Lambda packaging note
- GeoPandas and related GIS libraries are large.
- ZIP deployment may hit Lambda size limits.
- Recommended approach:
  1. Try a prebuilt GeoPandas Lambda layer first.
  2. If that is not practical in your Learner Lab, deploy Lambda as a container image.

### API Gateway replaces direct backend URL
- API Gateway becomes public HTTP endpoint.
- It forwards requests to Lambda.
- Existing route structure (`/api/upload`, `/api/analyze`, etc.) can remain the same.
- Test `GET /health` through API Gateway after deployment.

### Frontend in final AWS setup
- Do **not** serve frontend from Lambda.
- Host frontend separately, typically on S3 static website hosting (optionally with CloudFront later).
- Update frontend API base URL to your API Gateway URL.

### S3 replaces local file storage
- Set `STORAGE_PROVIDER=aws`.
- Upload/read flows use boto3 and `s3://...` URIs.
- No endpoint contract change needed because storage is abstracted in service layer.

### Cognito integration path
- Add Cognito User Pool for login.
- Configure API Gateway authorizer with Cognito.
- Protect selected API routes while keeping public routes (like health) open.

---

## 4) Key Design Decisions (to avoid rewrites)

### Built now for future migration
- Storage abstraction (`local` vs `aws`) behind one service API.
- Centralized config via environment variables.
- Route layer handles HTTP only; heavy logic stays in service modules.
- Static frontend mount is disabled automatically in Lambda environments.

### Must NOT be hardcoded
- Port number
- Upload path
- AWS bucket/region
- AWS credentials
- Environment mode
- API Gateway production URL

### Environment variable usage
- Local: safe defaults work out of the box.
- Render: set env vars in Render dashboard.
- AWS: set env vars in Lambda configuration or rely on IAM role.

### Production CORS note
- `allow_origins=["*"]` is okay for development.
- In production, change it to your actual frontend URL(s), such as:
  - local testing URL
  - Render URL
  - S3 frontend URL
- In API Gateway, ensure CORS/OPTIONS is enabled for browser preflight requests.

---

## 5) Local vs Cloud Execution (Concrete View)

### Mode A: Local Development Mode
- Code runs on your own machine.
- You start backend manually with Uvicorn.
- Frontend is opened via `http://localhost:8000`.
- Files are saved to local folder (`data/uploads`).
- User experience: quick testing and debugging.

### Mode B: Cloud Deployment Mode
- Code runs on Render (now) or AWS (later).
- Platform starts backend using the start command.
- Frontend is accessed via public service URL.
- Files are local temporary storage on Render, or S3 in AWS mode.
- User experience: public access over internet, cloud logs, persistent service URL.

### What changes when moving Local → Render → AWS
- **Changes:** hosting location, URL, environment variable source, storage backend, and final frontend hosting location.
- **Stays same:** API endpoints, frontend workflow, request/response structure, and core GIS logic.

This stability is the main reason the architecture uses config + service abstraction.
