# Vocabulary Pomodoro App — Project Tasks

## Phase 1: Project Scaffolding & Infrastructure

### 1.1 Repository & Base Structure
- [ ] Initialize Git repository structure (`/backend`, `/frontend`, `/docker`, `/.github`)
- [ ] Create root-level `docker-compose.yml` (PostgreSQL, Zookeeper, Kafka, Backend, Frontend)
- [ ] Create `.env.example` with all required environment variables

### 1.2 Backend Bootstrap (Spring Boot)
- [ ] Create Maven project with Spring Boot 3.x (Java 25)
- [ ] Configure `pom.xml` with all required dependencies:
  - [ ] spring-boot-starter-web
  - [ ] spring-boot-starter-data-jpa
  - [ ] spring-boot-starter-security
  - [ ] spring-kafka
  - [ ] springdoc-openapi-starter-webmvc-ui
  - [ ] jjwt (JWT library)
  - [ ] postgresql driver
  - [ ] lombok
  - [ ] mapstruct
  - [ ] testcontainers (test scope)
- [ ] Configure `application.yml` (datasource, Kafka, JWT secret, CORS)
- [ ] Configure `application-docker.yml` for Docker environment

### 1.3 Frontend Bootstrap (Angular)
- [ ] Create Angular project (latest stable) with routing and standalone components
- [ ] Install and configure Angular Material
- [ ] Install and configure Tailwind CSS
- [ ] Set up Angular project folder structure (`core`, `shared`, `features`, `layout`)
- [ ] Configure `environment.ts` and `environment.prod.ts`
- [ ] Set up HTTP interceptor for JWT token attachment
- [ ] Set up global error interceptor

### 1.4 Docker
- [ ] Write multi-stage `Dockerfile` for backend (Maven build → JRE runtime)
- [ ] Write multi-stage `Dockerfile` for frontend (Node build → Nginx)
- [ ] Write `nginx.conf` for Angular SPA routing
- [ ] Test full `docker-compose up` stack locally

---

## Phase 2: Backend — Database Entities & Repositories

### 2.1 Base Entity
- [ ] Create `BaseEntity` abstract class with `id` (UUID), `createdAt`, `updatedAt` (`@PrePersist`, `@PreUpdate`)

### 2.2 Entities
- [ ] Create `User` entity (id, username, email, passwordHash, avatarUrl, role enum)
- [ ] Create `Topic` entity (id, name, description, externalApiRef)
- [ ] Create `Vocabulary` entity (id, topicId FK, word, definition, pronunciation, partOfSpeech, exampleSentence, audioUrl, imageUrl, difficulty enum)
- [ ] Create `PomodoroSession` entity (id, userId FK, topicId FK, status enum, durationMinutes, wordsStudied, startedAt, endedAt)
- [ ] Create `UserVocabularyProgress` entity (id, userId FK, vocabularyId FK, timesSeen, timesCorrect, masteryLevel enum, lastReviewedAt, nextReviewAt)
- [ ] Create `Quiz` entity (id, userId FK, topicId FK, quizType enum, totalQuestions, correctAnswers, score, timeTakenSeconds, completedAt)
- [ ] Create `QuizQuestion` entity (id, quizId FK, vocabularyId FK, questionText, correctAnswer, userAnswer, isCorrect)
- [ ] Create `LeaderboardEntry` entity (id, userId FK unique, totalWordsLearned, totalStudyMinutes, totalQuizzes, highestQuizScore, weeklyPoints, totalPoints, updatedAt)
- [ ] Add proper JPA indexes on all foreign keys and frequently queried columns

### 2.3 Repositories
- [ ] `UserRepository` (findByEmail, findByUsername)
- [ ] `TopicRepository`
- [ ] `VocabularyRepository` (findByTopicId, full-text search query)
- [ ] `PomodoroSessionRepository` (findByUserIdOrderByStartedAtDesc)
- [ ] `UserVocabularyProgressRepository` (findByUserIdAndVocabularyId)
- [ ] `QuizRepository` (findByUserIdOrderByCreatedAtDesc)
- [ ] `QuizQuestionRepository`
- [ ] `LeaderboardEntryRepository` (findTopNByOrderByTotalPointsDesc, findByUserId)

### 2.4 Database Migrations
- [ ] Set up Flyway or Liquibase for schema migrations
- [ ] Write initial migration script `V1__init_schema.sql`

---

## Phase 3: Backend — Security & Authentication

