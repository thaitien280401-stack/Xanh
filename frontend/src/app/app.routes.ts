import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },

  {
    path: 'auth',
    canActivate: [guestGuard],
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
      },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },

  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/app-layout/app-layout.component').then(m => m.AppLayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'pomodoro',
        children: [
          {
            path: '',
            loadComponent: () => import('./features/pomodoro/session-setup/session-setup.component').then(m => m.SessionSetupComponent)
          },
          {
            path: 'session/:id',
            loadComponent: () => import('./features/pomodoro/timer/timer.component').then(m => m.TimerComponent)
          },
          {
            path: 'summary/:id',
            loadComponent: () => import('./features/pomodoro/session-summary/session-summary.component').then(m => m.SessionSummaryComponent)
          }
        ]
      },
      {
        path: 'vocabulary',
        children: [
          {
            path: '',
            loadComponent: () => import('./features/vocabulary/browse/browse.component').then(m => m.BrowseComponent)
          },
          {
            path: 'import',
            loadComponent: () => import('./features/vocabulary/import/import.component').then(m => m.ImportComponent)
          }
        ]
      },
      {
        path: 'quiz',
        children: [
          {
            path: '',
            loadComponent: () => import('./features/quiz/setup/quiz-setup.component').then(m => m.QuizSetupComponent)
          },
          {
            path: 'take/:id',
            loadComponent: () => import('./features/quiz/take/quiz-take.component').then(m => m.QuizTakeComponent)
          },
          {
            path: 'results/:id',
            loadComponent: () => import('./features/quiz/results/quiz-results.component').then(m => m.QuizResultsComponent)
          }
        ]
      },
      {
        path: 'leaderboard',
        loadComponent: () => import('./features/leaderboard/leaderboard.component').then(m => m.LeaderboardComponent)
      }
    ]
  },

  { path: '**', redirectTo: '/dashboard' }
];
