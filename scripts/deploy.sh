#!/usr/bin/env bash
# =============================================================================
# deploy.sh — First-time production server setup for Xanh
#
# Run this script once on a fresh Ubuntu/Debian server to install Docker,
# create the deployment directory, and configure the application.
#
# Usage (as root or with sudo):
#   curl -fsSL https://raw.githubusercontent.com/thaitien280401-stack/Xanh/main/scripts/deploy.sh | sudo bash
#
# Or if you have the repo checked out:
#   sudo bash scripts/deploy.sh
# =============================================================================

set -euo pipefail

APP_DIR="/opt/xanh-vocab"
REPO="https://github.com/thaitien280401-stack/Xanh"

echo "======================================================"
echo "  Xanh Vocabulary App — Production Server Setup"
echo "======================================================"

# ── 1. Install Docker Engine + Compose v2 ─────────────────────────────────
if ! command -v docker &>/dev/null; then
  echo "📦 Installing Docker..."
  apt-get update -q
  apt-get install -y -q ca-certificates curl gnupg lsb-release
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
    | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  chmod a+r /etc/apt/keyrings/docker.gpg
  echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
    | tee /etc/apt/sources.list.d/docker.list >/dev/null
  apt-get update -q
  apt-get install -y -q docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
  systemctl enable --now docker
  echo "✅ Docker installed: $(docker --version)"
else
  echo "✅ Docker already installed: $(docker --version)"
fi

# ── 2. Verify Docker Compose v2 ───────────────────────────────────────────
docker compose version >/dev/null 2>&1 \
  || { echo "❌ Docker Compose v2 not found. Install docker-compose-plugin."; exit 1; }
echo "✅ Docker Compose: $(docker compose version)"

# ── 3. Create deployment directory ────────────────────────────────────────
mkdir -p "$APP_DIR/scripts"
echo "📁 Created $APP_DIR"

# ── 4. Download docker-compose.yml from repository ────────────────────────
echo "📥 Downloading docker-compose.yml..."
curl -fsSL "$REPO/raw/main/docker-compose.yml" -o "$APP_DIR/docker-compose.yml"
echo "📥 Downloading init-letsencrypt.sh..."
curl -fsSL "$REPO/raw/main/scripts/init-letsencrypt.sh" -o "$APP_DIR/scripts/init-letsencrypt.sh"
chmod +x "$APP_DIR/scripts/init-letsencrypt.sh"

# ── 5. Create .env from example if it doesn't exist ──────────────────────
if [ ! -f "$APP_DIR/.env" ]; then
  curl -fsSL "$REPO/raw/main/.env.example" -o "$APP_DIR/.env"
  echo ""
  echo "⚠️  Created $APP_DIR/.env from .env.example"
  echo "   ➡  Edit it now to set your real values before continuing:"
  echo "      nano $APP_DIR/.env"
  echo ""
  echo "   Required values to change:"
  echo "     DOMAIN          — your production domain (e.g. xanh.example.com)"
  echo "     CERTBOT_EMAIL   — your email for Let's Encrypt notifications"
  echo "     POSTGRES_PASSWORD — strong random password"
  echo "     JWT_SECRET      — at least 256-bit random (openssl rand -base64 32)"
  echo "     FRONTEND_URL    — https://\$DOMAIN"
fi

# ── 6. Open firewall ports (ufw) ──────────────────────────────────────────
if command -v ufw &>/dev/null; then
  ufw allow 22/tcp   comment "SSH"   2>/dev/null || true
  ufw allow 80/tcp   comment "HTTP"  2>/dev/null || true
  ufw allow 443/tcp  comment "HTTPS" 2>/dev/null || true
  echo "✅ Firewall rules added (ports 22, 80, 443)"
fi

echo ""
echo "======================================================"
echo "  Setup complete! Next steps:"
echo "======================================================"
echo ""
echo "1. Edit the .env file with your production values:"
echo "   nano $APP_DIR/.env"
echo ""
echo "2. Log in to GitHub Container Registry (if images are private):"
echo "   echo \$GITHUB_PAT | docker login ghcr.io -u <username> --password-stdin"
echo ""
echo "3. Bootstrap Let's Encrypt certificates:"
echo "   sudo $APP_DIR/scripts/init-letsencrypt.sh"
echo ""
echo "4. Start the full application stack:"
echo "   cd $APP_DIR && docker compose up -d"
echo ""
echo "5. Verify all containers are healthy:"
echo "   docker compose ps"
echo "   docker compose logs backend --tail=50"
echo ""
ACTUAL_DOMAIN=$(grep '^DOMAIN=' "$APP_DIR/.env" 2>/dev/null | cut -d= -f2 || echo "")
if [ -z "$ACTUAL_DOMAIN" ] || [ "$ACTUAL_DOMAIN" = "yourdomain.com" ]; then
  echo "6. Access your app at https://<your-domain>  (edit DOMAIN in $APP_DIR/.env first)"
else
  echo "6. Access your app at https://$ACTUAL_DOMAIN"
fi
echo ""