- [ ] Configure Spring Security filter chain (stateless, JWT-based)
- [ ] Implement `JwtService` (generate, validate, extract claims)
- [ ] Implement `JwtAuthenticationFilter`
- [ ] Implement `UserDetailsService` backed by `UserRepository`
- [ ] Create `AuthController` with endpoints:
  - [ ] `POST /api/v1/auth/register`
  - [ ] `POST /api/v1/auth/login`
  - [ ] `POST /api/v1/auth/refresh`
  - [ ] `POST /api/v1/auth/logout`
- [ ] Create `AuthService` (register, login, refresh token logic)
- [ ] Implement refresh token storage (DB or in-memory)
- [ ] Add `@PreAuthorize` role checks to protected endpoints
- [ ] Write unit tests for `JwtService` and `AuthService`

---

## Phase 4: Backend — Core API Modules

### 4.1 User Module
- [ ] Create `UserController` (`GET /api/v1/users/me`, `PUT /api/v1/users/me`)
- [ ] Create `UserService` and `UserMapper` (MapStruct)
- [ ] Create `UserDto`, `UpdateUserRequest` DTOs

### 4.2 Topic Module
- [ ] Create `TopicController` (list, get by id, create)
- [ ] Create `TopicService` and `TopicMapper`
- [ ] Create `ExternalVocabApiClient` (RestClient/WebClient wrapper around Free Dictionary API or similar)
- [ ] Add `GET /api/v1/topics/external/search?query=` endpoint

### 4.3 Vocabulary Module
- [ ] Create `VocabularyController` (list by topic, get by id, search, import)
- [ ] Create `VocabularyService` (CRUD + import from external API)
- [ ] Create `VocabularyMapper` and DTOs
- [ ] Implement `POST /api/v1/vocabularies/import?topicId=` — calls external API, persists batch

### 4.4 Pomodoro Module
- [ ] Create `PomodoroController` (start, list, get, complete, abandon)
- [ ] Create `PomodoroService`
  - [ ] On complete: publish `pomodoro.session.completed` Kafka event
- [ ] Create `PomodoroSessionDto`, `StartSessionRequest` DTOs
- [ ] Write unit tests for `PomodoroService`

### 4.5 Quiz Module
- [ ] Create `QuizController` (generate, submit, list, get by id)
- [ ] Create `QuizService`
  - [ ] `generateQuiz()` — randomly select N vocab from topic, build questions per quiz type
  - [ ] `submitQuiz()` — grade answers, persist score, publish `quiz.completed` Kafka event
- [ ] Create `UserVocabularyProgressService` — update mastery level after quiz, publish `vocabulary.progress.updated`
- [ ] Create Quiz DTOs (`GenerateQuizRequest`, `SubmitQuizRequest`, `QuizResultDto`)
- [ ] Write unit tests for quiz generation and grading logic

### 4.6 Leaderboard Module
- [ ] Create `LeaderboardController` (`GET /api/v1/leaderboard`, `GET /api/v1/leaderboard/me`)
- [ ] Create `LeaderboardService` (fetch top N, fetch user rank)
- [ ] Create `LeaderboardDto`

### 4.7 Progress Module
- [ ] Create `ProgressController` (`GET /api/v1/progress/vocabulary`, `GET /api/v1/progress/stats`)
- [ ] Create `ProgressService` (aggregate stats per user)

---

## Phase 5: Backend — Kafka Event-Driven Layer

- [ ] Configure `KafkaProducerConfig` (serializer, bootstrap servers)
- [ ] Configure `KafkaConsumerConfig` (deserializer, group id, consumer factory)
- [ ] Define Kafka topic names as constants (`KafkaTopics.java`)
- [ ] Create event POJOs: `QuizCompletedEvent`, `PomodoroCompletedEvent`, `VocabProgressUpdatedEvent`
- [ ] Implement `LeaderboardKafkaConsumer`
  - [ ] `@KafkaListener` on `quiz.completed` → update score/points in leaderboard
  - [ ] `@KafkaListener` on `pomodoro.session.completed` → update study time/words in leaderboard
- [ ] Implement `StatsKafkaConsumer`
  - [ ] `@KafkaListener` on `vocabulary.progress.updated` → update total words learned
- [ ] Write integration tests for Kafka consumers using Testcontainers + EmbeddedKafka

---

## Phase 6: Backend — Swagger / OpenAPI

