#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${FRONTEND_BUCKET_NAME:-gis-cloud-frontend-3sktb}" || -z "${API_BASE_URL:-https://si0babi5s0.execute-api.ap-southeast-1.amazonaws.com/prod}" ]]; then
  echo "Error: FRONTEND_BUCKET_NAME and API_BASE_URL must be set."
  exit 1
fi

AWS_REGION="${AWS_REGION:-ap-southeast-1}"

TMP_DIR="$(mktemp -d)"
cleanup() { rm -rf "${TMP_DIR}"; }
trap cleanup EXIT

cp frontend/index.html "${TMP_DIR}/index.html"
cp frontend/app.js "${TMP_DIR}/app.js"

cat > "${TMP_DIR}/config.js" <<EOF
window.__APP_CONFIG__ = {
  API_BASE_URL: "${API_BASE_URL}"
};
EOF

aws s3 sync "${TMP_DIR}" "s3://${FRONTEND_BUCKET_NAME}" \
  --region "${AWS_REGION}" \
  --delete \
  --cache-control "public, max-age=60"

echo "Frontend synced to s3://${FRONTEND_BUCKET_NAME}"
