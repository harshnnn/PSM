import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BookingApiService, BookingRequest, DeliverySpeed, PackagingPreference, ParcelSize, PaymentMethod } from '../../services/booking-api.service';
import { AuthApiService, ProfileResponse } from '../../services/auth-api.service';
import { SessionData, SessionService } from '../../services/session.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './booking.component.html',
  styleUrl: './booking.component.css'
})
export class BookingComponent implements OnInit, OnDestroy {
  readonly parcelSizes: ParcelSize[] = ['SMALL', 'MEDIUM', 'LARGE', 'CUSTOM'];
  readonly deliverySpeeds: DeliverySpeed[] = ['STANDARD', 'EXPRESS', 'SAME_DAY'];
  readonly packagingPreferences: PackagingPreference[] = ['STANDARD', 'CUSTOM', 'ECO_FRIENDLY', 'FRAGILE'];
  readonly paymentMethods: PaymentMethod[] = ['CASH', 'CARD', 'UPI', 'WALLET'];

  form!: FormGroup;
  session: SessionData | null = null;
  isCustomer = false;
  successMessage = '';
  errorMessage = '';
  lastBookingId: number | null = null;
  lastBookingAmount = 0;
  private valueChangeSub?: Subscription;
  private profileSub?: Subscription;

  constructor(
    private readonly fb: FormBuilder,
    private readonly bookingApi: BookingApiService,
    private readonly authApi: AuthApiService,
    private readonly sessionService: SessionService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.session = this.sessionService.get();

    this.form = this.fb.group({
      senderName: ['', [Validators.required, Validators.maxLength(50)]],
      senderAddress: ['', [Validators.required, Validators.maxLength(200)]],
      senderContact: ['', [Validators.required, Validators.pattern(/^\+?[0-9]{7,15}$/)]],
      receiverName: ['', [Validators.required, Validators.maxLength(50)]],
      receiverAddress: ['', [Validators.required, Validators.maxLength(200)]],
      receiverPinCode: ['', [Validators.required, Validators.pattern(/^\d{5,6}$/)]],
      receiverContact: ['', [Validators.required, Validators.pattern(/^\+?[0-9]{7,15}$/)]],
      parcelSize: ['SMALL' as ParcelSize, [Validators.required]],
      weightKg: [1, [Validators.required, Validators.min(0.1), Validators.max(999), Validators.pattern(/^\d{1,5}(\.\d{1,2})?$/)]],
      contentsDescription: ['', [Validators.required, Validators.maxLength(255)]],
      deliverySpeed: ['STANDARD' as DeliverySpeed, [Validators.required]],
      packagingPreference: ['STANDARD' as PackagingPreference, [Validators.required]],
      preferredPickup: [this.defaultPickup(), [Validators.required]],
      serviceCost: [0, [Validators.required, Validators.min(0)]],
      paymentMethod: ['CASH' as PaymentMethod, [Validators.required]],
      insuranceSelected: [false],
      trackingEnabled: [true]
    });

    if (!this.session) {
      this.router.navigate(['/login']);
      return;
    }

    this.isCustomer = this.session.role === 'CUSTOMER';
    this.prefillSender();
    this.updateCost();
    this.valueChangeSub = this.form.valueChanges.subscribe(() => this.updateCost());
  }

  ngOnDestroy(): void {
    this.valueChangeSub?.unsubscribe();
    this.profileSub?.unsubscribe();
  }

  submit(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage = 'Fix the highlighted fields before submitting.';
      return;
    }

