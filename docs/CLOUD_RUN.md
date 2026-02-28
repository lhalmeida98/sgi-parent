# Deploy a Cloud Run (SGI Backend)

## 1) Login and select project

```bash
gcloud auth login
gcloud config set project vualaback
```

Prerequisito: el proyecto debe tener una cuenta de facturacion asociada.

```bash
gcloud billing projects describe vualaback --format='value(billingEnabled)'
gcloud billing accounts list
gcloud billing projects link vualaback --billing-account=XXXXXX-XXXXXX-XXXXXX
```

## 2) Setup (one-time)

```bash
cd "/home/henry/Documentos/Proyectos /sgi-parent"
chmod +x scripts/gcp/*.sh
PROJECT_ID=vualaback REGION=us-central1 ./scripts/gcp/setup-cloudrun.sh
```

## 3) Create or update secrets

```bash
export PROJECT_ID=vualaback
export SPRING_DATASOURCE_URL='jdbc:postgresql://...'
export SPRING_DATASOURCE_USERNAME='...'
export SPRING_DATASOURCE_PASSWORD='...'
export APP_SECURITY_JWT_SECRET='...'
export RESEND_API_KEY='...'
export MAIL_FROM_EMAIL='no-reply@tu-dominio.com'
export MAIL_FROM_NAME='VualaBack'

./scripts/gcp/upsert-secrets.sh
```

## 4) Deploy

```bash
PROJECT_ID=vualaback \
REGION=us-central1 \
SERVICE=sgi-backend \
REPO=sgi \
CORS_ALLOWED_ORIGINS=https://tu-frontend.com \
ALLOW_UNAUTHENTICATED=true \
./scripts/gcp/deploy-cloudrun.sh
```

## 5) Verify

```bash
URL="$(gcloud run services describe sgi-backend --region us-central1 --format='value(status.url)')"
echo "$URL"
curl -i "$URL/api/ping"
```

## Notes

- Runtime file storage in Cloud Run is ephemeral. This deploy sets:
  - `APP_SIGNATURE_STORAGEDIR=/tmp/storage/firmas`
  - `APP_EMPRESA_LOGO_STORAGEDIR=/tmp/storage/logos`
- If you need persistence for signatures/logos, move them to Cloud Storage in a next step.
