import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { BookingHistoryApiService, BookingHistoryEntry, PageResponse } from '../../services/booking-history-api.service';
import { SessionData, SessionService } from '../../services/session.service';

@Component({
  selector: 'app-booking-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking-history.component.html',
  styleUrl: './booking-history.component.css'
})
export class BookingHistoryComponent implements OnInit, OnDestroy {
  session: SessionData | null = null;
  isCustomer = false;
  entries: BookingHistoryEntry[] = [];
  page = 0;
  size = 5;
  totalPages = 0;
  totalElements = 0;
  last = false;
  loading = false;
  errorMessage = '';

  // Officer filters
  searchCustomerId = '';
  startDate: string | null = null;
  endDate: string | null = null;

  private sub?: Subscription;

  constructor(
    private readonly historyApi: BookingHistoryApiService,
    private readonly sessionService: SessionService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.session = this.sessionService.get();
    if (!this.session) {
      this.router.navigate(['/login']);
      return;
    }
    this.isCustomer = this.session.role === 'CUSTOMER';

    if (this.isCustomer) {
      this.searchCustomerId = this.session.username;
    }

    this.loadPage(0);
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  logout(): void {
    this.sessionService.clear();
    this.router.navigate(['/login']);
  }

  next(): void {
    if (!this.last) {
      this.loadPage(this.page + 1);
    }
  }

  prev(): void {
    if (this.page > 0) {
      this.loadPage(this.page - 1);
    }
  }

  search(): void {
    this.loadPage(0);
  }

  private loadPage(page: number): void {
    if (!this.session) return;
    this.loading = true;
    this.errorMessage = '';

    let obs;
    if (this.isCustomer) {
      obs = this.historyApi.getCustomerHistory(this.searchCustomerId, page, this.size);
    } else {
      const customerId = this.searchCustomerId.trim() || null;
      const start = this.startDate && this.startDate.trim() ? this.startDate : null;
      const end = this.endDate && this.endDate.trim() ? this.endDate : null;
      obs = this.historyApi.getOfficerHistory(customerId, start, end, page, this.size);
    }

    this.sub?.unsubscribe();
    this.sub = obs.subscribe({
      next: (res: PageResponse<BookingHistoryEntry>) => {
        this.entries = res.content;
        this.page = res.page;
        this.size = res.size;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.last = res.last;
        this.loading = false;
      },
      error: () => {
        this.entries = [];
        this.errorMessage = 'Unable to load booking history.';
        this.loading = false;
      }
    });
  }
}
