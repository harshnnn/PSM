import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface InvoiceResponse {
  id: number;
  invoiceNumber: string;
  bookingId: number;
  customerId: string;
  receiverName: string;
  receiverAddress: string;
  receiverPin: string;
  receiverMobile: string;
  parcelWeightGrams: number;
  contentsDescription: string;
  parcelDeliveryType: string;
  parcelPackingPreference: string;
  parcelPickupTime: string;
  parcelDropoffTime?: string | null;
  parcelServiceCost: number;
  paymentTime: string;
  paymentMode: string;
  transactionRef: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class InvoiceApiService {
  private readonly baseUrl = 'http://localhost:8080/api/invoices';

  constructor(private readonly http: HttpClient) {}

  get(id: number): Observable<InvoiceResponse> {
    return this.http.get<InvoiceResponse>(`${this.baseUrl}/${id}`);
  }

  list(customerId?: string): Observable<InvoiceResponse[]> {
    if (customerId) {
      return this.http.get<InvoiceResponse[]>(this.baseUrl, { params: { customerId } });
    }
    return this.http.get<InvoiceResponse[]>(this.baseUrl);
  }
}
