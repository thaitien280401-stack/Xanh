export type TopicStatus = 'ACTIVE' | 'DONE';

export interface Topic {
  id: string;
  name: string;
  description: string;
  externalApiRef: string;
  vocabularyCount: number;
  status: TopicStatus;
  createdAt: string;
}

export interface TopicPage {
  content: Topic[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface PomodoroSyncRequest {
  keywords: string[];
}

export interface PomodoroSyncResponse {
  created: number;
  updated: number;
  wordsAdded: number;
  topics: Topic[];
}

export interface CreateTopicRequest {
  name: string;
  description?: string;
  externalApiRef?: string;
}
