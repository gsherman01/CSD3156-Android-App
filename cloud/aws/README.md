# AWS Deployment (SAM, Lambda Container Image)

This folder contains a **ready-to-run AWS baseline** for this project:
- FastAPI backend on Lambda **container image** + API Gateway (HTTP API)
- Data storage in private S3 bucket (`uploads/`, `results/`)
- Frontend static hosting in public S3 website bucket

Using a container image avoids Lambda ZIP package-size limits for GeoPandas/Shapely dependencies.

## 1) Prerequisites

1. AWS account and IAM permissions for CloudFormation, Lambda, API Gateway, ECR, S3, and IAM role creation.
2. AWS CLI configured (`aws configure`).
3. AWS SAM CLI installed.
4. Docker running (Docker Desktop on Windows).

## 2) Deploy backend + buckets (Linux/macOS/Git Bash)

From repository root:

```bash
export AWS_REGION=ap-southeast-1
export APP_NAME=gis-cloud-api
export STAGE_NAME=prod
export STACK_NAME=${APP_NAME}-${STAGE_NAME}
export FRONTEND_BUCKET_NAME=<globally-unique-frontend-bucket>
export DATA_BUCKET_NAME=<globally-unique-data-bucket>
export CORS_ORIGINS=https://<your-frontend-domain>

bash cloud/aws/scripts/deploy.sh
```

## 3) Deploy backend + buckets (Windows PowerShell)

```powershell
$env:AWS_REGION="ap-southeast-1"
$env:APP_NAME="gis-cloud-api"
$env:STAGE_NAME="prod"
$env:STACK_NAME="$($env:APP_NAME)-$($env:STAGE_NAME)"
$env:FRONTEND_BUCKET_NAME="<globally-unique-frontend-bucket>"
$env:DATA_BUCKET_NAME="<globally-unique-data-bucket>"
$env:CORS_ORIGINS="https://<your-frontend-domain>"

bash cloud/aws/scripts/deploy.sh
```

After deploy, copy `ApiBaseUrl` and `FrontendWebsiteUrl` from outputs.

## 4) Publish frontend configured for API Gateway

```bash
export FRONTEND_BUCKET_NAME=<same-frontend-bucket>
export API_BASE_URL=<ApiBaseUrl from stack outputs>
export AWS_REGION=ap-southeast-1

bash cloud/aws/scripts/sync_frontend.sh
```

PowerShell equivalent:

```powershell
$env:FRONTEND_BUCKET_NAME="<same-frontend-bucket>"
$env:API_BASE_URL="<ApiBaseUrl from stack outputs>"
$env:AWS_REGION="ap-southeast-1"

bash cloud/aws/scripts/sync_frontend.sh
```

Open the `FrontendWebsiteUrl` output to test the app.

## 5) Smoke tests

```bash
curl "${API_BASE_URL}/health"
```

Expected:

```json
{"status":"ok","app":"GIS Cloud API","env":"aws"}
```

## 6) Suggested hardening before production

- Replace S3 website public hosting with CloudFront + Origin Access Control.
- Restrict CORS to exact frontend domain(s).
- Add WAF and request throttling.
- Add Cognito authorizer for protected API routes.
- Add lifecycle/retention policies on data bucket.

## Troubleshooting

- If CloudFormation reports `reserved keys` for Lambda environment variables, do not set platform-reserved names (for example `AWS_REGION`) in your template. Lambda already injects these at runtime.
- If SAM build fails on Windows, ensure Docker Desktop is running and `sam --version` (or `sam.cmd --version`) works in your shell.
