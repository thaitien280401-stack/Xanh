export interface LeaderboardEntry {
  rank: number;
  userId: string;
  username: string;
  avatarUrl: string | null;
  totalWordsLearned: number;
  totalStudyMinutes: number;
  totalQuizzes: number;
  highestQuizScore: number;
  weeklyPoints: number;
  totalPoints: number;
}

export interface ProgressStats {
  totalSessions: number;
  totalStudyMinutes: number;
  totalWordsStudied: number;
  totalQuizzesCompleted: number;
  averageQuizScore: number;
  masteryBreakdown: Record<string, number>;
}
