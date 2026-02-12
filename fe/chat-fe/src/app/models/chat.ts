import { signal } from '@angular/core';
export interface askRequest {
  question: string;
  useRag: boolean;
}

export interface askResponse {
  answer: string;
  confidenceScore?: number;
  id?: number;
  source?: string;
  metadata?: Record<string, unknown>;
}
