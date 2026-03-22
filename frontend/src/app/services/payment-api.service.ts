import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SessionService } from './session.service';

export type PaymentMode = 'DEBIT' | 'CREDIT';

export interface PaymentBillResponse {
  bookingId: number;
  customerId: string;
  amount: number;
  paymentStatus: string;
  bookingStatus: string;
  createdAt: string;
}

export interface PaymentRequest {
  bookingId: number;
  amount: number;
  paymentMode: PaymentMode;
  cardNumber: string;
  cardHolderName: string;
  expiry: string;
  cvv: string;
}

export interface PaymentResponse {
  bookingId: number;
  invoiceId?: number;
  invoiceNumber?: string;
  trackingNumber?: string;
  customerId: string;
  amount: number;
  paymentMode: PaymentMode;
  status: 'SUCCESS' | 'FAILED';
  transactionRef: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class PaymentApiService {
  private readonly baseUrl = 'http://localhost:8080/api/payments';

  constructor(private readonly http: HttpClient, private readonly sessionService: SessionService) {}

  private buildHeaders(): Record<string, string> {
    const session = this.sessionService.get();
    if (!session) {
      return {};
    }

    return {
      'X-User-Role': session.role,
      'X-Username': session.username
    };
  }

  bill(bookingId: number): Observable<PaymentBillResponse> {
    return this.http.get<PaymentBillResponse>(`${this.baseUrl}/bill/${bookingId}`, { headers: this.buildHeaders() });
  }

  pay(payload: PaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(this.baseUrl, payload, { headers: this.buildHeaders() });
  }
}
