#!/usr/bin/env bash
set -euo pipefail

# Check for SAM CLI (Linux/Mac or Windows)
if ! (command -v sam >/dev/null 2>&1 || command -v sam.cmd >/dev/null 2>&1); then
  echo "Error: AWS SAM CLI is not installed. Install from https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html"
  exit 1
fi

# Determine SAM CLI command
if command -v sam >/dev/null 2>&1; then
  SAM_CMD="sam"
elif command -v sam.cmd >/dev/null 2>&1; then
  SAM_CMD="sam.cmd"
else
  echo "Error: SAM CLI not found"
  exit 1
fi

APP_NAME="${APP_NAME:-gis-cloud-api}"
STAGE_NAME="${STAGE_NAME:-prod}"
AWS_REGION="${AWS_REGION:-ap-southeast-1}"
STACK_NAME="${STACK_NAME:-${APP_NAME}-${STAGE_NAME}}"

# Keep defaults for convenience (can still be overridden by env vars).
FRONTEND_BUCKET_NAME="${FRONTEND_BUCKET_NAME:-gis-cloud-frontend-3sktb}"
DATA_BUCKET_NAME="${DATA_BUCKET_NAME:-gis-cloud-data-3sktb}"

if [[ -z "${FRONTEND_BUCKET_NAME}" || -z "${DATA_BUCKET_NAME}" ]]; then
  echo "Error: FRONTEND_BUCKET_NAME and DATA_BUCKET_NAME must be set."
  exit 1
fi

CORS_ORIGINS="${CORS_ORIGINS:-http://gis-cloud-frontend-3sktb.s3-website-ap-southeast-1.amazonaws.com}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE_PATH="${SCRIPT_DIR}/../template.yaml"

# Optional Python override for SAM build environments where Python 3.12 path is non-default.
if command -v python3.12 >/dev/null 2>&1; then
  export PYTHON_EXE="${PYTHON_EXE:-$(command -v python3.12)}"
fi

echo "Using SAM command: ${SAM_CMD}"
echo "Building SAM application (container-image Lambda)..."
"${SAM_CMD}" build --template-file "${TEMPLATE_PATH}"

echo "Deploying stack ${STACK_NAME} in ${AWS_REGION}..."
"${SAM_CMD}" deploy \
  --stack-name "${STACK_NAME}" \
  --region "${AWS_REGION}" \
  --capabilities CAPABILITY_IAM \
  --resolve-s3 \
  --resolve-image-repos \
  --no-confirm-changeset \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
    AppName="${APP_NAME}" \
    StageName="${STAGE_NAME}" \
    FrontendBucketName="${FRONTEND_BUCKET_NAME}" \
    DataBucketName="${DATA_BUCKET_NAME}" \
    CorsOrigins="${CORS_ORIGINS}"

echo "Stack outputs:"
aws cloudformation describe-stacks \
  --stack-name "${STACK_NAME}" \
  --region "${AWS_REGION}" \
  --query 'Stacks[0].Outputs[*].[OutputKey,OutputValue]' \
  --output table
