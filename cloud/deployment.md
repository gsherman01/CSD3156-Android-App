# Cloud Deployment Notes

## Local / Render mode
- Install dependencies from `requirements.txt`.
- Default storage mode is local (`STORAGE_PROVIDER=local`).
- Uploads are written under `UPLOAD_DIR` (default `data/uploads`), using relative paths.

## Render start command
Use a dynamic PORT and bind to all interfaces:

```bash
uvicorn backend.main:app --host 0.0.0.0 --port ${PORT:-8000}
```

This is compatible with local execution and Render deployment.

## AWS-ready configuration (optional)
To switch storage to S3 without changing API endpoints:

- `STORAGE_PROVIDER=aws`
- `AWS_REGION=...`
- `S3_BUCKET_NAME=...`
- credentials via IAM role (preferred) or env vars:
  - `AWS_ACCESS_KEY_ID`
  - `AWS_SECRET_ACCESS_KEY`
  - `AWS_SESSION_TOKEN` (optional)

## Monitoring/logging
- App logs are emitted to stdout using Python logging.
- Request-level logs include method, path, status, and latency.
- Upload and processing steps emit operational logs.
