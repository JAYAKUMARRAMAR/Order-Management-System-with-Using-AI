import { Injectable } from '@angular/core';
import { signal } from '@angular/core';

/**
 * LoadingStateService manages loading states for API calls
 * Uses Angular signals for reactive state management
 */
@Injectable({
  providedIn: 'root'
})
export class LoadingStateService {
  
  private loadingStates = signal<{ [key: string]: boolean }>({});

  /**
   * Set loading state for a specific operation
   */
  setLoading(operationId: string, isLoading: boolean): void {
    const states = { ...this.loadingStates() };
    states[operationId] = isLoading;
    this.loadingStates.set(states);
  }

  /**
   * Get loading state for a specific operation
   */
  isLoading(operationId: string): boolean {
    return this.loadingStates()[operationId] || false;
  }

  /**
   * Check if any operation is loading
   */
  isAnyLoading(): boolean {
    return Object.values(this.loadingStates()).some(state => state);
  }

  /**
   * Clear all loading states
   */
  clearAllLoading(): void {
    this.loadingStates.set({});
  }

  /**
   * Get all loading states as a signal
   */
  getLoadingStates() {
    return this.loadingStates;
  }
}
