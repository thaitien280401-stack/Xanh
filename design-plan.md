# Design Plan — Topic Scheduler & Grid UI

## 1. Database Schema Updates

### 1.1 `topics` table — add `status` column

```sql
ALTER TABLE topics
  ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('ACTIVE', 'DONE'));

CREATE INDEX idx_topics_status ON topics(status);
```

### 1.2 New `scheduler_config` table

Stores runtime-configurable key/value pairs for the scheduler.

```sql
CREATE TABLE scheduler_config (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  config_key   VARCHAR(100) NOT NULL UNIQUE,
  config_value VARCHAR(255) NOT NULL,
  description  TEXT,
  created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);
```

Seed rows:

| config_key          | config_value | description                                        |
|---------------------|--------------|----------------------------------------------------|
| INTERVAL_HOURS      | 2            | How often the scheduler runs (hours)               |
| MIN_VOCAB_THRESHOLD | 10           | Min vocabulary count for a topic to be marked DONE |
| SCHEDULER_ENABLED   | true         | Master on/off switch                               |

---

## 2. REST API Endpoints

### Topics (extended)

| Method | Path                          | Auth     | Description                               |
|--------|-------------------------------|----------|-------------------------------------------|
| GET    | `/api/v1/topics/paged`        | Public   | Paginated topics filtered by status       |
| PATCH  | `/api/v1/topics/{id}/status`  | ADMIN    | Manually update a topic's status          |

Query params for `/paged`:
- `status` — `ACTIVE` (default) or `DONE`
- `page` — 0-based (default `0`)
- `size` — items per page (default `18`)

### Admin Scheduler Config

| Method | Path                                   | Auth  | Description              |
|--------|----------------------------------------|-------|--------------------------|
| GET    | `/api/v1/admin/scheduler/config`       | ADMIN | List all config entries  |
| PUT    | `/api/v1/admin/scheduler/config/{key}` | ADMIN | Update a config value    |

Updating `INTERVAL_HOURS` triggers an immediate reschedule at the new rate.

---

## 3. Spring Boot Scheduling Strategy

### Problem
`@Scheduled(fixedDelay = ...)` is static — the interval is baked in at compile time.

### Solution: `ThreadPoolTaskScheduler` + `ScheduledFuture`

```
┌────────────────────────────────┐
│   TopicSchedulerService        │
│  ┌──────────────────────────┐  │
│  │ ThreadPoolTaskScheduler  │  │
│  └──────────┬───────────────┘  │
│             │ scheduleAtFixedRate(task, duration)
│  ┌──────────▼───────────────┐  │
│  │  ScheduledFuture<?>      │  │  ← cancel + re-create on config change
│  └──────────────────────────┘  │
└────────────────────────────────┘
```

1. On `@PostConstruct`, read `INTERVAL_HOURS` from `scheduler_config`.
2. Schedule `chargeTopics()` via `taskScheduler.scheduleAtFixedRate(...)`.
3. When admin calls `PUT /admin/scheduler/config/INTERVAL_HOURS`, the service cancels the current `ScheduledFuture` and issues a new one with the updated duration.

### `chargeTopics()` logic

```
for each ACTIVE topic:
  count = vocabularyRepository.countByTopicId(topic.id)
  if count >= MIN_VOCAB_THRESHOLD:
    topic.status = DONE
    save(topic)
    log("Topic '%s' marked DONE", topic.name)
```

---

## 4. Angular Component Structure

```
features/vocabulary/
├── browse/           (existing — vocabulary word list)
└── topic-grid/       (NEW)
    ├── topic-grid.component.ts      — tab + pagination logic
    ├── topic-grid.component.html    — 6×3 CSS grid + tabs + paginator
    └── topic-grid.component.scss    — grid layout styles
```

### State (Angular Signals)

```typescript
activeTab   = signal<'ACTIVE' | 'DONE'>('ACTIVE')
topics      = signal<TopicPage | null>(null)
currentPage = signal(0)
loading     = signal(false)
```

### Tab switching

```
MatTabGroup selectedIndex change
  → activeTab.set(...)
  → currentPage.set(0)
  → loadTopics()
```

### Pagination

- Page size: **18** (6 columns × 3 rows)
- `MatPaginator` emits `PageEvent` → `currentPage.set(event.pageIndex)` → `loadTopics()`

### Grid CSS

```scss
.topic-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 1rem;
}

// responsive: ≤1024px → 3 cols, ≤640px → 2 cols
```
