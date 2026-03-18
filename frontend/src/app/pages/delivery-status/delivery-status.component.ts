import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { SessionData, SessionService } from '../../services/session.service';
import { TrackingApiService, TrackingResponse } from '../../services/tracking-api.service';

@Component({
  selector: 'app-delivery-status',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DatePipe],
  templateUrl: './delivery-status.component.html',
  styleUrl: './delivery-status.component.css'
})
export class DeliveryStatusComponent implements OnInit {
  session: SessionData | null = null;

  searchForm!: FormGroup;
  updateForm!: FormGroup;

  loadingSearch = false;
  loadingSave = false;

  errorMessage = '';
  successMessage = '';

  result: TrackingResponse | null = null;

  readonly statuses = [
    { label: 'Picked up', value: 'PICKED_UP' },
    { label: 'In Transit', value: 'IN_TRANSIT' },
    { label: 'Delivered', value: 'DELIVERED' },
    { label: 'Returned', value: 'RETURNED' }
  ];

  constructor(
    private readonly fb: FormBuilder,
    private readonly trackingApi: TrackingApiService,
    private readonly sessionService: SessionService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.session = this.sessionService.get();
    if (!this.session) {
      this.router.navigate(['/login']);
      return;
    }
    if (this.session.role !== 'OFFICER') {
      this.errorMessage = 'Only officers can update delivery status.';
      return;
    }

    this.searchForm = this.fb.group({
      bookingId: ['', [Validators.required, Validators.pattern(/^\d+$/)]]
    });

    this.updateForm = this.fb.group({
      status: ['PICKED_UP', Validators.required]
    });
  }

  search(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      this.errorMessage = 'Enter a valid Booking ID.';
      return;
    }

    this.loadingSearch = true;
    this.errorMessage = '';
    this.successMessage = '';

    const bookingId = String(this.searchForm.value.bookingId).trim();
    this.trackingApi.officerLookup(bookingId).subscribe({
      next: (res) => {
        this.result = res;
        if (this.statuses.some((s) => s.value === res.trackingStatus)) {
          this.updateForm.patchValue({ status: res.trackingStatus });
        }
        this.loadingSearch = false;
      },
      error: (err) => {
        this.loadingSearch = false;
        this.result = null;
        this.errorMessage = err?.error?.error || 'Booking not found.';
      }
    });
  }

  saveStatus(): void {
    if (!this.result) {
      this.errorMessage = 'Search and select a booking first.';
      return;
    }

    if (this.updateForm.invalid) {
      this.updateForm.markAllAsTouched();
      this.errorMessage = 'Please select a status.';
      return;
    }

    this.loadingSave = true;
    this.errorMessage = '';
    this.successMessage = '';

    const status = String(this.updateForm.value.status);
    this.trackingApi.updateDeliveryStatus(this.result.bookingId, status).subscribe({
      next: (res) => {
        this.result = res;
        this.loadingSave = false;
        this.successMessage = 'Delivery status updated successfully.';
      },
      error: (err) => {
        this.loadingSave = false;
        this.errorMessage = err?.error?.error || 'Unable to update delivery status.';
      }
    });
  }
}
