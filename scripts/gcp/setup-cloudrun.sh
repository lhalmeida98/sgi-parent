#!/usr/bin/env bash
set -euo pipefail

PROJECT_ID="${PROJECT_ID:-}"
REGION="${REGION:-us-central1}"
REPO="${REPO:-sgi}"
SA_NAME="${SA_NAME:-sgi-backend-sa}"

if [[ -z "${PROJECT_ID}" ]]; then
  echo "Error: define PROJECT_ID. Ejemplo: PROJECT_ID=vualaback ./scripts/gcp/setup-cloudrun.sh"
  exit 1
fi

SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

echo "Config project: ${PROJECT_ID}"
gcloud config set project "${PROJECT_ID}" >/dev/null
gcloud config set run/region "${REGION}" >/dev/null

echo "Check billing status"
if ! billing_enabled="$(gcloud billing projects describe "${PROJECT_ID}" --format='value(billingEnabled)' 2>/dev/null)"; then
  echo "Error: no se pudo consultar el estado de facturacion del proyecto ${PROJECT_ID}."
  echo "Verifica permisos de IAM para Billing Account Viewer/Admin y vuelve a intentar."
  exit 1
fi

if [[ "${billing_enabled}" != "True" ]]; then
  echo "Error: el proyecto ${PROJECT_ID} no tiene facturacion habilitada."
  echo "Asocia una cuenta de facturacion y vuelve a ejecutar este script."
  echo
  echo "Comandos utiles:"
  echo "  gcloud billing accounts list"
  echo "  gcloud billing projects link ${PROJECT_ID} --billing-account=XXXXXX-XXXXXX-XXXXXX"
  echo
  echo "Tambien puedes hacerlo en consola:"
  echo "  https://console.cloud.google.com/billing/linkedaccount?project=${PROJECT_ID}"
  exit 1
fi

echo "Enable required services"
gcloud services enable \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com \
  secretmanager.googleapis.com

if ! gcloud artifacts repositories describe "${REPO}" --location "${REGION}" >/dev/null 2>&1; then
  echo "Create Artifact Registry: ${REPO}"
  gcloud artifacts repositories create "${REPO}" \
    --repository-format=docker \
    --location="${REGION}" \
    --description="Docker images for SGI backend"
else
  echo "Artifact Registry already exists: ${REPO}"
fi

if ! gcloud iam service-accounts describe "${SA_EMAIL}" >/dev/null 2>&1; then
  echo "Create service account: ${SA_NAME}"
  gcloud iam service-accounts create "${SA_NAME}" \
    --display-name="SGI Backend Cloud Run"
else
  echo "Service account already exists: ${SA_EMAIL}"
fi

echo "Grant Secret Manager access to runtime service account"
gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/secretmanager.secretAccessor" >/dev/null

echo "Setup completed."
