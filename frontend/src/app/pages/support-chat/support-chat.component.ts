import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  SupportApiService,
  SupportConversationSummary,
  SupportMessage,
  SupportReadReceipt
} from '../../services/support-api.service';
import { SessionData, SessionService } from '../../services/session.service';

@Component({
  selector: 'app-support-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './support-chat.component.html',
  styleUrl: './support-chat.component.css'
})
export class SupportChatComponent implements OnInit, OnDestroy {
  session: SessionData | null = null;
  isOfficer = false;

  conversations: SupportConversationSummary[] = [];
  messages: SupportMessage[] = [];

  activeCustomerUsername = '';
  draftMessage = '';
  newConversationUsername = '';

  loadingMessages = false;
  sending = false;
  errorMessage = '';
  websocketConnected = false;

  private fallbackTimerId?: ReturnType<typeof setInterval>;
  private reconnectTimerId?: ReturnType<typeof setTimeout>;
  private websocket?: WebSocket;
  private markReadInFlight = false;

  @ViewChild('messagesContainer') private messagesContainer?: ElementRef<HTMLDivElement>;
  @ViewChild('bottomAnchor') private bottomAnchor?: ElementRef<HTMLDivElement>;

  constructor(
    private readonly supportApi: SupportApiService,
    private readonly sessionService: SessionService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.session = this.sessionService.get();
    if (!this.session) {
      this.router.navigate(['/login']);
      return;
    }

    this.isOfficer = this.session.role === 'OFFICER';

    if (this.isOfficer) {
      this.loadOfficerConversations(true);
    } else {
      this.activeCustomerUsername = this.session.username;
      this.loadActiveConversation();
    }

    this.connectRealtime();
    this.startFallbackPolling();
  }

  ngOnDestroy(): void {
    this.stopFallbackPolling();
    this.closeRealtime();

    if (this.reconnectTimerId) {
      clearTimeout(this.reconnectTimerId);
      this.reconnectTimerId = undefined;
    }
  }

  selectConversation(customerUsername: string): void {
    if (!customerUsername) {
      return;
    }

    this.activeCustomerUsername = customerUsername;
    this.errorMessage = '';
    this.sendPresenceUpdate();
    this.loadActiveConversation();
  }

  openConversationByUsername(): void {
    const normalized = this.newConversationUsername.trim();
    if (!normalized) {
      this.errorMessage = 'Enter a customer username to open a conversation.';
      return;
    }

    this.activeCustomerUsername = normalized;
    this.newConversationUsername = '';
    this.errorMessage = '';
    this.sendPresenceUpdate();
    this.loadActiveConversation();
  }

  sendMessage(): void {
    const message = this.draftMessage.trim();
    if (!message) {
      return;
    }

    if (!this.activeCustomerUsername) {
      this.errorMessage = 'Select a conversation before sending a message.';
      return;
    }

    this.sending = true;
    this.errorMessage = '';

    this.supportApi.sendMessage({
      customerUsername: this.isOfficer ? this.activeCustomerUsername : undefined,
      message
    }).subscribe({
      next: () => {
        this.draftMessage = '';
        this.sending = false;
        if (!this.websocketConnected) {
          this.loadActiveConversation();
        }
        if (this.isOfficer) {
          this.loadOfficerConversations(false);
        }
      },
      error: (error) => {
        this.sending = false;
        this.errorMessage = this.extractError(error, 'Unable to send message right now.');
      }
    });
  }

  trackByMessage(_: number, message: SupportMessage): number {
    return message.id;
  }

  trackByConversation(_: number, convo: SupportConversationSummary): string {
    return convo.customerUsername;
  }

  private loadOfficerConversations(selectFirst: boolean): void {
    this.supportApi.listConversations().subscribe({
      next: (conversations) => {
        this.conversations = conversations;

        if (!this.activeCustomerUsername && selectFirst && conversations.length > 0) {
          this.activeCustomerUsername = conversations[0].customerUsername;
          this.sendPresenceUpdate();
          this.loadActiveConversation();
          return;
        }

        if (this.activeCustomerUsername && !conversations.some((c) => c.customerUsername === this.activeCustomerUsername)) {
          this.messages = [];
        }
      },
      error: (error) => {
        this.errorMessage = this.extractError(error, 'Unable to load support conversations.');
      }
    });
  }

  private loadActiveConversation(): void {
    if (!this.activeCustomerUsername) {
      return;
    }

    this.loadingMessages = true;
    this.supportApi.getConversationMessages(this.activeCustomerUsername).subscribe({
      next: (messages) => {
        this.messages = messages;
        this.loadingMessages = false;
        this.scrollToLatestMessage();
        this.markActiveConversationRead();
      },
      error: (error) => {
        this.loadingMessages = false;
        this.messages = [];
        this.errorMessage = this.extractError(error, 'Unable to load support messages.');
      }
    });
  }

  isOwnMessage(message: SupportMessage): boolean {
    if (!this.session) {
      return false;
    }

    return message.senderUsername.toLowerCase() === this.session.username.toLowerCase();
  }

