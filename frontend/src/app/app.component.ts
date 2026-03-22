import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { SessionData, SessionService } from './services/session.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  session: SessionData | null = null;
  theme: 'dark' | 'light' = 'dark';
  private navSub?: Subscription;

  constructor(private readonly sessionService: SessionService, private readonly router: Router) {}

  ngOnInit(): void {
    this.initializeTheme();
    this.refreshSession();
    this.navSub = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.refreshSession();
      }
    });
  }

  ngOnDestroy(): void {
    this.navSub?.unsubscribe();
  }

  logout(): void {
    this.sessionService.clear();
    this.session = null;
    this.router.navigate(['/login']);
  }

  toggleTheme(): void {
    this.theme = this.theme === 'dark' ? 'light' : 'dark';
    this.applyTheme();
  }

  private refreshSession(): void {
    this.session = this.sessionService.get();
  }

  private initializeTheme(): void {
    const savedTheme = localStorage.getItem('psm-theme');
    if (savedTheme === 'light' || savedTheme === 'dark') {
      this.theme = savedTheme;
    } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) {
      this.theme = 'light';
    }
    this.applyTheme();
  }

  private applyTheme(): void {
    document.documentElement.setAttribute('data-theme', this.theme);
    localStorage.setItem('psm-theme', this.theme);
  }
}
