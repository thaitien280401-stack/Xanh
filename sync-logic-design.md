# Sync Logic Design — Pomodoro Topic Sync (Upsert Pattern)

## 1. Overview

`POST /api/v1/topics/pomodoro-sync` accepts a list of **keywords**,
calls an AI/Vocabulary API to retrieve related word data, then **upserts**
topics and vocabulary into the database.

```
Frontend (button click)
    │
    ▼
POST /api/v1/topics/pomodoro-sync  { "keywords": ["technology", "science"] }
    │
    ▼
PomodoroSyncController
    │
    ▼
PomodoroSyncService.sync(keywords)
    ├─► AiVocabSyncService.analyze(keywords)          ← AI/External API call
    │       returns List<TopicSyncData>
    │         each: { topicName, words[] }
    │
    └─► For each TopicSyncData:
          ┌─ topicRepository.findByName(name) ─┐
          │  EXISTS?                            │
          │  YES → UPDATE (append new words)    │
          │  NO  → CREATE (new topic + words)   │
          └─────────────────────────────────────┘
    │
    ▼
PomodoroSyncResponse { created, updated, wordsAdded, topics[] }
    │
    ▼
Frontend refreshes 6×3 grid
```

---

## 2. API Data Structure

### Request
```json
POST /api/v1/topics/pomodoro-sync
Authorization: Bearer <token>

{
  "keywords": ["technology", "business", "science"]
}
```

### Response
```json
{
  "created": 2,
  "updated": 1,
  "wordsAdded": 14,
  "topics": [
    {
      "id": "uuid",
      "name": "Technology",
      "description": "Auto-generated via Pomodoro sync",
      "vocabularyCount": 7,
      "status": "ACTIVE",
      "createdAt": "2026-04-01T10:00:00"
    }
  ]
}
```

### Internal model (`TopicSyncData`)
```java
record TopicSyncData(String topicName, List<WordEntry> words) {}
```

---

## 3. Upsert Flow (Detailed)

```
for each TopicSyncData(topicName, words):

  topic = topicRepository.findByName(topicName)

  if topic IS PRESENT:                       // UPDATE path
    for each word in words:
      if NOT vocabularyRepository.existsByWordAndTopicId(word, topic.id):
        vocabularyRepository.save(new Vocabulary(topic, word, ...))
        wordsAdded++
    if wordsAdded > 0: updated++

  if topic IS EMPTY:                         // CREATE path
    newTopic = topicRepository.save(Topic(topicName, "Auto-generated..."))
    for each word in words:
      vocabularyRepository.save(new Vocabulary(newTopic, word, ...))
      wordsAdded++
    created++
```

**Key guarantees:**
- No duplicate topics (checked by name)
- No duplicate vocabulary (checked by word + topic_id composite)
- Existing topic data is **never deleted** — only appended
- All within a single `@Transactional` boundary

---

## 4. AI Service Placeholder

`AiVocabSyncService` is the **only component that needs to change** when
swapping in a real AI provider:

```
Current (placeholder):
  keyword → ExternalVocabApiClient.fetchWord(keyword)
  (uses free dictionary API as a stand-in)

Future (real AI):
  keywords → OpenAI/Gemini API
  → "Give me 10 vocab words related to: {keywords}"
  → parse structured JSON response → List<TopicSyncData>
```

The rest of the pipeline (`PomodoroSyncService`, controller, frontend) is
**AI-provider agnostic**.

---

## 5. Frontend State Flow

```
[Sync Button] click
    │ syncing = true  (shows spinner)
    ▼
topicService.pomodoroSync(keywords)
    │
    ▼  on success:
    ├─ snackbar "Sync complete: X created, Y updated"
    ├─ loadTopics()   ← refreshes 6×3 grid
    └─ syncing = false

    on error:
    ├─ snackbar error message
    └─ syncing = false
```
