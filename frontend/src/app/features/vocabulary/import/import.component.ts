import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TopicService } from '../../../core/services/topic.service';
import { VocabularyService } from '../../../core/services/vocabulary.service';
import { Topic } from '../../../core/models/topic.model';

@Component({
  selector: 'app-import',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatSnackBarModule,
  ],
  templateUrl: './import.component.html',
})
export class ImportComponent implements OnInit {
  topics = signal<Topic[]>([]);
  selectedTopicId = signal<string>('');
  wordInput = signal('');
  preview = signal<any[]>([]);
  searching = signal(false);
  importing = signal(false);
  importedWords = signal<Set<string>>(new Set());

  constructor(
    private topicService: TopicService,
    private vocabService: VocabularyService,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.topicService.getAll().subscribe(t => this.topics.set(t));
  }

  search(): void {
    const word = this.wordInput().trim();
    if (!word) return;
    this.searching.set(true);
    this.preview.set([]);
    this.topicService.searchExternal(word).subscribe({
      next: results => { this.preview.set(results); this.searching.set(false); },
      error: () => { this.searching.set(false); this.snackBar.open('Word not found', 'Close', { duration: 3000 }); },
    });
  }

  importWord(word: string): void {
    if (!this.selectedTopicId()) {
      this.snackBar.open('Please select a topic first', 'Close', { duration: 3000 });
      return;
    }
    this.importing.set(true);
    this.vocabService.importWord(this.selectedTopicId(), word).subscribe({
      next: imported => {
        this.importedWords.update(s => { s.add(word); return new Set(s); });
        this.importing.set(false);
        this.snackBar.open(`"${word}" imported successfully!`, 'Close', { duration: 3000 });
      },
      error: err => {
        this.importing.set(false);
        this.snackBar.open(err.error?.message || 'Import failed', 'Close', { duration: 3000 });
      },
    });
  }
}
