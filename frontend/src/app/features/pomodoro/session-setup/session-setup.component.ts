import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
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
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './session-setup.component.html',
})
export class SessionSetupComponent implements OnInit {
  private fb = inject(FormBuilder);
  private topicService = inject(TopicService);
  private pomodoroService = inject(PomodoroService);
  private router = inject(Router);

  topics = signal<Topic[]>([]);
  loadingTopics = signal(true);
  starting = signal(false);
  errorMessage = signal('');
  selectedDuration = signal<25 | 50>(25);

  form = this.fb.group({
    topicId: ['', Validators.required],
  });

  durations: Array<{ value: 25 | 50; label: string; description: string }> = [
    { value: 25, label: '25 min', description: 'Short session' },
    { value: 50, label: '50 min', description: 'Long session' },
  ];

  ngOnInit(): void {
    this.topicService.getAll().subscribe({
      next: (topics) => {
        this.topics.set(topics);
        this.loadingTopics.set(false);
      },
      error: () => {
        this.loadingTopics.set(false);
        this.errorMessage.set('Failed to load topics.');
      },
    });
  }

  selectDuration(value: 25 | 50): void {
    this.selectedDuration.set(value);
  }

  startSession(): void {
    if (this.form.invalid) return;
    this.starting.set(true);
    this.errorMessage.set('');

    this.pomodoroService.startSession({
      topicId: this.form.value.topicId!,
      durationMinutes: this.selectedDuration(),
    }).subscribe({
      next: (session) => {
        this.starting.set(false);
        this.router.navigate(['/pomodoro/session', session.id]);
      },
      error: (err) => {
        this.starting.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Failed to start session.');
      },
    });
  }
}
