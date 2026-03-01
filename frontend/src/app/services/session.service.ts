import { Injectable } from '@angular/core';

export interface SessionData {
  username: string;
  role: 'CUSTOMER' | 'OFFICER';
}

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly key = 'auth-session';

  save(session: SessionData): void {
    localStorage.setItem(this.key, JSON.stringify(session));
  }

  get(): SessionData | null {
    const raw = localStorage.getItem(this.key);
    return raw ? (JSON.parse(raw) as SessionData) : null;
  }

  clear(): void {
    localStorage.removeItem(this.key);
  }

  isLoggedIn(): boolean {
    return this.get() !== null;
  }
}
