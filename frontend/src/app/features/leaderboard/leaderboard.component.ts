import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { LeaderboardService } from '../../core/services/leaderboard.service';
import { AuthService } from '../../core/services/auth.service';
import { LeaderboardEntry } from '../../core/models/leaderboard.model';

@Component({
  selector: 'app-leaderboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatTabsModule],
  templateUrl: './leaderboard.component.html',
})
export class LeaderboardComponent implements OnInit {
  allTimeEntries = signal<LeaderboardEntry[]>([]);
  weeklyEntries = signal<LeaderboardEntry[]>([]);
  myRank = signal<LeaderboardEntry | null>(null);
  loading = signal(true);
  currentUserId: string | undefined;

  constructor(
    private leaderboardService: LeaderboardService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.authService.currentUser()?.userId;
    this.leaderboardService.getLeaderboard('all-time', 20).subscribe(e => this.allTimeEntries.set(e));
    this.leaderboardService.getLeaderboard('weekly', 20).subscribe(e => { this.weeklyEntries.set(e); this.loading.set(false); });
    this.leaderboardService.getMyRank().subscribe(r => this.myRank.set(r));
  }

  isCurrentUser(entry: LeaderboardEntry): boolean {
    return entry.userId === this.currentUserId;
  }

  rankMedal(rank: number): string {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return String(rank);
  }

  initials(username: string): string {
    return username.slice(0, 2).toUpperCase();
  }
}
