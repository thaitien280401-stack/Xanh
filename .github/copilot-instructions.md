# Xanh Vocabulary App — Copilot Instructions

## Project Overview

**Xanh** is a full-stack vocabulary learning application that combines the Pomodoro study technique with gamified quizzes and a leaderboard. Users import English vocabulary topics, study them in timed Pomodoro sessions, test themselves with AI-generated quizzes, and track mastery progress. Events (quiz completed, session completed) flow through Kafka to update the real-time leaderboard asynchronously.

---

## Technology Stack

### Backend
| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.3.5 |
| Build | Maven 3.9 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Messaging | Apache Kafka (Confluent 7.6) |
| Auth | JWT (jjwt 0.12.6) + BCrypt |
| Mapping | MapStruct 1.6 |
| Boilerplate | Lombok 1.18 |
| API Docs | SpringDoc OpenAPI 2.6 (Swagger UI at `/swagger-ui.html`) |
| Testing | JUnit 5, Mockito, Testcontainers, Spring Security Test |

### Frontend
| Layer | Technology |
|---|---|
| Language | TypeScript 5.9 |
| Framework | Angular 21 (standalone components) |
| UI Library | Angular Material 21 |
| Styling | TailwindCSS 3.4 + SCSS |
| State | Angular Signals |
| HTTP | Angular HttpClient + RxJS 7.8 |
| Build | Angular CLI / `@angular/build` |
| Testing | Vitest + Karma (ChromeHeadless) |

### Infrastructure
| Component | Technology |
|---|---|
| Containerisation | Docker + Docker Compose |
| Image Registry | GitHub Container Registry (GHCR) |
| CI | GitHub Actions (`ci.yml`) |
| CD | GitHub Actions (`cd.yml`) — builds & pushes on `main`, deploys via SSH |
| Reverse Proxy | Nginx 1.27 (inside frontend container) |

---

## Repository Layout

```
Xanh/
├── .env.example              # All required environment variables (copy → .env)
├── .github/
│   ├── copilot-instructions.md
│   └── workflows/
│       ├── ci.yml            # Build & test on PRs to main/develop
│       └── cd.yml            # Build Docker images & deploy on push to main
├── docker-compose.yml        # Full local stack (postgres, zookeeper, kafka, backend, frontend)
├── backend/                  # Spring Boot application
│   ├── Dockerfile            # Multi-stage: Maven build → JRE 21 runtime
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/xanh/vocabulary/
│       │   ├── config/       # Security, Kafka, Swagger, AppProperties
│       │   ├── controller/   # REST controllers
│       │   ├── dto/          # request/ and response/ DTOs
│       │   ├── entity/       # JPA entities
│       │   ├── enums/        # Difficulty, MasteryLevel, QuizType, Role, SessionStatus, TopicStatus
│       │   ├── event/        # Kafka event POJOs
│       │   ├── exception/    # GlobalExceptionHandler
│       │   ├── kafka/        # producer/ and consumer/
│       │   ├── repository/   # Spring Data JPA repositories
│       │   ├── security/     # JwtService, JwtAuthenticationFilter, CustomUserDetailsService
│       │   └── service/      # Business logic
│       └── main/resources/
│           ├── application.yml
│           └── db/migration/ # Flyway scripts (V1__init_schema.sql, V2__seed_data.sql, …)
└── frontend/                 # Angular application
    ├── Dockerfile            # Multi-stage: Node 22 build → Nginx 1.27
    ├── nginx.conf            # SPA routing + /api/ reverse proxy + gzip + cache headers
    └── src/app/
        ├── core/             # guards/, interceptors/, models/, services/
        ├── features/         # auth/, dashboard/, leaderboard/, pomodoro/, quiz/, vocabulary/
        └── layout/           # app-layout/ (navbar + sidebar + router-outlet)
```

---

## Backend Coding Conventions

### General
- All source lives under `com.xanh.vocabulary.*`.
- Use `@RequiredArgsConstructor` (Lombok) for constructor injection — never `@Autowired` on fields.
- Use `@Slf4j` for logging; never use `System.out.println`.
- All entities extend `BaseEntity` (UUID primary key, `createdAt`, `updatedAt` set by `@PrePersist`/`@PreUpdate`).
- Return DTOs from all controller methods — never expose entities directly.
- Validate all request bodies with `@Valid` + JSR-380 annotations on DTOs.

### REST API Conventions
- Base path prefix: `/api/v1/`
- Plural noun resource names: `/api/v1/vocabularies`, `/api/v1/topics`
- Use standard HTTP verbs: `GET` (read), `POST` (create), `PUT` (replace), `PATCH` (partial update), `DELETE`
- Admin-only endpoints live under `/api/v1/admin/**` — secured by `hasRole("ADMIN")` in `SecurityConfig`
- Public read endpoints: `GET /api/v1/topics/**` and `GET /api/v1/vocabularies/**`

