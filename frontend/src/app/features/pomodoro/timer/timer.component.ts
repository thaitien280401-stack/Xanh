import { Component, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule } from '@angular/material/dialog';
import { PomodoroService } from '../../../core/services/pomodoro.service';
import { VocabularyService } from '../../../core/services/vocabulary.service';
import { PomodoroSession } from '../../../core/models/pomodoro.model';
import { Vocabulary } from '../../../core/models/vocabulary.model';

@Component({
  selector: 'app-timer',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatDialogModule,
  ],
  templateUrl: './timer.component.html',
})
export class TimerComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private pomodoroService = inject(PomodoroService);
  private vocabularyService = inject(VocabularyService);

  session = signal<PomodoroSession | null>(null);
  vocabulary = signal<Vocabulary[]>([]);
  currentIndex = signal(0);
  flipped = signal(false);
  loading = signal(true);

  totalSeconds = signal(0);
  secondsLeft = signal(0);
  timerRunning = signal(false);
  private intervalId: ReturnType<typeof setInterval> | null = null;

  completing = signal(false);
  abandoning = signal(false);

  get currentWord(): Vocabulary | null {
    const words = this.vocabulary();
    const idx = this.currentIndex();
    return words.length > 0 ? words[idx] : null;
  }

  get progressValue(): number {
    const total = this.totalSeconds();
    if (total === 0) return 0;
    return ((total - this.secondsLeft()) / total) * 100;
  }

  get timerDisplay(): string {
    const s = this.secondsLeft();
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`;
  }

  get circumference(): number {
    return 2 * Math.PI * 80;
  }

  get strokeDashoffset(): number {
    const progress = this.secondsLeft() / (this.totalSeconds() || 1);
    return this.circumference * (1 - progress);
  }

  ngOnInit(): void {
    const sessionId = this.route.snapshot.paramMap.get('id')!;
    this.pomodoroService.getSession(sessionId).subscribe({
      next: (s) => {
        this.session.set(s);
        const secs = s.durationMinutes * 60;
        this.totalSeconds.set(secs);
        this.secondsLeft.set(secs);
        this.loadVocabulary(s.topicId);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }

  private loadVocabulary(topicId: string): void {
    this.vocabularyService.getByTopic(topicId, 0, 50).subscribe({
      next: (page) => {
        this.vocabulary.set(page.content);
        this.loading.set(false);
        this.startTimer();
      },
      error: () => {
        this.loading.set(false);
        this.startTimer();
      },
    });
  }

  private startTimer(): void {
    this.timerRunning.set(true);
    this.intervalId = setInterval(() => {
      const left = this.secondsLeft();
      if (left <= 1) {
        this.secondsLeft.set(0);
        this.timerRunning.set(false);
        clearInterval(this.intervalId!);
      } else {
        this.secondsLeft.set(left - 1);
      }
    }, 1000);
  }

  nextCard(): void {
    this.flipped.set(false);
    const words = this.vocabulary();
    if (words.length === 0) return;
    this.currentIndex.update(i => (i + 1) % words.length);
  }

  flipCard(): void {
    this.flipped.update(v => !v);
  }

  complete(): void {
    const s = this.session();
    if (!s) return;
    this.completing.set(true);
    this.stopTimer();
    this.pomodoroService.completeSession(s.id, this.currentIndex()).subscribe({
      next: () => {
        this.completing.set(false);
        this.router.navigate(['/pomodoro/summary', s.id]);
      },
      error: () => {
        this.completing.set(false);
        this.router.navigate(['/pomodoro/summary', s.id]);
      },
    });
  }

  abandon(): void {
    const s = this.session();
    if (!s) return;
    this.abandoning.set(true);
    this.stopTimer();
    this.pomodoroService.abandonSession(s.id).subscribe({
      next: () => {
        this.abandoning.set(false);
        this.router.navigate(['/pomodoro']);
      },
      error: () => {
        this.abandoning.set(false);
        this.router.navigate(['/pomodoro']);
      },
    });
  }

  private stopTimer(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
    this.timerRunning.set(false);
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }
}
