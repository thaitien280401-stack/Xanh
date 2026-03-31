import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { QuizService } from '../../../core/services/quiz.service';
import { Quiz, QuizQuestion } from '../../../core/models/quiz.model';

@Component({
  selector: 'app-quiz-take',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatProgressBarModule, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule,
  ],
  templateUrl: './quiz-take.component.html',
})
export class QuizTakeComponent implements OnInit {
  quiz = signal<Quiz | null>(null);
  currentIndex = signal(0);
  answers = signal<Record<string, string>>({});
  startTime = Date.now();
  submitting = signal(false);
  fillAnswer = signal('');
  selectedOption = signal<string | null>(null);

  currentQuestion = computed<QuizQuestion | null>(() => {
    const q = this.quiz();
    if (!q) return null;
    return q.questions[this.currentIndex()] ?? null;
  });

  progress = computed(() => {
    const q = this.quiz();
    if (!q) return 0;
    return ((this.currentIndex() + 1) / q.totalQuestions) * 100;
  });

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.quizService.getById(id).subscribe(q => this.quiz.set(q));
    this.startTime = Date.now();
  }

  selectOption(opt: string): void {
    this.selectedOption.set(opt);
  }

  next(): void {
    const q = this.currentQuestion();
    if (!q) return;
    const answer = this.quiz()!.quizType === 'FILL_BLANK'
      ? this.fillAnswer()
      : (this.selectedOption() ?? '');
    this.answers.update(a => ({ ...a, [q.id]: answer }));
    this.fillAnswer.set('');
    this.selectedOption.set(null);

    if (this.currentIndex() + 1 < this.quiz()!.totalQuestions) {
      this.currentIndex.update(i => i + 1);
    } else {
      this.submit();
    }
  }

  private submit(): void {
    const quiz = this.quiz()!;
    this.submitting.set(true);
    const timeTaken = Math.round((Date.now() - this.startTime) / 1000);
    this.quizService.submit(quiz.id, {
      answers: this.answers(),
      timeTakenSeconds: timeTaken,
    }).subscribe({
      next: () => this.router.navigate(['/quiz/results', quiz.id]),
      error: () => this.router.navigate(['/quiz/results', quiz.id]),
    });
  }
}