### Database & Migrations
- **Never** use `ddl-auto: create` or `ddl-auto: update` — all schema changes go through Flyway scripts.
- Migration scripts: `backend/src/main/resources/db/migration/V{n}__{description}.sql` (double underscore).
- Always add indexes on foreign key columns and columns used in `WHERE` clauses.
- Use `UUID` primary keys generated with `gen_random_uuid()`.

### Security
- JWT access token expiry: 15 min (900 000 ms). Refresh token expiry: 7 days (604 800 000 ms).
- Passwords hashed with BCrypt (strength 10, default).
- CORS allowed origins are comma-separated via `FRONTEND_URL` env var.

### Kafka
- Topic name constants in `KafkaTopics.java`.
- Event POJOs in `com.xanh.vocabulary.event`.
- Producers: `EventProducer` bean; inject and call `send(topic, event)`.
- Consumers: `@KafkaListener` in `com.xanh.vocabulary.kafka.consumer`.

### Testing
- Unit tests use Mockito; no Spring context (`@ExtendWith(MockitoExtension.class)`).
- Integration tests use `@SpringBootTest` + Testcontainers for PostgreSQL and Kafka.
- Test class naming: `{ClassName}Test`.
- CI test command: `mvn -B test` from `./backend`.

---

## Frontend Coding Conventions

### General
- All components are **standalone** (no `NgModule`).
- Use Angular **Signals** (`signal()`, `computed()`, `effect()`) for local component state. Avoid `Subject`/`BehaviorSubject` for UI state.
- Use `inject()` for dependency injection inside class bodies and functions — avoid constructor injection where inject() is cleaner.
- Lazy-load every feature route with `loadComponent: () => import(...)`.
- Component file suffix: `.component.ts` / `.component.html` / `.component.scss`.
- Service file suffix: `.service.ts` (singleton, `providedIn: 'root'`).

### HTTP
- All API calls go through dedicated services in `src/app/core/services/`.
- `environment.apiUrl` (`/api/v1`) is the base URL — never hardcode it.
- The `JwtInterceptor` (`core/interceptors/jwt.interceptor.ts`) automatically attaches the Bearer token to every outgoing request.

### Styling
- TailwindCSS utility classes are the default styling approach.
- Angular Material components for complex UI (forms, tables, dialogs, tabs, paginators).
- Component-scoped SCSS files for layout overrides only.

### Testing
- Unit test files: `{component}.spec.ts` co-located with the component.
- CI test command: `npm run test -- --watch=false --browsers=ChromeHeadless` from `./frontend`.

---

## Local Development Setup

### Prerequisites
- Docker Desktop (or Docker Engine + Compose v2)
- Java 21 + Maven 3.9 (for backend-only development)
- Node 22 + npm 10 (for frontend-only development)

### Start the full stack

```bash
cp .env.example .env
# Edit .env — set a strong JWT_SECRET before running
docker compose up -d --build
```

Services after startup:

| Service | URL |
|---|---|
| Frontend (Angular/Nginx) | http://localhost |
| Backend (Spring Boot) | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 |
| Kafka | localhost:9092 |

### Backend only (no Docker)

```bash
# Start dependencies (postgres + kafka) from docker-compose
docker compose up -d postgres zookeeper kafka

cd backend
mvn spring-boot:run
```

### Frontend only

```bash
cd frontend
npm ci --legacy-peer-deps
npm start          # dev server at http://localhost:4200
```

---

## Go Live — Deployment Guide

### Architecture

```
GitHub (main branch)
  └─ CI passes
  └─ CD workflow:
       ├─ Builds backend Docker image → pushes to GHCR
       ├─ Builds frontend Docker image → pushes to GHCR
       └─ SSH deploy job:
            └─ On production server at /opt/xanh-vocab/
                 └─ docker compose pull && docker compose up -d
```

### Required GitHub Actions Secrets

Configure these in **Settings → Secrets and Variables → Actions** on the repository:

| Secret | Description |
|---|---|
| `DEPLOY_HOST` | Hostname or IP of the production server |
| `DEPLOY_USER` | SSH username (e.g., `ubuntu`, `deploy`) |
| `DEPLOY_SSH_KEY` | Private SSH key (the public key must be in `~/.ssh/authorized_keys` on the server) |

### Required Environment Variables on the Production Server

Create `/opt/xanh-vocab/.env` with **production** values (do **not** use the defaults):

```env
# ─── PostgreSQL ───────────────────────────────────────────────
POSTGRES_DB=xanh_vocab
POSTGRES_USER=xanh
POSTGRES_PASSWORD=<strong-random-password>

# ─── Spring Boot ──────────────────────────────────────────────
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/xanh_vocab
SPRING_DATASOURCE_USERNAME=xanh
SPRING_DATASOURCE_PASSWORD=<same-as-POSTGRES_PASSWORD>

# ─── JWT (minimum 256 bits = 32 random bytes, base64-encoded) ─
JWT_SECRET=<generate-with: openssl rand -base64 32>
JWT_ACCESS_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000

# ─── Kafka ────────────────────────────────────────────────────
KAFKA_BOOTSTRAP_SERVERS=kafka:29092

# ─── External Vocabulary API ──────────────────────────────────
EXTERNAL_VOCAB_API_BASE_URL=https://api.dictionaryapi.dev/api/v2/entries/en

# ─── CORS: your real domain(s) ────────────────────────────────
FRONTEND_URL=https://yourdomain.com
```

