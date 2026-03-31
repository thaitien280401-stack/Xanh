# Vocabulary Pomodoro App — Project Tasks

## Phase 1: Project Scaffolding & Infrastructure

### 1.1 Repository & Base Structure
- [x] Initialize Git repository structure (`/backend`, `/frontend`, `/docker`, `/.github`)
- [x] Create root-level `docker-compose.yml` (PostgreSQL, Zookeeper, Kafka, Backend, Frontend)
- [x] Create `.env.example` with all required environment variables

### 1.2 Backend Bootstrap (Spring Boot)
- [x] Create Maven project with Spring Boot 3.x (Java 21 LTS)
- [x] Configure `pom.xml` with all required dependencies:
  - [x] spring-boot-starter-web
  - [x] spring-boot-starter-data-jpa
  - [x] spring-boot-starter-security
  - [x] spring-kafka
  - [x] springdoc-openapi-starter-webmvc-ui
  - [x] jjwt (JWT library)
  - [x] postgresql driver
  - [x] lombok
  - [x] mapstruct
  - [x] testcontainers (test scope)
- [x] Configure `application.yml` (datasource, Kafka, JWT secret, CORS)
- [x] Configure `application-docker.yml` for Docker environment

### 1.3 Frontend Bootstrap (Angular)
- [x] Create Angular project (latest stable) with routing and standalone components
- [x] Install and configure Angular Material
- [x] Install and configure Tailwind CSS
- [x] Set up Angular project folder structure (`core`, `shared`, `features`, `layout`)
- [x] Configure `environment.ts` and `environment.prod.ts`
- [x] Set up HTTP interceptor for JWT token attachment
- [x] Set up global error interceptor

### 1.4 Docker
- [x] Write multi-stage `Dockerfile` for backend (Maven build → JRE runtime)
- [x] Write multi-stage `Dockerfile` for frontend (Node build → Nginx)
- [x] Write `nginx.conf` for Angular SPA routing
- [ ] Test full `docker-compose up` stack locally

---

## Phase 2: Backend — Database Entities & Repositories

### 2.1 Base Entity
- [x] Create `BaseEntity` abstract class with `id` (UUID), `createdAt`, `updatedAt` (`@PrePersist`, `@PreUpdate`)

### 2.2 Entities
- [x] Create `User` entity (id, username, email, passwordHash, avatarUrl, role enum)
- [x] Create `Topic` entity (id, name, description, externalApiRef)
- [x] Create `Vocabulary` entity (id, topicId FK, word, definition, pronunciation, partOfSpeech, exampleSentence, audioUrl, imageUrl, difficulty enum)
- [x] Create `PomodoroSession` entity (id, userId FK, topicId FK, status enum, durationMinutes, wordsStudied, startedAt, endedAt)
- [x] Create `UserVocabularyProgress` entity (id, userId FK, vocabularyId FK, timesSeen, timesCorrect, masteryLevel enum, lastReviewedAt, nextReviewAt)
- [x] Create `Quiz` entity (id, userId FK, topicId FK, quizType enum, totalQuestions, correctAnswers, score, timeTakenSeconds, completedAt)
- [x] Create `QuizQuestion` entity (id, quizId FK, vocabularyId FK, questionText, correctAnswer, userAnswer, isCorrect)
- [x] Create `LeaderboardEntry` entity (id, userId FK unique, totalWordsLearned, totalStudyMinutes, totalQuizzes, highestQuizScore, weeklyPoints, totalPoints, updatedAt)
- [x] Add proper JPA indexes on all foreign keys and frequently queried columns

### 2.3 Repositories
- [x] `UserRepository` (findByEmail, findByUsername)
- [x] `TopicRepository`
- [x] `VocabularyRepository` (findByTopicId, full-text search query)
- [x] `PomodoroSessionRepository` (findByUserIdOrderByStartedAtDesc)
- [x] `UserVocabularyProgressRepository` (findByUserIdAndVocabularyId)
- [x] `QuizRepository` (findByUserIdOrderByCreatedAtDesc)
- [x] `QuizQuestionRepository`
- [x] `LeaderboardEntryRepository` (findTopNByOrderByTotalPointsDesc, findByUserId)

### 2.4 Database Migrations
- [x] Set up Flyway for schema migrations
- [x] Write initial migration script `V1__init_schema.sql`

---

## Phase 3: Backend — Security & Authentication

- [x] Configure Spring Security filter chain (stateless, JWT-based)
- [x] Implement `JwtService` (generate, validate, extract claims)
- [x] Implement `JwtAuthenticationFilter`
- [x] Implement `UserDetailsService` backed by `UserRepository`
- [x] Create `AuthController` with endpoints:
  - [x] `POST /api/v1/auth/register`
  - [x] `POST /api/v1/auth/login`
  - [x] `POST /api/v1/auth/refresh`
  - [x] `POST /api/v1/auth/logout`
- [x] Create `AuthService` (register, login, refresh token logic)
- [x] Implement refresh token storage (DB)
- [x] Add `@PreAuthorize` role checks to protected endpoints
- [x] Write unit tests for `JwtService` and `AuthService`

---

## Phase 4: Backend — Core API Modules

### 4.1 User Module
- [x] Create `UserController` (`GET /api/v1/users/me`, `PUT /api/v1/users/me`)
- [x] Create `UserService` and DTOs

### 4.2 Topic Module
- [x] Create `TopicController` (list, get by id, create)
- [x] Create `TopicService`
- [x] Create `ExternalVocabApiClient` (RestClient wrapper around Free Dictionary API)
- [x] Add `GET /api/v1/topics/external/search?query=` endpoint

