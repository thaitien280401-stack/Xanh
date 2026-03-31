import { Component, signal, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  templateUrl: './app-layout.component.html',
})
export class AppLayoutComponent {
  private authService = inject(AuthService);

  currentUser = this.authService.currentUser;

  navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    { label: 'Pomodoro', icon: 'timer', route: '/pomodoro' },
    { label: 'Vocabulary', icon: 'menu_book', route: '/vocabulary' },
    { label: 'Quiz', icon: 'quiz', route: '/quiz' },
    { label: 'Leaderboard', icon: 'leaderboard', route: '/leaderboard' },
  ];

  sidebarOpen = signal(true);

  toggleSidebar(): void {
    this.sidebarOpen.update(v => !v);
  }

  logout(): void {
    this.authService.logout();
  }

  getUserInitials(): string {
    const name = this.currentUser()?.username ?? '';
    return name.slice(0, 2).toUpperCase() || 'U';
  }
}
