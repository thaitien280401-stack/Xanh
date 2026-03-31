export interface Topic {
  id: string;
  name: string;
  description: string;
  externalApiRef: string;
  vocabularyCount: number;
  createdAt: string;
}

export interface CreateTopicRequest {
  name: string;
  description?: string;
  externalApiRef?: string;
}
