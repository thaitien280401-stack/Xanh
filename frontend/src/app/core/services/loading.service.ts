import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface LoadingState {
  loading: boolean;
  message: string;
}

/**
 * App-wide loading indicator service.
 *
 * Consumers inject this service and call `show(msg)` / `hide()`.
 * Components subscribe to `state$` to drive spinners or overlays.
 */
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly _state = new BehaviorSubject<LoadingState>({ loading: false, message: '' });

  readonly state$: Observable<LoadingState> = this._state.asObservable();

  show(message = 'Loading...'): void {
    this._state.next({ loading: true, message });
  }

  hide(): void {
    this._state.next({ loading: false, message: '' });
  }

  get isLoading(): boolean {
    return this._state.getValue().loading;
  }
}
