# Deployment Strategy: Local → Render → AWS (Exact Steps)

This guide gives you concrete commands to migrate this project to AWS safely.

---

## 1) Keep local development stable first

### Backend (local)
```bash
python3.12 -m venv .venv
source .venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt
uvicorn backend.main:app --reload --host 0.0.0.0 --port ${PORT:-8000}
```

Check:
```bash
curl http://localhost:8000/health
```

### Frontend (local)
Open:
- `http://localhost:8000`

---

## 2) AWS target architecture (what you are moving to)

- **Backend API**: API Gateway HTTP API → AWS Lambda (`backend.lambda_handler.handler`)
- **Data storage**: Private S3 bucket for uploads/results
- **Frontend hosting**: S3 static website bucket (or CloudFront in production)
- **Config model**: environment variables (`STORAGE_PROVIDER=aws`, bucket/region/CORS)

The repo now includes a ready AWS SAM template and deployment scripts:
- `cloud/aws/template.yaml`
- `cloud/aws/scripts/deploy.sh`
- `cloud/aws/scripts/sync_frontend.sh`

---

## 3) Exact AWS migration steps

## Step A — Install and authenticate tooling

1. Install AWS CLI
2. Install AWS SAM CLI
3. Configure credentials:
```bash
aws configure
```

Optional identity check:
```bash
aws sts get-caller-identity
```

## Step B — Choose names and region

Use globally unique bucket names:
```bash
export AWS_REGION=ap-southeast-1
export APP_NAME=gis-cloud-api
export STAGE_NAME=prod
export STACK_NAME=${APP_NAME}-${STAGE_NAME}

export FRONTEND_BUCKET_NAME=gis-cloud-frontend-<unique-suffix>
export DATA_BUCKET_NAME=gis-cloud-data-<unique-suffix>
```

Set your frontend origin for API CORS (use `*` only during initial testing):
```bash
export CORS_ORIGINS=https://<your-frontend-domain>
```

## Step C — Deploy Lambda + API Gateway + S3 buckets

From repo root:
```bash
bash cloud/aws/scripts/deploy.sh
```

This command:
- Builds and deploys the SAM stack
- Creates Lambda + API Gateway HTTP API
- Creates frontend website bucket + policy
- Creates private data bucket
- Prints stack outputs (including `ApiBaseUrl` and `FrontendWebsiteUrl`)

## Step D — Publish frontend with API URL injected

Use the API URL returned from Step C:
```bash
export API_BASE_URL=<ApiBaseUrl-from-stack-output>
bash cloud/aws/scripts/sync_frontend.sh
```

This uploads:
- `frontend/index.html`
- `frontend/app.js`
- generated `config.js` containing your API URL

## Step E — Run smoke tests

```bash
curl "${API_BASE_URL}/health"
```
Expected:
```json
{"status":"ok","app":"GIS Cloud API","env":"aws"}
```

Then open your `FrontendWebsiteUrl`, upload a sample GeoJSON, and run one analysis operation.

---

## 4) App configuration behavior after migration

- Backend automatically switches to S3 when `STORAGE_PROVIDER=aws`.
- Lambda uses IAM role permissions from the SAM template (no hardcoded secrets required).
- Frontend reads `window.__APP_CONFIG__.API_BASE_URL` from `config.js`, so you can redeploy frontend without editing source code.

---

## 5) Production hardening checklist

Before final production cutover, do these:

1. Replace S3 website with CloudFront + Origin Access Control.
2. Restrict CORS to exact frontend URL(s) only.
3. Add AWS WAF and API throttling.
4. Add Cognito authorizer for protected API routes.
5. Add CloudWatch alarms (5XX count, Lambda errors, duration, throttles).
6. Add S3 lifecycle policies for upload/result retention.

---

## 6) Rollback strategy

If a deployment fails:
- Use previous CloudFormation stack state (automatic rollback).
- Keep old frontend `config.js` and re-sync known working assets.
- Switch DNS / user links back only after `/health` and one analysis endpoint both pass.

