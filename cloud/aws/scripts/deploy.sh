#!/usr/bin/env bash
set -euo pipefail

if ! command -v sam >/dev/null 2>&1; then
  echo "Error: AWS SAM CLI is not installed. Install from https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html"
  exit 1
fi

APP_NAME="${APP_NAME:-gis-cloud-api}"
STAGE_NAME="${STAGE_NAME:-prod}"
AWS_REGION="${AWS_REGION:-ap-southeast-1}"
STACK_NAME="${STACK_NAME:-${APP_NAME}-${STAGE_NAME}}"

if [[ -z "${FRONTEND_BUCKET_NAME:-}" || -z "${DATA_BUCKET_NAME:-}" ]]; then
  echo "Error: FRONTEND_BUCKET_NAME and DATA_BUCKET_NAME must be set."
  exit 1
fi

CORS_ORIGINS="${CORS_ORIGINS:-*}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE_PATH="${SCRIPT_DIR}/../template.yaml"

echo "Building SAM application..."
sam build --template-file "${TEMPLATE_PATH}"

echo "Deploying stack ${STACK_NAME} in ${AWS_REGION}..."
sam deploy \
  --stack-name "${STACK_NAME}" \
  --region "${AWS_REGION}" \
  --capabilities CAPABILITY_IAM \
  --resolve-s3 \
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
