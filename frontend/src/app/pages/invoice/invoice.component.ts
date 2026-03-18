import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { InvoiceApiService, InvoiceResponse } from '../../services/invoice-api.service';
import { SessionService } from '../../services/session.service';

@Component({
  selector: 'app-invoice',
  standalone: true,
  imports: [CommonModule, DatePipe, CurrencyPipe],
  templateUrl: './invoice.component.html',
  styleUrl: './invoice.component.css'
})
export class InvoiceComponent implements OnInit {
  invoice: InvoiceResponse | null = null;
  loading = false;
  errorMessage = '';
  trackingNumber = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly invoiceApi: InvoiceApiService,
    private readonly sessionService: SessionService
  ) {}

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    const invoiceId = Number(params.get('invoiceId')) || null;
    this.trackingNumber = params.get('trackingNumber') || '';
    if (!invoiceId) {
      this.errorMessage = 'No invoice specified.';
      return;
    }

    this.loading = true;
    const session = this.sessionService.get();
    const customerId = session?.role === 'CUSTOMER' ? session.username : undefined;
    this.invoiceApi.get(invoiceId).subscribe({
      next: (inv) => {
        if (customerId && inv.customerId !== customerId) {
          this.errorMessage = 'You are not authorized to view this invoice.';
          this.loading = false;
          return;
        }
        this.invoice = inv;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = typeof err?.error === 'string' ? err.error : 'Unable to load invoice.';
      }
    });
  }

  goHome(): void {
    this.router.navigate(['/home']);
  }

  viewHistory(): void {
    this.router.navigate(['/previous-booking']);
  }

  print(): void {
    window.print();
  }

  openTracking(): void {
    this.router.navigate(['/tracking'], {
      queryParams: this.trackingNumber ? { bookingId: this.trackingNumber } : undefined
    });
  }
}
