import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { SessionData, SessionService } from '../../services/session.service';
import { TrackingApiService, TrackingResponse } from '../../services/tracking-api.service';

@Component({
  selector: 'app-pickup-scheduling',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DatePipe],
  templateUrl: './pickup-scheduling.component.html',
  styleUrl: './pickup-scheduling.component.css'
})
export class PickupSchedulingComponent implements OnInit {
  session: SessionData | null = null;

  searchForm!: FormGroup;
  scheduleForm!: FormGroup;

  loadingSearch = false;
  loadingSave = false;

  errorMessage = '';
  successMessage = '';
  minPickupDateTime = '';

  result: TrackingResponse | null = null;

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
      this.errorMessage = 'Only officers can access pickup scheduling.';
      return;
    }

    this.searchForm = this.fb.group({
      bookingId: ['', [Validators.required, Validators.pattern(/^\d+$/)]]
    });

    this.scheduleForm = this.fb.group({
      pickupDateTime: ['', Validators.required]
    });

    this.minPickupDateTime = this.getCurrentDateTimeLocal();
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
    this.result = null;

    const bookingId = String(this.searchForm.value.bookingId).trim();
    this.trackingApi.officerLookup(bookingId).subscribe({
      next: (res) => {
        this.result = res;
        this.scheduleForm.patchValue({
          pickupDateTime: this.toDateTimeLocalValue(res.pickupScheduledAt)
        });
        this.loadingSearch = false;
      },
      error: (err) => {
        this.loadingSearch = false;
        this.errorMessage = err?.error?.error || 'Booking not found.';
      }
    });
  }

  saveSchedule(): void {
    if (!this.result) {
      this.errorMessage = 'Search and select a booking first.';
      return;
    }
    if (this.scheduleForm.invalid) {
      this.scheduleForm.markAllAsTouched();
      this.errorMessage = 'Pickup date and time are required.';
      return;
    }

    const raw = String(this.scheduleForm.value.pickupDateTime || '').trim();
    const normalized = raw.length === 16 ? `${raw}:00` : raw;
    const selected = new Date(normalized);

    if (Number.isNaN(selected.getTime())) {
      this.errorMessage = 'Pickup date and time format is invalid.';
      return;
    }

    if (selected.getTime() < Date.now()) {
      this.errorMessage = 'Pickup date and time cannot be in the past.';
      return;
    }

    this.loadingSave = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.trackingApi.schedulePickup(this.result.bookingId, normalized).subscribe({
      next: (res) => {
        this.result = res;
        this.loadingSave = false;
        this.successMessage = 'Pickup schedule saved successfully.';
      },
      error: (err) => {
        this.loadingSave = false;
        this.errorMessage = err?.error?.error || 'Unable to save pickup schedule.';
      }
    });
  }

  private getCurrentDateTimeLocal(): string {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    return now.toISOString().slice(0, 16);
  }

  private toDateTimeLocalValue(value?: string | null): string {
    if (!value) {
      return '';
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return '';
    }

    date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    return date.toISOString().slice(0, 16);
  }
}
