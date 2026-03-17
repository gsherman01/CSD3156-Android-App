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

### Run frontend locally
- Frontend is served by FastAPI static mount.
- Open `http://localhost:8000` in browser.

### Local file storage behavior
- Keep `STORAGE_PROVIDER=local`.
- Uploaded files are written to `UPLOAD_DIR` (default: `data/uploads`).

---

## 2) Render Deployment (Intermediate Step)

### How code is deployed from GitHub to Render
1. Push code to GitHub repository.
2. In Render dashboard, create a new **Web Service** from that GitHub repo.
3. Select branch (for example `main` or your working branch).

### Backend hosting on Render
- Runtime: Python.
- Build command (example):
  ```bash
  pip install -r requirements.txt
  ```
- Start command:
  ```bash
  uvicorn backend.main:app --host 0.0.0.0 --port ${PORT:-8000}
  ```

### Frontend serving on Render
- No separate frontend service is required in this architecture.
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
- Keep service logic in `backend/services/*` (already done) so route logic can be reused.
- Use `backend/lambda_handler.py` (Mangum adapter) as Lambda entrypoint.

### API Gateway replaces direct backend URL
- API Gateway becomes public HTTP endpoint.
- It forwards requests to Lambda.
- Existing route structure (`/api/upload`, `/api/analyze`, etc.) can remain the same.

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

### Must NOT be hardcoded
- Port number
- Upload path
- AWS bucket/region
- AWS credentials
- Environment mode

### Environment variable usage
- Local: safe defaults work out of the box.
- Render: set env vars in Render dashboard.
- AWS: set env vars in Lambda configuration / IAM role context.

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
- **Changes:** hosting location, URL, environment variable source, storage backend.
- **Stays same:** API endpoints, frontend behavior, overall user workflow, core GIS logic.

This stability is the main reason the architecture uses config + service abstraction.
