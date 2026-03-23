import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SupportMessage {
  id: number;
  customerUsername: string;
  senderRole: 'CUSTOMER' | 'OFFICER' | string;
  senderUsername: string;
  message: string;
  createdAt: string;
  deliveredAt?: string | null;
  readAt?: string | null;
  readByUsername?: string | null;
}

export interface SupportConversationSummary {
  customerUsername: string;
  lastMessage: string;
  lastSenderRole: string;
  lastUpdatedAt: string;
  messageCount: number;
}

export interface SendSupportMessageRequest {
  customerUsername?: string;
  message: string;
}

export interface SupportReadReceipt {
  customerUsername: string;
  readByUsername: string;
  readByRole: string;
  readAt: string;
  messageIds: number[];
}

@Injectable({ providedIn: 'root' })
export class SupportApiService {
  private readonly baseUrl = 'http://localhost:8080/api/support';

  constructor(private readonly http: HttpClient) {}

  listConversations(): Observable<SupportConversationSummary[]> {
    return this.http.get<SupportConversationSummary[]>(`${this.baseUrl}/conversations`);
  }

  getConversationMessages(customerUsername: string): Observable<SupportMessage[]> {
    return this.http.get<SupportMessage[]>(`${this.baseUrl}/conversations/${encodeURIComponent(customerUsername)}/messages`);
  }

  sendMessage(payload: SendSupportMessageRequest): Observable<SupportMessage> {
    return this.http.post<SupportMessage>(`${this.baseUrl}/messages`, payload);
  }

  markConversationRead(customerUsername: string): Observable<SupportReadReceipt> {
    return this.http.post<SupportReadReceipt>(
      `${this.baseUrl}/conversations/${encodeURIComponent(customerUsername)}/read`,
      {}
    );
  }
}