### 4.3 Vocabulary Module
- [x] Create `VocabularyController` (list by topic, get by id, search, import)
- [x] Create `VocabularyService` (CRUD + import from external API)
- [x] Implement `POST /api/v1/vocabularies/import?topicId=`

### 4.4 Pomodoro Module
- [x] Create `PomodoroController` (start, list, get, complete, abandon)
- [x] Create `PomodoroService`
  - [x] On complete: publish `pomodoro.session.completed` Kafka event
- [x] Write unit tests for `PomodoroService`

### 4.5 Quiz Module
- [x] Create `QuizController` (generate, submit, list, get by id)
- [x] Create `QuizService`
  - [x] `generateQuiz()` — randomly select N vocab from topic, build questions per quiz type
  - [x] `submitQuiz()` — grade answers, persist score, publish `quiz.completed` Kafka event
- [x] Create `UserVocabularyProgressService` — update mastery level after quiz
- [x] Write unit tests for quiz generation and grading logic

### 4.6 Leaderboard Module
- [x] Create `LeaderboardController` (`GET /api/v1/leaderboard`, `GET /api/v1/leaderboard/me`)
- [x] Create `LeaderboardService`

### 4.7 Progress Module
- [x] Create `ProgressController` (`GET /api/v1/progress/stats`)
- [x] Create `ProgressService`

---

## Phase 5: Backend — Kafka Event-Driven Layer

- [x] Configure `KafkaProducerConfig` (serializer, bootstrap servers)
- [x] Configure `KafkaConsumerConfig` (deserializer, group id, consumer factory)
- [x] Define Kafka topic names as constants (`KafkaTopics.java`)
- [x] Create event POJOs: `QuizCompletedEvent`, `PomodoroCompletedEvent`
- [x] Implement `LeaderboardKafkaConsumer`
  - [x] `@KafkaListener` on `quiz.completed` → update score/points in leaderboard
  - [x] `@KafkaListener` on `pomodoro.session.completed` → update study time/words in leaderboard

---

## Phase 6: Backend — Swagger / OpenAPI

- [x] Add `springdoc-openapi` configuration class
- [x] Configure JWT Bearer auth scheme in OpenAPI config
- [x] Add `@Tag` and `@Operation` annotations to all controllers
- [x] Add `@ApiResponse` annotations documenting responses
- [x] Define separate API groups: `auth`, `vocabulary`, `pomodoro`, `quiz`, `leaderboard`
- [x] Verify Swagger UI accessible at `/swagger-ui.html`

---

## Phase 7: Frontend — Core & Auth

- [x] Implement `AuthService` (login, register, logout, token storage)
- [x] Implement `AuthGuard` (redirect to login if unauthenticated)
- [x] Build `LoginComponent` (form, validation, error messages)
- [x] Build `RegisterComponent` (form, validation)
- [x] Set up app routing with lazy-loaded feature modules
- [x] Build `NavbarComponent` (logo, nav links, user avatar menu)
- [x] Build `SidebarComponent` (navigation links)
- [x] Build main `AppLayoutComponent` (navbar + sidebar + router-outlet)

---

## Phase 8: Frontend — Dashboard

- [x] Build `DashboardComponent`
  - [x] Today's stats card (sessions, words studied)
  - [x] Mastery distribution breakdown
  - [x] Quick-start Pomodoro button

---

## Phase 9: Frontend — Pomodoro Feature

- [x] Build `SessionSetupComponent` (select topic, set duration)
- [x] Build `PomodoroTimerComponent`
  - [x] Circular countdown timer
  - [x] Vocabulary flip card (word → definition on click)
  - [x] Progress indicator
- [x] Build `SessionSummaryComponent`
- [x] Integrate with `PomodoroService`

---

## Phase 10: Frontend — Vocabulary Feature

- [x] Build `VocabularyBrowseComponent` (topic grid, vocab list)
- [x] Build `VocabularyImportComponent`

---

## Phase 11: Frontend — Quiz Feature

- [x] Build `QuizSetupComponent`
- [x] Build `QuizTakeComponent`
- [x] Build `QuizResultsComponent`

---

## Phase 12: Frontend — Leaderboard Feature

- [x] Build `LeaderboardComponent`
  - [x] Tab toggle: Weekly vs All-Time
  - [x] Top 20 ranked list
  - [x] Highlighted current user row

---

## Phase 13: CI/CD Pipelines

- [x] Create `.github/workflows/ci.yml`
  - [x] Trigger: on pull request to `main`
  - [x] Jobs: checkout, set up Java 21, Maven build & test
  - [x] Jobs: checkout, set up Node, npm install, Angular build + test
- [x] Create `.github/workflows/cd.yml`
  - [x] Trigger: on push to `main`
  - [x] Jobs: build backend Docker image, push to GHCR
  - [x] Jobs: build frontend Docker image, push to GHCR
  - [x] Job: deploy via SSH

---

## Phase 14: Testing & Quality

- [x] Write unit tests for `AuthService`
- [x] Write unit tests for `QuizService`
- [x] Write Spring Boot context load test
- [x] Add backend `@ControllerAdvice` global exception handler
- [x] Add input validation (`@Valid`, `@NotNull`, etc.) on all request DTOs
- [ ] Write integration tests for Controllers (MockMvc + Testcontainers PostgreSQL)
- [ ] Write Kafka consumer integration tests

---

## Phase 15: Final Polish

- [x] Add database seed data script (`V2__seed_data.sql`)
- [x] Add CORS configuration for production domain
- [ ] Add API rate limiting
- [x] Write root-level `README.md` (see below)
- [ ] Final end-to-end smoke test of full Docker Compose stack
