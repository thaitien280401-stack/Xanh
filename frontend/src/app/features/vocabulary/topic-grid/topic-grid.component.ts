import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TopicService } from '../../../core/services/topic.service';
import { Topic, TopicPage, TopicStatus } from '../../../core/models/topic.model';

@Component({
  selector: 'app-topic-grid',
  standalone: true,
  imports: [
    CommonModule, RouterModule, ReactiveFormsModule, FormsModule,
    MatTabsModule, MatButtonModule, MatIconModule,
    MatPaginatorModule, MatProgressSpinnerModule,
    MatFormFieldModule, MatInputModule, MatSnackBarModule,
  ],
  templateUrl: './topic-grid.component.html',
  styleUrl: './topic-grid.component.scss',
})
export class TopicGridComponent implements OnInit {
  private topicService = inject(TopicService);
  private route        = inject(ActivatedRoute);
  private router       = inject(Router);
  private fb           = inject(FormBuilder);
  private snackBar     = inject(MatSnackBar);

  readonly PAGE_SIZE = 18;

  // ── Grid state ─────────────────────────────────────────────────
  activeTab   = signal<TopicStatus>('ACTIVE');
  topicPage   = signal<TopicPage | null>(null);
  currentPage = signal(0);
  loading     = signal(false);

  totalElements = computed(() => this.topicPage()?.totalElements ?? 0);
  topics        = computed(() => this.topicPage()?.content ?? []);

  // ── Pomodoro create-topic panel ────────────────────────────────
  pomodoroMode  = signal(false);
  showForm      = signal(false);
  submitting    = signal(false);
  syncing       = signal(false);

  /** Keywords to send to the AI sync endpoint (editable via the sync input) */
  syncKeywords  = signal<string>('');

  createForm = this.fb.group({
    name:        ['', [Validators.required, Validators.maxLength(100)]],
    description: [''],
  });

  // ── Lifecycle ──────────────────────────────────────────────────
  ngOnInit(): void {
    // Detect ?mode=pomodoro from session-setup navigation
    const mode = this.route.snapshot.queryParamMap.get('mode');
    if (mode === 'pomodoro') {
      this.pomodoroMode.set(true);
      this.showForm.set(true);
    }
    this.loadTopics();
  }

  // ── Tab & pagination ───────────────────────────────────────────
  onTabChange(index: number): void {
    this.activeTab.set(index === 0 ? 'ACTIVE' : 'DONE');
    this.currentPage.set(0);
    this.loadTopics();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.loadTopics();
  }

  trackById(_: number, topic: Topic): string {
    return topic.id;
  }

  // ── Create topic (Pomodoro Mode) ───────────────────────────────
  toggleForm(): void {
    this.showForm.update(v => !v);
    if (!this.showForm()) this.createForm.reset();
  }

  submitCreate(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    const { name, description } = this.createForm.value;
    this.topicService.create({ name: name!, description: description ?? undefined }).subscribe({
      next: topic => {
        this.submitting.set(false);
        this.createForm.reset();
        this.showForm.set(false);
        this.snackBar.open(`Topic "${topic.name}" created!`, 'Close', { duration: 3000 });
        this.loadTopics();

        // If we arrived from Pomodoro setup, offer to go back
        if (this.pomodoroMode()) {
          this.snackBar.open(
            `"${topic.name}" added. Go back to start your session.`,
            'Go to Pomodoro',
            { duration: 6000 }
          ).onAction().subscribe(() => this.router.navigate(['/pomodoro']));
        }
      },
      error: err => {
        this.submitting.set(false);
        this.snackBar.open(
          err?.error?.message ?? 'Failed to create topic. Admin access required.',
          'Close',
          { duration: 4000 }
        );
      },
    });
  }

  // ── Pomodoro Sync ──────────────────────────────────────────────
  /**
   * Calls POST /api/v1/topics/pomodoro-sync with the entered keywords.
   * On success: shows a summary snackbar and refreshes the grid.
   */
  onPomodoroSync(): void {
    const raw = this.syncKeywords().trim();
    if (!raw) {
      this.snackBar.open('Enter at least one keyword to sync.', 'Close', { duration: 3000 });
      return;
    }
    const keywords = raw.split(',').map(k => k.trim()).filter(k => k.length > 0);

    this.syncing.set(true);
    this.topicService.pomodoroSync(keywords).subscribe({
      next: res => {
        this.syncing.set(false);
        this.syncKeywords.set('');
        const msg = `Sync complete — ${res.created} created, ${res.updated} updated, ${res.wordsAdded} words added.`;
        this.snackBar.open(msg, 'Close', { duration: 5000 });
        this.loadTopics(); // refresh 6×3 grid
      },
      error: err => {
        this.syncing.set(false);
        this.snackBar.open(
          err?.error?.message ?? 'Sync failed. Please try again.',
          'Close',
          { duration: 4000 }
        );
      },
    });
  }

  // ── Private ────────────────────────────────────────────────────
  private loadTopics(): void {
    this.loading.set(true);
    this.topicService.getPaged(this.activeTab(), this.currentPage(), this.PAGE_SIZE).subscribe({
      next: page => { this.topicPage.set(page); this.loading.set(false); },
      error: ()   => this.loading.set(false),
    });
  }
}
