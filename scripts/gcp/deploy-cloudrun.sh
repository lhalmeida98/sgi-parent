#!/usr/bin/env bash
set -euo pipefail

PROJECT_ID="${PROJECT_ID:-}"
REGION="${REGION:-us-central1}"
SERVICE="${SERVICE:-sgi-backend}"
REPO="${REPO:-sgi}"
SA_NAME="${SA_NAME:-sgi-backend-sa}"

SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"
CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-https://tu-frontend.com}"
ALLOW_UNAUTHENTICATED="${ALLOW_UNAUTHENTICATED:-true}"

CPU="${CPU:-1}"
MEMORY="${MEMORY:-1Gi}"
MIN_INSTANCES="${MIN_INSTANCES:-0}"
MAX_INSTANCES="${MAX_INSTANCES:-5}"
CONCURRENCY="${CONCURRENCY:-20}"
TIMEOUT="${TIMEOUT:-300}"

if [[ -z "${PROJECT_ID}" ]]; then
  echo "Error: define PROJECT_ID. Ejemplo: PROJECT_ID=vualaback ./scripts/gcp/deploy-cloudrun.sh"
  exit 1
fi

SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"
TAG="$(date +%Y%m%d-%H%M%S)"
IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO}/${SERVICE}:${TAG}"

gcloud config set project "${PROJECT_ID}" >/dev/null

echo "Build image: ${IMAGE}"
gcloud builds submit --tag "${IMAGE}" .

env_vars="SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE},CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS},APP_SIGNATURE_STORAGEDIR=/tmp/storage/firmas,APP_EMPRESA_LOGO_STORAGEDIR=/tmp/storage/logos"
secret_vars="SPRING_DATASOURCE_URL=SPRING_DATASOURCE_URL:latest,SPRING_DATASOURCE_USERNAME=SPRING_DATASOURCE_USERNAME:latest,SPRING_DATASOURCE_PASSWORD=SPRING_DATASOURCE_PASSWORD:latest,APP_SECURITY_JWT_SECRET=APP_SECURITY_JWT_SECRET:latest,RESEND_API_KEY=RESEND_API_KEY:latest,MAIL_FROM_EMAIL=MAIL_FROM_EMAIL:latest,MAIL_FROM_NAME=MAIL_FROM_NAME:latest"

deploy_cmd=(
  gcloud run deploy "${SERVICE}"
  --image "${IMAGE}"
  --region "${REGION}"
  --service-account "${SA_EMAIL}"
  --port 8080
  --cpu "${CPU}"
  --memory "${MEMORY}"
  --min-instances "${MIN_INSTANCES}"
  --max-instances "${MAX_INSTANCES}"
  --concurrency "${CONCURRENCY}"
  --timeout "${TIMEOUT}"
  --set-env-vars "${env_vars}"
  --set-secrets "${secret_vars}"
)

if [[ "${ALLOW_UNAUTHENTICATED}" == "true" ]]; then
  deploy_cmd+=(--allow-unauthenticated)
else
  deploy_cmd+=(--no-allow-unauthenticated)
fi

echo "Deploy service: ${SERVICE}"
"${deploy_cmd[@]}"

URL="$(gcloud run services describe "${SERVICE}" --region "${REGION}" --format='value(status.url)')"
echo "Service URL: ${URL}"

if [[ "${ALLOW_UNAUTHENTICATED}" == "true" ]]; then
  echo "Ping check"
  curl -fsS "${URL}/api/ping"
  echo
fi

echo "Deploy completed."
