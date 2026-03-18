import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TrackingResponse {
  bookingId: string;
  originalBookingId: number;
  customerId: string;
  receiverName: string;
  amount: number;
  trackingStatus: string;
  shippedAt: string;
  pickupScheduledAt?: string | null;
  lastUpdatedAt: string;
}

@Injectable({ providedIn: 'root' })
export class TrackingApiService {
  private readonly baseUrl = 'http://localhost:8080/api/tracking';

  constructor(private readonly http: HttpClient) {}

  trackCustomer(bookingId: string, customerId: string): Observable<TrackingResponse> {
    return this.http.get<TrackingResponse>(`${this.baseUrl}/${bookingId}`, {
      params: { customerId }
    });
  }

  officerShipments(customerId?: string, bookingId?: string): Observable<TrackingResponse[]> {
    const params: Record<string, string> = {};
    if (customerId) {
      params['customerId'] = customerId;
    }
    if (bookingId) {
      params['bookingId'] = bookingId;
    }
    return this.http.get<TrackingResponse[]>(`${this.baseUrl}/officer/shipments`, { params });
  }

  officerLookup(bookingId: string): Observable<TrackingResponse> {
    return this.http.get<TrackingResponse>(`${this.baseUrl}/officer/booking/${bookingId}`);
  }

  schedulePickup(bookingId: string, pickupDateTime: string): Observable<TrackingResponse> {
    return this.http.put<TrackingResponse>(`${this.baseUrl}/officer/booking/${bookingId}/pickup`, {
      pickupDateTime
    });
  }

  updateDeliveryStatus(bookingId: string, status: string): Observable<TrackingResponse> {
    return this.http.put<TrackingResponse>(`${this.baseUrl}/officer/booking/${bookingId}/status`, {
      status
    });
  }
}