- [ ] Add `springdoc-openapi` configuration class
- [ ] Configure JWT Bearer auth scheme in OpenAPI config
- [ ] Add `@Tag` and `@Operation` annotations to all controllers
- [ ] Add `@ApiResponse` annotations documenting 200, 400, 401, 403, 404 responses
- [ ] Define separate API groups: `auth`, `vocabulary`, `pomodoro`, `quiz`, `leaderboard`
- [ ] Verify Swagger UI accessible at `/swagger-ui.html`

---

## Phase 7: Frontend — Core & Auth

- [ ] Implement `AuthService` (login, register, logout, token storage)
- [ ] Implement `AuthGuard` (redirect to login if unauthenticated)
- [ ] Build `LoginComponent` (form, validation, error messages)
- [ ] Build `RegisterComponent` (form, validation)
- [ ] Set up app routing with lazy-loaded feature modules
- [ ] Build `NavbarComponent` (logo, nav links, user avatar menu)
- [ ] Build `SidebarComponent` (navigation links)
- [ ] Build main `AppLayoutComponent` (navbar + sidebar + router-outlet)

---

## Phase 8: Frontend — Dashboard

- [ ] Build `DashboardComponent`
  - [ ] Today's stats card (sessions, words studied)
  - [ ] Weekly progress chart (ng2-charts or ApexCharts)
  - [ ] Mastery distribution donut chart
  - [ ] Quick-start Pomodoro button
  - [ ] Recent quiz scores list

---

## Phase 9: Frontend — Pomodoro Feature

- [ ] Build `SessionSetupComponent` (select topic, set duration 25/50 min)
- [ ] Build `PomodoroTimerComponent`
  - [ ] Circular countdown timer (CSS animation)
  - [ ] Start / Pause / Stop controls
  - [ ] Vocabulary flip card (word → definition on click)
  - [ ] Progress indicator (card X of Y)
- [ ] Build `SessionSummaryComponent` (words seen, time, go-to-quiz CTA)
- [ ] Integrate with `PomodoroService` (API calls + local timer state)

---

## Phase 10: Frontend — Vocabulary Feature

- [ ] Build `VocabularyBrowseComponent` (topic grid, vocab list per topic)
- [ ] Build `VocabularyCardComponent` (reusable flip card)
- [ ] Build `VocabularyImportComponent` (search external API, preview, import)
- [ ] Build `VocabularySearchComponent` (full-text search with debounce)

---

## Phase 11: Frontend — Quiz Feature

- [ ] Build `QuizSetupComponent` (choose topic, type, number of questions)
- [ ] Build `QuizTakeComponent`
  - [ ] Multiple choice question layout
  - [ ] Fill-in-the-blank input
  - [ ] True/False buttons
  - [ ] Progress bar and timer
- [ ] Build `QuizResultsComponent` (score, correct/incorrect breakdown per question)

---

## Phase 12: Frontend — Leaderboard Feature

- [ ] Build `LeaderboardComponent`
  - [ ] Tab toggle: Weekly vs All-Time
  - [ ] Top 20 ranked list with avatars, points, badges
  - [ ] Highlighted current user row
  - [ ] User's own rank card at bottom if not in top 20

---

## Phase 13: CI/CD Pipelines

- [ ] Create `.github/workflows/ci.yml`
  - [ ] Trigger: on pull request to `main`
  - [ ] Jobs: checkout, set up Java 25, Maven build & test, upload test report
  - [ ] Jobs: checkout, set up Node, npm install, Angular build, run unit tests
- [ ] Create `.github/workflows/cd.yml`
  - [ ] Trigger: on push to `main`
  - [ ] Jobs: build backend Docker image, push to GHCR
  - [ ] Jobs: build frontend Docker image, push to GHCR
  - [ ] Job: deploy (docker-compose pull + up on target server via SSH)

---

## Phase 14: Testing & Quality

- [ ] Write unit tests for all backend Services (>80% coverage target)
- [ ] Write integration tests for all Controllers (MockMvc + Testcontainers PostgreSQL)
- [ ] Write Kafka consumer integration tests (EmbeddedKafka)
- [ ] Write Angular unit tests for critical components (AuthService, PomodoroTimer)
- [ ] Add backend `@ControllerAdvice` global exception handler
- [ ] Add input validation (`@Valid`, `@NotNull`, etc.) on all request DTOs

---

## Phase 15: Final Polish

- [ ] Add database seed data script (`V2__seed_data.sql`) with sample topics and vocabulary
- [ ] Add API rate limiting (Spring bucket4j or custom filter)
- [ ] Add CORS configuration for production domain
- [ ] Write root-level `README.md` with setup, run, and API docs instructions
- [ ] Final end-to-end smoke test of full Docker Compose stack
