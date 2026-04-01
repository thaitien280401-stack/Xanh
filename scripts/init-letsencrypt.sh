#!/usr/bin/env bash
# =============================================================================
# init-letsencrypt.sh — Bootstrap Let's Encrypt TLS certificates for Xanh
#
# Run this script ONCE on the production server BEFORE starting the full stack.
# It will:
#   1. Load DOMAIN and CERTBOT_EMAIL from /opt/xanh-vocab/.env
#   2. Create temporary self-signed certificates so Nginx can start
#   3. Start Nginx so Certbot can complete the HTTP-01 ACME challenge
#   4. Obtain real certificates from Let's Encrypt via the webroot plugin
#   5. Restart Nginx with the real certificates
#
# Usage:
#   cd /opt/xanh-vocab
#   chmod +x scripts/init-letsencrypt.sh
#   sudo ./scripts/init-letsencrypt.sh
# =============================================================================

set -euo pipefail

COMPOSE_DIR="/opt/xanh-vocab"
ENV_FILE="$COMPOSE_DIR/.env"

# ── Load required variables ────────────────────────────────────────────────
if [ ! -f "$ENV_FILE" ]; then
  echo "❌  $ENV_FILE not found. Copy .env.example → .env and fill in all values."
  exit 1
fi

# shellcheck source=/dev/null
source "$ENV_FILE"

DOMAIN="${DOMAIN:?DOMAIN must be set in $ENV_FILE}"
EMAIL="${CERTBOT_EMAIL:?CERTBOT_EMAIL must be set in $ENV_FILE}"

echo "🔐 Initialising Let's Encrypt certificates for domain: $DOMAIN"

# ── 1. Create dummy certificate so Nginx can start ────────────────────────
# Check whether a certificate already exists inside the certbot volume.
# We run a quick inspection via 'docker compose run' so the volume name is
# resolved by Compose (avoiding a hardcoded project-name prefix).
CERT_EXISTS=$(docker compose -f "$COMPOSE_DIR/docker-compose.yml" run --rm \
  --entrypoint "/bin/sh" certbot \
  -c "[ -f /etc/letsencrypt/live/$DOMAIN/fullchain.pem ] && echo yes || echo no" 2>/dev/null || echo "no")

if [ "$CERT_EXISTS" != "yes" ]; then
  echo "📝 Creating temporary self-signed certificate..."
  # Use docker compose run so Docker Compose resolves the volume name correctly
  # regardless of the Compose project name setting.
  docker compose -f "$COMPOSE_DIR/docker-compose.yml" run --rm \
    --entrypoint "/bin/sh" certbot \
    -c "apk add --no-cache openssl 2>/dev/null || true; \
        mkdir -p /etc/letsencrypt/live/$DOMAIN && \
        openssl req -x509 -nodes -newkey rsa:4096 -days 1 \
          -keyout /etc/letsencrypt/live/$DOMAIN/privkey.pem \
          -out    /etc/letsencrypt/live/$DOMAIN/fullchain.pem \
          -subj '/CN=localhost'"
fi

# ── 2. Start Nginx (uses dummy cert) ──────────────────────────────────────
echo "🚀 Starting Nginx with temporary certificate..."
cd "$COMPOSE_DIR"
docker compose up -d frontend

# Give Nginx a moment to start
sleep 5

# ── 3. Obtain real certificate via webroot challenge ──────────────────────
echo "🌐 Requesting Let's Encrypt certificate for $DOMAIN..."
docker compose run --rm certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email "$EMAIL" \
  --agree-tos \
  --no-eff-email \
  --force-renewal \
  -d "$DOMAIN"

# ── 4. Reload Nginx to pick up the real certificate ───────────────────────
echo "🔄 Reloading Nginx..."
docker compose exec frontend nginx -s reload

echo ""
echo "✅ Let's Encrypt certificate installed for https://$DOMAIN"
echo "   Certbot will automatically renew the certificate every 12 hours."
echo ""
echo "Next step: start the full stack with:"
echo "  cd $COMPOSE_DIR && docker compose up -d"
