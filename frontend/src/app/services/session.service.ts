import { Injectable } from '@angular/core';

export interface SessionData {
  username: string;
  role: 'CUSTOMER' | 'OFFICER';
  token: string;
}

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly key = 'auth-session';

  save(session: SessionData): void {
    localStorage.setItem(this.key, JSON.stringify(session));
  }

  get(): SessionData | null {
    const raw = localStorage.getItem(this.key);
    if (!raw) {
      return null;
    }

    const parsed = JSON.parse(raw) as Partial<SessionData>;
    if (!parsed?.username || !parsed?.role || !parsed?.token) {
      this.clear();
      return null;
    }

    return parsed as SessionData;
  }

  clear(): void {
    localStorage.removeItem(this.key);
  }

  isLoggedIn(): boolean {
    return this.get() !== null;
  }
}