    const payload = {
      ...(this.form.getRawValue() as BookingRequest),
      customerId: this.session?.username ?? 'anonymous'
    } satisfies BookingRequest;
    this.bookingApi.create(payload).subscribe({
      next: (res) => {
        this.successMessage = `Booking created with ID ${res.id}. Payment status: ${res.paymentStatus}.`;
        this.lastBookingId = res.id;
        this.lastBookingAmount = res.serviceCost;
        this.form.reset({
          senderName: this.form.get('senderName')?.value,
          senderAddress: this.form.get('senderAddress')?.value,
          senderContact: this.form.get('senderContact')?.value,
          parcelSize: 'SMALL',
          deliverySpeed: 'STANDARD',
          packagingPreference: 'STANDARD',
          preferredPickup: this.defaultPickup(),
          serviceCost: this.form.get('serviceCost')?.value ?? 0,
          paymentMethod: 'CASH',
          insuranceSelected: false,
          trackingEnabled: true
        });
      },
      error: (error) => {
        const payloadError = error?.error;
        if (typeof payloadError === 'string') {
          this.errorMessage = payloadError;
          this.lastBookingId = null;
          return;
        }
        if (payloadError && typeof payloadError === 'object') {
          const first = Object.values(payloadError)[0];
          this.errorMessage = typeof first === 'string' ? first : 'Booking failed. Please try again.';
          this.lastBookingId = null;
          return;
        }
        this.errorMessage = 'Booking failed. Please try again.';
        this.lastBookingId = null;
      }
    });
  }

  reset(): void {
    this.successMessage = '';
    this.errorMessage = '';
    const senderName = this.form.get('senderName')?.value;
    const senderAddress = this.form.get('senderAddress')?.value;
    const senderContact = this.form.get('senderContact')?.value;
    this.form.reset({
      senderName,
      senderAddress,
      senderContact,
      parcelSize: 'SMALL',
      weightKg: 1,
      contentsDescription: '',
      deliverySpeed: 'STANDARD',
      packagingPreference: 'STANDARD',
      preferredPickup: this.defaultPickup(),
      serviceCost: 0,
      paymentMethod: 'CASH',
      insuranceSelected: false,
      trackingEnabled: true
    });
  }

  goHome(): void {
    this.router.navigate(['/home']);
  }

  goToPayBill(): void {
    if (!this.lastBookingId) return;
    this.router.navigate(['/pay-bill'], {
      queryParams: {
        bookingId: this.lastBookingId
      }
    });
  }

  get minPickup(): string {
    const now = new Date();
    now.setMinutes(now.getMinutes() + 60);
    return this.toDateTimeLocal(now);
  }

  private prefillSender(): void {
    if (!this.session) return;

    const stored = sessionStorage.getItem('registration-profile');
    const profile = stored
      ? (JSON.parse(stored) as { customerName: string; address: string; countryCode: string; mobileNumber: string })
      : null;

    const applyProfile = (data: { customerName: string; address: string; countryCode: string; mobileNumber: string }) => {
      this.form.patchValue({
        senderName: data.customerName,
        senderAddress: data.address,
        senderContact: `${data.countryCode}${data.mobileNumber}`
      });
    };

    if (profile) {
      applyProfile(profile);
    } else {
      this.form.patchValue({
        senderName: this.session.username,
        senderAddress: '',
        senderContact: ''
      });
    }

    if (this.session.role !== 'CUSTOMER') {
      return;
    }

    this.profileSub = this.authApi.profile(this.session.username).subscribe({
      next: (res: ProfileResponse) => {
        sessionStorage.setItem(
          'registration-profile',
          JSON.stringify({
            customerName: res.customerName,
            address: res.address,
            countryCode: res.countryCode,
            mobileNumber: res.mobileNumber
          })
        );
        applyProfile(res);
      },
      error: () => {
        // Keep existing fallback values when profile fetch fails.
      }
    });
  }

  private updateCost(): void {
    const value = this.form.getRawValue();
    let cost = 50;

    switch (value.parcelSize) {
      case 'MEDIUM':
        cost += 30;
        break;
      case 'LARGE':
        cost += 70;
        break;
      case 'CUSTOM':
        cost += 90;
        break;
      default:
        break;
    }

    const weight = Number(value.weightKg) || 0;
    cost += Math.max(weight * 10, 5);

    if (value.deliverySpeed === 'EXPRESS') {
      cost += 40;
    } else if (value.deliverySpeed === 'SAME_DAY') {
      cost += 80;
    }

    if (value.packagingPreference === 'CUSTOM') {
      cost += 20;
    } else if (value.packagingPreference === 'ECO_FRIENDLY') {
      cost += 15;
    } else if (value.packagingPreference === 'FRAGILE') {
      cost += 25;
    }

    if (value.insuranceSelected) {
      cost += 25;
    }

    if (value.trackingEnabled) {
      cost += 5;
    }

    this.form.patchValue({ serviceCost: Math.round(cost * 100) / 100 }, { emitEvent: false });
  }

  private defaultPickup(): string {
    const date = new Date();
    date.setHours(date.getHours() + 2);
    return this.toDateTimeLocal(date);
  }

  private toDateTimeLocal(date: Date): string {
    const pad = (n: number) => `${n}`.padStart(2, '0');
    const yyyy = date.getFullYear();
    const mm = pad(date.getMonth() + 1);
    const dd = pad(date.getDate());
    const hh = pad(date.getHours());
    const min = pad(date.getMinutes());
    return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
  }
}
