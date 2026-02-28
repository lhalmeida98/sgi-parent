#!/usr/bin/env bash
set -euo pipefail

PROJECT_ID="${PROJECT_ID:-}"

if [[ -z "${PROJECT_ID}" ]]; then
  echo "Error: define PROJECT_ID. Ejemplo: PROJECT_ID=vualaback ./scripts/gcp/upsert-secrets.sh"
  exit 1
fi

required_vars=(
  SPRING_DATASOURCE_URL
  SPRING_DATASOURCE_USERNAME
  SPRING_DATASOURCE_PASSWORD
  APP_SECURITY_JWT_SECRET
  RESEND_API_KEY
  MAIL_FROM_EMAIL
  MAIL_FROM_NAME
)

missing=0
for key in "${required_vars[@]}"; do
  if [[ -z "${!key:-}" ]]; then
    echo "Missing env var: ${key}"
    missing=1
  fi
done

if [[ "${missing}" -eq 1 ]]; then
  echo "Export the missing variables and rerun."
  exit 1
fi

gcloud config set project "${PROJECT_ID}" >/dev/null

upsert_secret() {
  local name="$1"
  local value="$2"
  if gcloud secrets describe "${name}" >/dev/null 2>&1; then
    printf '%s' "${value}" | gcloud secrets versions add "${name}" --data-file=- >/dev/null
    echo "Updated secret version: ${name}"
  else
    printf '%s' "${value}" | gcloud secrets create "${name}" \
      --replication-policy=automatic \
      --data-file=- >/dev/null
    echo "Created secret: ${name}"
  fi
}

upsert_secret SPRING_DATASOURCE_URL "${SPRING_DATASOURCE_URL}"
upsert_secret SPRING_DATASOURCE_USERNAME "${SPRING_DATASOURCE_USERNAME}"
upsert_secret SPRING_DATASOURCE_PASSWORD "${SPRING_DATASOURCE_PASSWORD}"
upsert_secret APP_SECURITY_JWT_SECRET "${APP_SECURITY_JWT_SECRET}"
upsert_secret RESEND_API_KEY "${RESEND_API_KEY}"
upsert_secret MAIL_FROM_EMAIL "${MAIL_FROM_EMAIL}"
upsert_secret MAIL_FROM_NAME "${MAIL_FROM_NAME}"

echo "Secrets upsert completed."
