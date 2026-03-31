export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';

export interface Vocabulary {
  id: string;
  topicId: string;
  topicName: string;
  word: string;
  definition: string;
  pronunciation: string;
  partOfSpeech: string;
  exampleSentence: string;
  audioUrl: string;
  imageUrl: string;
  difficulty: Difficulty;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