  private extractError(error: unknown, fallback: string): string {
    const payload = (error as { error?: unknown })?.error;
    if (payload && typeof payload === 'object') {
      const obj = payload as Record<string, unknown>;
      if (typeof obj['error'] === 'string') {
        return obj['error'];
      }
      const first = Object.values(obj)[0];
      if (typeof first === 'string') {
        return first;
      }
    }

    return fallback;
  }

  getReceiptLabel(message: SupportMessage): string {
    if (!this.isOwnMessage(message)) {
      return '';
    }

    if (message.readAt) {
      return 'Read';
    }
    if (message.deliveredAt) {
      return 'Delivered';
    }
    return 'Sent';
  }

  private markActiveConversationRead(): void {
    if (!this.activeCustomerUsername || this.markReadInFlight || !this.session) {
      return;
    }

    this.markReadInFlight = true;
    this.supportApi.markConversationRead(this.activeCustomerUsername).subscribe({
      next: (receipt) => this.applyReadReceipt(receipt),
      error: () => {
        // Ignore mark-read transient errors to avoid chat interruption.
      },
      complete: () => {
        this.markReadInFlight = false;
      }
    });
  }

  private applyReadReceipt(receipt: SupportReadReceipt): void {
    if (!receipt.messageIds?.length) {
      return;
    }

    const ids = new Set(receipt.messageIds);
    this.messages = this.messages.map((message) => {
      if (!ids.has(message.id)) {
        return message;
      }

      return {
        ...message,
        deliveredAt: receipt.readAt,
        readAt: receipt.readAt,
        readByUsername: receipt.readByUsername
      };
    });
  }

  private connectRealtime(): void {
    if (!this.session || this.websocketConnected) {
      return;
    }

    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const query = new URLSearchParams({
      username: this.session.username,
      role: this.session.role
    });
    const url = `${protocol}://localhost:8088/ws/support?${query.toString()}`;

    this.websocket = new WebSocket(url);

    this.websocket.onopen = () => {
      this.websocketConnected = true;
      this.stopFallbackPolling();
      this.sendPresenceUpdate();
    };

    this.websocket.onmessage = (event) => {
      this.handleRealtimeEvent(event.data);
    };

    this.websocket.onerror = () => {
      this.websocketConnected = false;
      this.startFallbackPolling();
    };

    this.websocket.onclose = () => {
      this.websocketConnected = false;
      this.startFallbackPolling();
      this.scheduleReconnect();
    };
  }

  private closeRealtime(): void {
    if (this.websocket) {
      this.websocket.close();
      this.websocket = undefined;
    }
  }

  private scheduleReconnect(): void {
    if (this.reconnectTimerId) {
      return;
    }

    this.reconnectTimerId = setTimeout(() => {
      this.reconnectTimerId = undefined;
      this.connectRealtime();
    }, 3000);
  }

  private sendPresenceUpdate(): void {
    if (!this.websocket || this.websocket.readyState !== WebSocket.OPEN) {
      return;
    }

    this.websocket.send(JSON.stringify({
      type: 'presence',
      activeCustomerUsername: this.activeCustomerUsername
    }));
  }

  private handleRealtimeEvent(data: string): void {
    let event: { type?: string; payload?: unknown };
    try {
      event = JSON.parse(data);
    } catch {
      return;
    }

    if (!event.type) {
      return;
    }

    if (event.type === 'message') {
      const incoming = event.payload as SupportMessage;
      if (!incoming || !incoming.customerUsername) {
        return;
      }

      if (this.isOfficer) {
        this.loadOfficerConversations(false);
      }

      if (incoming.customerUsername === this.activeCustomerUsername) {
        this.upsertMessage(incoming);
        if (!this.isOwnMessage(incoming)) {
          this.markActiveConversationRead();
        }
      }
      return;
    }

    if (event.type === 'read_receipt') {
      const receipt = event.payload as SupportReadReceipt;
      if (receipt.customerUsername === this.activeCustomerUsername) {
        this.applyReadReceipt(receipt);
      }
    }
  }

  private upsertMessage(incoming: SupportMessage): void {
    const index = this.messages.findIndex((message) => message.id === incoming.id);
    if (index >= 0) {
      const next = [...this.messages];
      next[index] = incoming;
      this.messages = next;
      this.scrollToLatestMessage();
      return;
    }

    this.messages = [...this.messages, incoming].sort((a, b) => {
      const left = new Date(a.createdAt).getTime();
      const right = new Date(b.createdAt).getTime();
      return left - right;
    });
    this.scrollToLatestMessage();
  }

  private scrollToLatestMessage(): void {
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        const container = this.messagesContainer?.nativeElement;
        if (!container) {
          return;
        }

        container.scrollTop = container.scrollHeight;
        this.bottomAnchor?.nativeElement.scrollIntoView({ block: 'end' });
      });
    });
  }

  private startFallbackPolling(): void {
    if (this.fallbackTimerId) {
      return;
    }

    this.fallbackTimerId = setInterval(() => {
      if (this.websocketConnected) {
        return;
      }

      if (this.isOfficer) {
        this.loadOfficerConversations(false);
      }
      if (this.activeCustomerUsername) {
        this.loadActiveConversation();
      }
    }, 4000);
  }

  private stopFallbackPolling(): void {
    if (this.fallbackTimerId) {
      clearInterval(this.fallbackTimerId);
      this.fallbackTimerId = undefined;
    }
  }
}
