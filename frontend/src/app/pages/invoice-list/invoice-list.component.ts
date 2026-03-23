import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { InvoiceApiService, InvoiceResponse } from '../../services/invoice-api.service';
import { SessionData, SessionService } from '../../services/session.service';

@Component({
  selector: 'app-invoice-list',
  standalone: true,
  imports: [CommonModule, DatePipe, CurrencyPipe],
  templateUrl: './invoice-list.component.html',
  styleUrl: './invoice-list.component.css'
})
export class InvoiceListComponent implements OnInit {
  invoices: InvoiceResponse[] = [];
  loading = false;
  errorMessage = '';
  session: SessionData | null = null;
  isCustomer = false;

  constructor(
    private readonly invoiceApi: InvoiceApiService,
    private readonly sessionService: SessionService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.session = this.sessionService.get();
    this.isCustomer = this.session?.role === 'CUSTOMER';
    this.loadInvoices();
  }

  loadInvoices(): void {
    this.loading = true;
    this.errorMessage = '';
    const customerId = this.isCustomer ? this.session?.username : undefined;
    this.invoiceApi.list(customerId).subscribe({
      next: (items) => {
        this.invoices = [...items].sort((a, b) => {
          const aTime = a.paymentTime ?? a.createdAt;
          const bTime = b.paymentTime ?? b.createdAt;
          return (bTime || '').localeCompare(aTime || '');
        });
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = typeof err?.error === 'string' ? err.error : 'Unable to load invoices.';
      }
    });
  }

  viewInvoice(inv: InvoiceResponse): void {
    const target = this.isCustomer ? '/invoice' : '/admin/invoice';
    this.router.navigate([target], { queryParams: { invoiceId: inv.id, invoiceNumber: inv.invoiceNumber } });
  }

  backToPayments(): void {
    this.router.navigate([this.isCustomer ? '/pay-bill' : '/admin']);
  }

  trackById(_index: number, item: InvoiceResponse): number {
    return item.id;
  }
}