### Server Pre-flight Checklist

Before the first deployment, verify the following on the production server:

- [ ] Docker Engine and Docker Compose v2 are installed (`docker compose version`)
- [ ] Directory `/opt/xanh-vocab/` exists and contains `docker-compose.yml` and `.env`
- [ ] The Docker images are accessible: GHCR packages on this repo must be **public** or the server must be logged in with `docker login ghcr.io`
- [ ] Ports **80** (HTTP) and optionally **443** (HTTPS) are open in the server's firewall
- [ ] Port **8080** is **not** exposed publicly (traffic goes through Nginx reverse proxy on port 80)
- [ ] PostgreSQL data directory is backed up or a backup schedule is in place
- [ ] `JWT_SECRET` is at least 256-bit random — generated with `openssl rand -base64 32`

### First Deployment (manual steps)

```bash
# On the production server
mkdir -p /opt/xanh-vocab
cd /opt/xanh-vocab

# Copy docker-compose.yml from the repository (or scp/rsync it)
# Create .env with all production values (see above)

# Log in to GHCR if the packages are private
echo $GITHUB_PAT | docker login ghcr.io -u <github-username> --password-stdin

# Pull and start all services
docker compose pull
docker compose up -d

# Verify all containers are healthy
docker compose ps
docker compose logs backend --tail=50
```

### Updating docker-compose.yml on the Server

The CD workflow assumes `docker-compose.yml` is already present at `/opt/xanh-vocab/`. When you update the compose file in the repository, sync it to the server manually or extend the SSH deploy script:

```bash
# Example SSH deploy script extension in cd.yml
scp docker-compose.yml ${DEPLOY_USER}@${DEPLOY_HOST}:/opt/xanh-vocab/docker-compose.yml
```

### HTTPS / TLS (recommended before Go Live)

The current `docker-compose.yml` and `nginx.conf` serve HTTP only. To enable HTTPS:

1. Add a **Certbot/Let's Encrypt** container to `docker-compose.yml`, or
2. Place a cloud load balancer / reverse proxy (e.g., Cloudflare, AWS ALB) in front of the server that terminates TLS.
3. Update `FRONTEND_URL` in `.env` to use `https://`.
4. Add `add_header Strict-Transport-Security "max-age=63072000; includeSubDomains" always;` to `nginx.conf`.

### Smoke Test After Deployment

```bash
# Health: backend is up
curl -f http://<server-ip>/api/v1/topics

# Swagger UI is accessible
curl -f http://<server-ip>/swagger-ui.html -L | grep -i swagger

# Frontend SPA loads
curl -f http://<server-ip>/ | grep -i "<app-root>"

# Kafka and leaderboard event flow
# 1. Register a user via POST /api/v1/auth/register
# 2. Login and get JWT
# 3. Complete a quiz via POST /api/v1/quizzes/{id}/submit
# 4. Check leaderboard GET /api/v1/leaderboard — entry should appear
```

---

## CI/CD Pipeline Summary

### CI (`ci.yml`) — triggers on PR to `main` or `develop`
1. **Backend** — sets up JDK 21, runs `mvn -B clean package -DskipTests`, then `mvn -B test` (with a PostgreSQL service container).
2. **Frontend** — sets up Node 22, runs `npm ci --legacy-peer-deps`, `npm run build -- --configuration production`, and `npm run test`.

### CD (`cd.yml`) — triggers on push to `main`
1. **Build & push backend** image to `ghcr.io/<owner>/xanh/xanh-backend:latest` and `sha-<commit>`.
2. **Build & push frontend** image to `ghcr.io/<owner>/xanh/xanh-frontend:latest` and `sha-<commit>`.
3. **Deploy** via SSH: pulls new images and restarts containers with `docker compose up -d --remove-orphans`, then runs `docker system prune -f`.

---

## Key Architectural Decisions

| Decision | Rationale |
|---|---|
| Stateless JWT auth | Scales horizontally without session affinity |
| Kafka for leaderboard updates | Decouples scoring from request path; quiz/pomodoro completion is fast |
| Flyway for migrations | Schema changes are versioned, repeatable, and auditable |
| Angular Signals | Fine-grained reactivity without Zone.js overhead; aligns with Angular 17+ best practices |
| Multi-stage Dockerfiles | Keeps production images small (JRE-only for backend, Nginx-only for frontend) |
| Nginx as SPA host + API proxy | Avoids CORS issues in production; single entry point on port 80 |
