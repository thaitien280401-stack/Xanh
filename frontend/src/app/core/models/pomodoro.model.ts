export type SessionStatus = 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED';

export interface PomodoroSession {
  id: string;
  topicId: string;
  topicName: string;
  status: SessionStatus;
  durationMinutes: number;
  wordsStudied: number;
  startedAt: string;
  endedAt: string | null;
  createdAt: string;
}

export interface StartSessionRequest {
  topicId: string;
  durationMinutes: number;
}
