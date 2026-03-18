import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionData, SessionService } from '../../services/session.service';
import { TrackingApiService, TrackingResponse } from '../../services/tracking-api.service';

@Component({
  selector: 'app-tracking',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DatePipe, CurrencyPipe],
  templateUrl: './tracking.component.html',
  styleUrl: './tracking.component.css'
})
export class TrackingComponent implements OnInit {
  session: SessionData | null = null;

  customerForm!: FormGroup;
  officerForm!: FormGroup;

  loadingCustomer = false;
  loadingOfficer = false;

  customerError = '';
  officerError = '';

  customerResult: TrackingResponse | null = null;
  officerResults: TrackingResponse[] = [];

  constructor(
    private readonly fb: FormBuilder,
    private readonly trackingApi: TrackingApiService,
    private readonly sessionService: SessionService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.session = this.sessionService.get();
    if (!this.session) {
      this.router.navigate(['/login']);
      return;
    }

    const prefillBookingId = this.route.snapshot.queryParamMap.get('bookingId') || '';

    this.customerForm = this.fb.group({
      bookingId: [prefillBookingId, [Validators.required, Validators.pattern(/^\d{12}$/)]]
    });

    this.officerForm = this.fb.group({
      customerId: [''],
      bookingId: ['']
    });

    if (this.session.role === 'OFFICER') {
      this.searchOfficer();
    } else if (prefillBookingId) {
      this.searchCustomer();
    }
  }

  searchCustomer(): void {
    if (!this.session || this.session.role !== 'CUSTOMER') {
      return;
    }
    if (this.customerForm.invalid) {
      this.customerForm.markAllAsTouched();
      this.customerError = 'Enter a valid 12-digit Booking ID.';
      return;
    }

    this.loadingCustomer = true;
    this.customerError = '';
    this.customerResult = null;

    const bookingId = String(this.customerForm.value.bookingId).trim();
    this.trackingApi.trackCustomer(bookingId, this.session.username).subscribe({
      next: (result) => {
        this.customerResult = result;
        this.loadingCustomer = false;
      },
      error: (err) => {
        this.loadingCustomer = false;
        this.customerError = err?.error?.error || 'Tracking details not found.';
      }
    });
  }

  searchOfficer(): void {
    if (!this.session || this.session.role !== 'OFFICER') {
      return;
    }

    this.loadingOfficer = true;
    this.officerError = '';

    const customerId = String(this.officerForm.value.customerId || '').trim();
    const bookingId = String(this.officerForm.value.bookingId || '').trim();

    this.trackingApi.officerShipments(customerId || undefined, bookingId || undefined).subscribe({
      next: (results) => {
        this.officerResults = results;
        this.loadingOfficer = false;
      },
      error: (err) => {
        this.loadingOfficer = false;
        this.officerError = err?.error?.error || 'Unable to fetch shipped packages.';
      }
    });
  }

  clearOfficerFilters(): void {
    this.officerForm.reset({ customerId: '', bookingId: '' });
    this.searchOfficer();
  }
}
