import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { SessionData, SessionService } from './services/session.service';
import { SupportApiService, SupportMessage } from './services/support-api.service';

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
  supportUnreadCount = 0;

  private navSub?: Subscription;
  private supportUnreadSub?: Subscription;
  private supportSocket?: WebSocket;
  private supportReconnectTimerId?: ReturnType<typeof setTimeout>;

  constructor(
    private readonly sessionService: SessionService,
    private readonly supportApi: SupportApiService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.initializeTheme();
    this.refreshSession();
    this.configureSupportUnreadRealtime();
    this.navSub = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.refreshSession();
        this.configureSupportUnreadRealtime();
      }
    });
  }

  ngOnDestroy(): void {
    this.navSub?.unsubscribe();
    this.stopSupportUnreadRealtime();
  }

  logout(): void {
    this.sessionService.clear();
    this.session = null;
    this.supportUnreadCount = 0;
    this.stopSupportUnreadRealtime();
    this.router.navigate(['/login']);
  }

  toggleTheme(): void {
    this.theme = this.theme === 'dark' ? 'light' : 'dark';
    this.applyTheme();
  }

  private refreshSession(): void {
    this.session = this.sessionService.get();
  }

  private configureSupportUnreadRealtime(): void {
    if (!this.session || this.session.role !== 'CUSTOMER') {
      this.supportUnreadCount = 0;
      this.stopSupportUnreadRealtime();
      return;
    }

    if (this.isCustomerSupportRoute(this.router.url)) {
      this.supportUnreadCount = 0;
    } else {
      // Load current unread state once, then keep it live with websocket events.
      this.refreshSupportUnreadCount();
    }

    if (!this.supportSocket || this.supportSocket.readyState === WebSocket.CLOSED) {
      this.connectSupportSocket();
    }
  }

  private refreshSupportUnreadCount(): void {
    if (!this.session || this.session.role !== 'CUSTOMER') {
      this.supportUnreadCount = 0;
      return;
    }

    this.supportUnreadSub?.unsubscribe();
    this.supportUnreadSub = this.supportApi.getConversationMessages(this.session.username).subscribe({
      next: (messages) => {
        this.supportUnreadCount = messages.filter((message) =>
          message.senderRole === 'OFFICER' && !message.readAt
        ).length;
      },
      error: () => {
        // Keep previous unread indicator on transient failures.
      }
    });
  }

  private stopSupportUnreadRealtime(): void {
    this.supportUnreadSub?.unsubscribe();
    this.supportUnreadSub = undefined;

    if (this.supportReconnectTimerId) {
      clearTimeout(this.supportReconnectTimerId);
      this.supportReconnectTimerId = undefined;
    }

    if (this.supportSocket) {
      this.supportSocket.close();
      this.supportSocket = undefined;
    }
  }

  private connectSupportSocket(): void {
    if (!this.session || this.session.role !== 'CUSTOMER') {
      return;
    }

    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const query = new URLSearchParams({
      username: this.session.username,
      role: this.session.role
    });

    this.supportSocket = new WebSocket(`${protocol}://localhost:8088/ws/support?${query.toString()}`);

    this.supportSocket.onmessage = (event) => {
      this.handleSupportSocketEvent(event.data);
    };

    this.supportSocket.onclose = () => {
      this.supportSocket = undefined;
      if (!this.session || this.session.role !== 'CUSTOMER') {
        return;
      }
      if (this.supportReconnectTimerId) {
        return;
      }
      this.supportReconnectTimerId = setTimeout(() => {
        this.supportReconnectTimerId = undefined;
        this.connectSupportSocket();
      }, 3000);
    };
  }

  private handleSupportSocketEvent(raw: string): void {
    if (!this.session || this.session.role !== 'CUSTOMER') {
      return;
    }

    let event: { type?: string; payload?: unknown };
    try {
      event = JSON.parse(raw);
    } catch {
      return;
    }

    if (event.type === 'message') {
      const message = event.payload as SupportMessage;
      if (
        message &&
        message.customerUsername?.toLowerCase() === this.session.username.toLowerCase() &&
        message.senderRole === 'OFFICER' &&
        !message.readAt &&
        !this.isCustomerSupportRoute(this.router.url)
      ) {
        this.supportUnreadCount += 1;
      }
    }
  }

  private isCustomerSupportRoute(url: string): boolean {
    return url.startsWith('/support');
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
