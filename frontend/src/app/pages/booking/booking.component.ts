import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BookingApiService, BookingRequest, DeliverySpeed, PackagingPreference, ParcelSize, PaymentMethod } from '../../services/booking-api.service';
import { AuthApiService, ProfileResponse } from '../../services/auth-api.service';
import { SessionData, SessionService } from '../../services/session.service';
import { Subscription } from 'rxjs';
import { markAndFocusFirstInvalidControl } from '../../utils/form-validation';

function pickupDateTimeValidator(control: AbstractControl): ValidationErrors | null {
  const raw = control.value;
  if (!raw) {
    return null;
  }

  const parsed = new Date(raw);
  if (Number.isNaN(parsed.getTime())) {
    return { invalidDateTime: true };
  }

  const minAllowed = Date.now() + 60 * 60 * 1000;
  if (parsed.getTime() < minAllowed) {
    return { tooSoon: true };
  }

  return null;
}

function contactsMustDifferValidator(control: AbstractControl): ValidationErrors | null {
  const senderRaw = control.get('senderContact')?.value;
  const receiverRaw = control.get('receiverContact')?.value;

  if (!senderRaw || !receiverRaw) {
    return null;
  }

  const sender = String(senderRaw).replace(/\D/g, '');
  const receiver = String(receiverRaw).replace(/\D/g, '');
  return sender.length > 0 && sender === receiver ? { contactsSame: true } : null;
}

function parcelWeightLimitValidator(control: AbstractControl): ValidationErrors | null {
  const parcelSize = control.get('parcelSize')?.value as ParcelSize | null;
  const weightRaw = control.get('weightKg')?.value;
  const weight = Number(weightRaw);

  if (!parcelSize || Number.isNaN(weight) || weight <= 0) {
    return null;
  }

  const maxBySize: Record<ParcelSize, number> = {
    SMALL: 5,
    MEDIUM: 20,
    LARGE: 50,
    CUSTOM: 999
  };

  const maxAllowed = maxBySize[parcelSize];
  return weight > maxAllowed
    ? { parcelWeightLimit: { parcelSize, maxAllowed } }
    : null;
}

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
  private readonly personNamePattern = /^[A-Za-z]+(?:\s[A-Za-z]+)*$/;
  private readonly defaultInsuranceSelected = false;
  private readonly defaultTrackingEnabled = true;
  private readonly sizeDefaultWeight: Record<ParcelSize, number | null> = {
    SMALL: 1,
    MEDIUM: 10,
    LARGE: 30,
    CUSTOM: null
  };

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
      senderName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50), Validators.pattern(this.personNamePattern)]],
      senderAddress: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
      senderContact: ['', [Validators.required, Validators.pattern(/^(?:\+91\d{10}|\d{10})$/)]],
      receiverName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50), Validators.pattern(this.personNamePattern)]],
      receiverAddress: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
      receiverPinCode: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
      receiverContact: ['', [Validators.required, Validators.pattern(/^(?:\+91\d{10}|\d{10})$/)]],
      parcelSize: ['SMALL' as ParcelSize, [Validators.required]],
      weightKg: [1, [Validators.required, Validators.min(0.1), Validators.max(999), Validators.pattern(/^\d{1,5}(\.\d{1,2})?$/)]],
      contentsDescription: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(255)]],
      deliverySpeed: ['STANDARD' as DeliverySpeed, [Validators.required]],
      packagingPreference: ['STANDARD' as PackagingPreference, [Validators.required]],
      preferredPickup: [this.defaultPickup(), [Validators.required, pickupDateTimeValidator]],
      serviceCost: [0, [Validators.required, Validators.min(0)]],
      paymentMethod: ['CASH' as PaymentMethod, [Validators.required]],
    }, { validators: [contactsMustDifferValidator, parcelWeightLimitValidator] });

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
      markAndFocusFirstInvalidControl(this.form);
      this.errorMessage = 'Fix the highlighted fields before submitting.';
      return;
    }

    const formValue = this.form.getRawValue();

    const payload = {
      ...(formValue as BookingRequest),
      senderName: this.normalizeText(formValue.senderName),
      senderAddress: this.normalizeText(formValue.senderAddress),
      receiverName: this.normalizeText(formValue.receiverName),
      receiverAddress: this.normalizeText(formValue.receiverAddress),
      contentsDescription: this.normalizeText(formValue.contentsDescription),
      insuranceSelected: this.defaultInsuranceSelected,
      trackingEnabled: this.defaultTrackingEnabled,
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

  normalizeContactInput(controlName: 'senderContact' | 'receiverContact'): void {
    const control = this.form.get(controlName);
    if (!control) {
      return;
    }

    const normalized = this.normalizePhone(control.value);
    if (normalized !== control.value) {
      control.setValue(normalized);
    }
  }

  showContactsSameError(): boolean {
    const senderControl = this.form.get('senderContact');
    const receiverControl = this.form.get('receiverContact');
    if (!senderControl || !receiverControl) {
      return false;
    }

    const eitherTouched = senderControl.touched || receiverControl.touched;
    return eitherTouched
      && senderControl.valid
      && receiverControl.valid
      && !!this.form.errors?.['contactsSame'];
  }

  onParcelSizeChange(): void {
    const sizeControl = this.form.get('parcelSize');
    const weightControl = this.form.get('weightKg');
    if (!sizeControl || !weightControl) {
      return;
    }

    const selectedSize = sizeControl.value as ParcelSize;
    const defaultWeight = this.sizeDefaultWeight[selectedSize];
    weightControl.setValue(defaultWeight);
    weightControl.markAsTouched();
  }

  onWeightChange(): void {
    const sizeControl = this.form.get('parcelSize');
    const weightControl = this.form.get('weightKg');
    if (!sizeControl || !weightControl) {
      return;
    }

    const weight = Number(weightControl.value);
    if (Number.isNaN(weight) || weight <= 0) {
      return;
    }

    const inferredSize = this.inferParcelSizeByWeight(weight);
    if (inferredSize !== sizeControl.value) {
      sizeControl.setValue(inferredSize);
    }
  }

  showParcelWeightLimitError(): boolean {
    const sizeControl = this.form.get('parcelSize');
    const weightControl = this.form.get('weightKg');
    if (!sizeControl || !weightControl) {
      return false;
    }

    return (sizeControl.touched || weightControl.touched) && !!this.form.errors?.['parcelWeightLimit'];
  }

  parcelWeightLimitMessage(): string {
    const error = this.form.errors?.['parcelWeightLimit'] as { parcelSize?: string; maxAllowed?: number } | undefined;
    if (!error?.parcelSize || error.maxAllowed == null) {
      return 'Weight exceeds the limit for selected parcel size.';
    }

    return `${error.parcelSize} parcels can weigh up to ${error.maxAllowed} kg only.`;
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

    if (this.defaultInsuranceSelected) {
      cost += 25;
    }

    if (this.defaultTrackingEnabled) {
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

  private normalizeText(value: unknown): string {
    return String(value ?? '').trim().replace(/\s+/g, ' ');
  }

  private normalizePhone(value: unknown): string {
    const raw = String(value ?? '').trim();
    if (!raw) {
      return '';
    }

    const hasPlus = raw.startsWith('+');
    const digits = raw.replace(/\D/g, '');
    return hasPlus ? `+${digits}` : digits;
  }

  private inferParcelSizeByWeight(weight: number): ParcelSize {
    if (weight <= 5) {
      return 'SMALL';
    }
    if (weight <= 20) {
      return 'MEDIUM';
    }
    if (weight <= 50) {
      return 'LARGE';
    }
    return 'CUSTOM';
  }
}
