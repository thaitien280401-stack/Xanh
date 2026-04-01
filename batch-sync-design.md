# Batch AI Sync Design — High-Volume Vocabulary Synchronization

## 1. Topic ↔ Vocabulary (One-to-Many) Relationship

```
┌──────────────────┐          ┌──────────────────────────┐
│  topics          │  1    *  │  vocabularies            │
│──────────────────│──────────│──────────────────────────│
│ id        UUID PK│          │ id            UUID PK    │
│ name      VARCHAR│          │ topic_id      UUID FK ──►│
│ description TEXT │          │ word          VARCHAR    │
│ status    VARCHAR│          │ definition    TEXT       │
│ created_at       │          │ pronunciation VARCHAR    │
│ updated_at       │          │ part_of_speech VARCHAR   │
└──────────────────┘          │ example_sentence TEXT    │
                              │ audio_url     TEXT       │
                              │ difficulty    VARCHAR    │
                              └──────────────────────────┘
```

A single `Topic` owns many `Vocabulary` entries via `@OneToMany(mappedBy="topic")`.
The FK `topic_id` is indexed for fast lookup.

---

## 2. Batch Processing Configuration (application.yml)

```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50           # flush 50 INSERTs per round-trip
        order_inserts: true        # group same-entity INSERTs for batching
        order_updates: true        # group same-entity UPDATEs for batching
        generate_statistics: false # avoid overhead in production
```

### Why these settings matter

| Setting | Effect |
|---------|--------|
| `batch_size: 50` | 400 words = 8 round-trips instead of 400 |
| `order_inserts: true` | Hibernate groups entity types before flush → no interleaving |
| `GenerationType.UUID` | UUID PKs are pre-assigned in Java → **no IDENTITY select-after-insert**, so batching is not blocked |

> **Note:** `GenerationType.IDENTITY` defeats batch inserts because Hibernate must
> round-trip to get the generated key.  Our `BaseEntity` uses `GenerationType.UUID`,
> so `saveAll()` batches correctly.

---

## 3. Upsert + Deduplication Strategy

```
POST /api/v1/topics/sync-ai  { topicName, wordCount }
          │
          ▼
AiHighVolumeSyncService.syncTopic(topicName, wordCount)
          │
          ├─ 1. AI placeholder → generates List<WordEntry> (200–400 items)
          │
          ├─ 2. UPSERT topic
          │       topicRepository.findByName(topicName)
          │          EXISTS  → reuse entity
          │          ABSENT  → topicRepository.save(new Topic)
          │
          ├─ 3. ONE query for all existing words
          │       existingWords = vocabularyRepository.findWordsByTopicId(topicId)
          │       → Set<String> (lowercase) for O(1) lookup
          │
          ├─ 4. Deduplicate (stream + filter)
          │       newVocabs = aiWords.stream()
          │           .filter(w -> !existingWords.contains(w.word().toLowerCase()))
          │           .map(w -> Vocabulary.builder()...build())
          │           .toList();
          │
          └─ 5. Batch insert
                  vocabularyRepository.saveAll(newVocabs)
                  → Hibernate batches in groups of 50
```

### Key optimisation vs previous approach

| Old (per-word check) | New (batch) |
|----------------------|-------------|
| N × `existsByWordAndTopicId()` | 1 × `findWordsByTopicId()` |
| N × `save(vocab)` | 1 × `saveAll(vocabs)` |
| **400 SELECT + 400 INSERT** | **1 SELECT + 8 batched INSERTs** |

---

## 4. API Contract

### Request
```
POST /api/v1/topics/sync-ai
Authorization: Bearer <token>
Content-Type: application/json

{
  "topicName": "Technology",
  "wordCount": 300
}
```

### Response
```json
{
  "topicId":            "uuid",
  "topicName":          "Technology",
  "totalGenerated":     300,
  "saved":              287,
  "skippedDuplicates":  13,
  "processingTimeMs":   1240
}
```

---

## 5. Frontend Timeout Strategy

AI analysis + 400 DB writes can take 5–60 seconds depending on load.

```
HTTP request
  │ timeout(90_000ms)          ← RxJS timeout operator
  │ catchError(TimeoutError)   ← show "still processing" message
  │ catchError(HttpError)      ← show API error message
  ▼
LoadingService.setLoading(true, "Syncing 300 words with AI…")
  │
  ▼  on response / error:
LoadingService.setLoading(false)
grid refresh
```

`LoadingService` is an injectable Angular service holding a `BehaviorSubject`
so the loading state is observable from any component without prop-drilling.
