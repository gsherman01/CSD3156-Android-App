# Cloud Deployment Notes

## Local / Render mode
- Run with Uvicorn directly.
- Use `STORAGE_PROVIDER=local` to store uploads on instance disk.

## AWS migration path
1. Package FastAPI app with Mangum for Lambda.
2. Frontend static files should move to S3 + CloudFront.
3. Set `STORAGE_PROVIDER=aws` and configure:
   - `AWS_REGION`
   - `S3_BUCKET_NAME`
4. Put API behind API Gateway.
5. Add Cognito authorizer to protected routes.

## Why this structure is migration-ready
- `backend/services/storage_service.py` isolates S3/local storage behavior.
- `backend/services/gis_service.py` keeps processing logic independent from transport.
- `backend/routes/*` only handle HTTP concerns.
