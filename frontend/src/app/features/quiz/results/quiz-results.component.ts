import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { QuizService } from '../../../core/services/quiz.service';
import { Quiz } from '../../../core/models/quiz.model';

@Component({
  selector: 'app-quiz-results',
  standalone: true,
  imports: [
    CommonModule, MatCardModule, MatButtonModule,
    MatIconModule, MatDividerModule, MatProgressSpinnerModule,
  ],
  templateUrl: './quiz-results.component.html',
})
export class QuizResultsComponent implements OnInit {
  quiz = signal<Quiz | null>(null);

  scoreColor = computed(() => {
    const s = this.quiz()?.score ?? 0;
    if (s >= 80) return 'text-green-600';
    if (s >= 60) return 'text-yellow-600';
    return 'text-red-600';
  });

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.quizService.getById(id).subscribe(q => this.quiz.set(q));
  }

  tryAgain(): void {
    const q = this.quiz();
    if (q) this.router.navigate(['/quiz'], { queryParams: { topicId: q.topicId } });
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }
}
