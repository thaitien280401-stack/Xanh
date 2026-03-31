import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatChipsModule } from '@angular/material/chips';
import { TopicService } from '../../../core/services/topic.service';
import { QuizService } from '../../../core/services/quiz.service';
import { Topic } from '../../../core/models/topic.model';
import { QuizType } from '../../../core/models/quiz.model';

@Component({
  selector: 'app-quiz-setup',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatSelectModule, MatFormFieldModule, MatButtonToggleModule, MatChipsModule,
  ],
  templateUrl: './quiz-setup.component.html',
})
export class QuizSetupComponent implements OnInit {
  topics = signal<Topic[]>([]);
  selectedTopicId = signal<string>('');
  selectedType = signal<QuizType>('MULTIPLE_CHOICE');
  questionCount = signal(10);
  loading = signal(false);
  error = signal('');

  quizTypes: { value: QuizType; label: string; icon: string }[] = [
    { value: 'MULTIPLE_CHOICE', label: 'Multiple Choice', icon: 'radio_button_checked' },
    { value: 'FILL_BLANK', label: 'Fill in the Blank', icon: 'edit' },
    { value: 'TRUE_FALSE', label: 'True / False', icon: 'thumbs_up_down' },
  ];

  constructor(
    private topicService: TopicService,
    private quizService: QuizService,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.topicService.getAll().subscribe(t => this.topics.set(t));
    const topicId = this.route.snapshot.queryParamMap.get('topicId');
    if (topicId) this.selectedTopicId.set(topicId);
  }

  generate(): void {
    if (!this.selectedTopicId()) return;
    this.loading.set(true);
    this.error.set('');
    this.quizService.generate({
      topicId: this.selectedTopicId(),
      quizType: this.selectedType(),
      questionCount: this.questionCount(),
    }).subscribe({
      next: quiz => this.router.navigate(['/quiz/take', quiz.id]),
      error: err => {
        this.error.set(err.error?.message || 'Failed to generate quiz. Make sure the topic has enough words.');
        this.loading.set(false);
      },
    });
  }
}
