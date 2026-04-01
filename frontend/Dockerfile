# ─── Stage 1: Build ──────────────────────────────────────────
FROM node:22-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci --legacy-peer-deps

COPY . .
RUN npm run build -- --configuration production

# ─── Stage 2: Nginx ──────────────────────────────────────────
FROM nginx:1.27-alpine
# nginx.conf is a template — envsubst will expand ${DOMAIN} at container start
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/templates/default.conf.template

EXPOSE 80 443
CMD ["nginx", "-g", "daemon off;"]
