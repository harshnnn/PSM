import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BookingHistoryEntry {
  id: number;
  customerId: string;
  bookingId: string;
  bookingDate: string;
  receiverName: string;
  deliveredAddress: string;
  amount: number;
  status: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

@Injectable({ providedIn: 'root' })
export class BookingHistoryApiService {
  private readonly baseUrl = 'http://localhost:8080/api/history';

  constructor(private readonly http: HttpClient) {}

  getCustomerHistory(customerId: string, page = 0, size = 10): Observable<PageResponse<BookingHistoryEntry>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<BookingHistoryEntry>>(`${this.baseUrl}/customer/${customerId}`, { params });
  }

  getOfficerHistory(
    customerId: string | null,
    startDate: string | null,
    endDate: string | null,
    page = 0,
    size = 10
  ): Observable<PageResponse<BookingHistoryEntry>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (customerId) params = params.set('customerId', customerId);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<PageResponse<BookingHistoryEntry>>(`${this.baseUrl}/officer`, { params });
  }
}
