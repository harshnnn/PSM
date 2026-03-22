import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SessionService } from './session.service';

export type ParcelSize = 'SMALL' | 'MEDIUM' | 'LARGE' | 'CUSTOM';
export type DeliverySpeed = 'STANDARD' | 'EXPRESS' | 'SAME_DAY';
export type PackagingPreference = 'STANDARD' | 'CUSTOM' | 'ECO_FRIENDLY' | 'FRAGILE';
export type PaymentMethod = 'CASH' | 'CARD' | 'UPI' | 'WALLET';
export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED';
export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED';

export interface BookingRequest {
  customerId: string;
  senderName: string;
  senderAddress: string;
  senderContact: string;
  receiverName: string;
  receiverAddress: string;
  receiverPinCode: string;
  receiverContact: string;
  parcelSize: ParcelSize;
  weightKg: number;
  contentsDescription: string;
  deliverySpeed: DeliverySpeed;
  packagingPreference: PackagingPreference;
  preferredPickup: string;
  serviceCost: number;
  paymentMethod: PaymentMethod;
  insuranceSelected: boolean;
  trackingEnabled: boolean;
}

export interface BookingResponse {
  id: number;
  customerId?: string;
  senderName: string;
  receiverName: string;
  receiverPinCode: string;
  parcelSize: ParcelSize;
  weightKg: number;
  deliverySpeed: DeliverySpeed;
  packagingPreference: PackagingPreference;
  paymentStatus: PaymentStatus;
  bookingStatus: BookingStatus;
  serviceCost: number;
  preferredPickup: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class BookingApiService {
  // Routed via API Gateway (adjust path if your gateway mapping differs)
  private readonly baseUrl = 'http://localhost:8080/api/bookings';

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

  create(payload: BookingRequest): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(this.baseUrl, payload, { headers: this.buildHeaders() });
  }

  getAll(): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(this.baseUrl, { headers: this.buildHeaders() });
  }

  get(id: number): Observable<BookingResponse> {
    return this.http.get<BookingResponse>(`${this.baseUrl}/${id}`, { headers: this.buildHeaders() });
  }

  getUnpaid(customerId: string): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.baseUrl}/unpaid`, {
      params: { customerId },
      headers: this.buildHeaders()
    });
  }
}
