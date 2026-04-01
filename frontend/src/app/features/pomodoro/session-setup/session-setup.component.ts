import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TopicService } from '../../../core/services/topic.service';
import { PomodoroService } from '../../../core/services/pomodoro.service';
import { Topic } from '../../../core/models/topic.model';

@Component({
  selector: 'app-session-setup',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './session-setup.component.html',
  styleUrl: './session-setup.component.scss',
})
export class SessionSetupComponent implements OnInit {
  private fb              = inject(FormBuilder);
  private topicService    = inject(TopicService);
  private pomodoroService = inject(PomodoroService);
  private router          = inject(Router);

  // ── Pagination constants ───────────────────────────────────────
  readonly ITEMS_PER_PAGE = 18; // 6 columns × 3 rows

  // ── State ─────────────────────────────────────────────────────
  allTopics        = signal<Topic[]>([]);
  loadingTopics    = signal(true);
  starting         = signal(false);
  errorMessage     = signal('');
  selectedDuration = signal<25 | 50>(25);
  currentPage      = signal(0);

  // ── Computed ──────────────────────────────────────────────────
  totalPages = computed(() =>
    Math.ceil(this.allTopics().length / this.ITEMS_PER_PAGE)
  );

  pagedTopics = computed(() => {
    const start = this.currentPage() * this.ITEMS_PER_PAGE;
    return this.allTopics().slice(start, start + this.ITEMS_PER_PAGE);
  });

  pageNumbers = computed(() =>
    Array.from({ length: this.totalPages() }, (_, i) => i)
  );

  selectedTopicName = computed(() => {
    const id = this.form?.value?.topicId;
    return id ? (this.allTopics().find(t => t.id === id)?.name ?? '') : '';
  });

  // ── Form ──────────────────────────────────────────────────────
  form = this.fb.group({
    topicId: ['', Validators.required],
  });

  durations: Array<{ value: 25 | 50; label: string; description: string }> = [
    { value: 25, label: '25 min', description: 'Short session' },
    { value: 50, label: '50 min', description: 'Long session' },
  ];

  // ── Lifecycle ─────────────────────────────────────────────────
  ngOnInit(): void {
    this.topicService.getAll().subscribe({
      next: topics => {
        this.allTopics.set(topics);
        this.loadingTopics.set(false);
      },
      error: () => {
        this.loadingTopics.set(false);
        this.errorMessage.set('Failed to load topics.');
      },
    });
  }

  // ── Topic selection ───────────────────────────────────────────
  selectTopic(topicId: string): void {
    this.form.patchValue({ topicId });
    this.form.get('topicId')?.markAsTouched();
  }

  // ── Pagination ────────────────────────────────────────────────
  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages()) return;
    this.currentPage.set(page);
  }

  // ── Duration ──────────────────────────────────────────────────
  selectDuration(value: 25 | 50): void {
    this.selectedDuration.set(value);
  }

  // ── Pomodoro Mode ─────────────────────────────────────────────
  /**
   * Prepares a new topic with isPomodoro: true and routes to the
   * topic-grid page so the user can create it in Pomodoro mode.
   */
  onAddPomodoroTopic(): void {
    const pendingTopic = { isPomodoro: true };
    this.router.navigate(['/vocabulary/topics'], {
      state: { pendingTopic },
      queryParams: { mode: 'pomodoro' },
    });
  }

  // ── Start session ─────────────────────────────────────────────
  startSession(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.starting.set(true);
    this.errorMessage.set('');

    this.pomodoroService.startSession({
      topicId: this.form.value.topicId!,
      durationMinutes: this.selectedDuration(),
    }).subscribe({
      next: session => {
        this.starting.set(false);
        this.router.navigate(['/pomodoro/session', session.id]);
      },
      error: err => {
        this.starting.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Failed to start session.');
      },
    });
  }
}
