export type QuizType = 'MULTIPLE_CHOICE' | 'FILL_BLANK' | 'TRUE_FALSE';

export interface QuizQuestion {
  id: string;
  vocabularyId: string;
  word: string;
  questionText: string;
  options: string[];
  correctAnswer: string;
  userAnswer: string | null;
  isCorrect: boolean | null;
}

export interface Quiz {
  id: string;
  topicId: string;
  topicName: string;
  quizType: QuizType;
  totalQuestions: number;
  correctAnswers: number | null;
  score: number | null;
  timeTakenSeconds: number | null;
  completedAt: string | null;
  questions: QuizQuestion[];
  createdAt: string;
}

export interface GenerateQuizRequest {
  topicId: string;
  quizType: QuizType;
  questionCount: number;
}

export interface SubmitQuizRequest {
  answers: Record<string, string>;
  timeTakenSeconds: number;
}
