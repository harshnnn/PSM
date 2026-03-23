import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { BookingApiService, BookingResponse } from '../../services/booking-api.service';
import { PaymentApiService, PaymentBillResponse, PaymentMode } from '../../services/payment-api.service';
import { SessionData, SessionService } from '../../services/session.service';

@Component({
  selector: 'app-pay-bill',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './pay-bill.component.html',
  styleUrl: './pay-bill.component.css'
})
export class PayBillComponent implements OnInit, OnDestroy {
  session: SessionData | null = null;
  cardForm!: FormGroup;
  bill: PaymentBillResponse | null = null;
  unpaidBookings: BookingResponse[] = [];
  listLoading = false;
  listError = '';
  loadingPayment = false;
  showCardForm = false;
  successMessage = '';
  errorMessage = '';
  paymentMode: PaymentMode = 'DEBIT';
  private readonly cardHolderNamePattern = /^[A-Za-z]+(?:\s[A-Za-z]+)*$/;

  private paySub?: Subscription;

  constructor(
    private readonly fb: FormBuilder,
    private readonly bookingApi: BookingApiService,
    private readonly paymentApi: PaymentApiService,
    private readonly sessionService: SessionService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.session = this.sessionService.get();
    if (!this.session) {
      this.router.navigate(['/login']);
      return;
    }

    this.cardForm = this.fb.group({
      cardNumber: ['', [Validators.required, Validators.pattern(/^(?:\d{4}\s){3}\d{4}$/)]],
      cardHolderName: ['', [Validators.required, Validators.maxLength(60), Validators.pattern(this.cardHolderNamePattern)]],
      expiry: ['', [Validators.required, Validators.pattern(/^(0[1-9]|1[0-2])\/\d{2}$/)]],
      cvv: ['', [Validators.required, Validators.pattern(/^\d{3}$/)]]
    });

    const bookingIdParam = Number(this.route.snapshot.queryParamMap.get('bookingId')) || null;
    this.loadUnpaidBookings(bookingIdParam);
  }

  ngOnDestroy(): void {
    this.paySub?.unsubscribe();
  }

  private loadUnpaidBookings(preselectId: number | null): void {
    this.listLoading = true;
    this.listError = '';
    this.paymentMode = 'DEBIT';
    this.showCardForm = false;
    this.bill = null;

    this.bookingApi.getUnpaid(this.session?.username ?? '').subscribe({
      next: (bookings) => {
        this.unpaidBookings = bookings;
        this.listLoading = false;

        if (preselectId) {
          const match = bookings.find((b) => b.id === preselectId);
          if (match) {
            this.selectBooking(match);
          }
        }
      },
      error: (err) => {
        this.unpaidBookings = [];
        this.listLoading = false;
        this.listError = typeof err?.error === 'string' ? err.error : 'Unable to load unpaid bookings.';
      }
    });
  }

  selectBooking(booking: BookingResponse): void {
    this.bill = {
      bookingId: booking.id,
      customerId: booking.customerId ?? this.session?.username ?? 'customer',
      amount: booking.serviceCost,
      paymentStatus: booking.paymentStatus,
      bookingStatus: booking.bookingStatus,
      createdAt: booking.createdAt
    };
    this.paymentMode = 'DEBIT';
    this.showCardForm = false;
    this.successMessage = '';
    this.errorMessage = '';
  }

  proceedToCard(): void {
    if (!this.bill) {
      this.errorMessage = 'Load a bill first.';
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.showCardForm = true;
  }

  makePayment(): void {
    if (!this.bill) {
      this.errorMessage = 'Load a bill first.';
      return;
    }

    if (this.cardForm.invalid) {
      this.cardForm.markAllAsTouched();
      this.errorMessage = 'Fix the highlighted card details before paying.';
      return;
    }

    const formValue = this.cardForm.getRawValue();
    const payload = {
      bookingId: this.bill.bookingId,
      amount: this.bill.amount,
      paymentMode: this.paymentMode,
      ...formValue,
      cardNumber: this.toDigitsOnly(formValue.cardNumber)
    };

    this.loadingPayment = true;
    this.paySub?.unsubscribe();
    this.paySub = this.paymentApi.pay(payload).subscribe({
      next: (res) => {
        this.loadingPayment = false;
        this.successMessage = `${res.message} (Booking #${res.bookingId}, Txn ${res.transactionRef}${res.trackingNumber ? `, Track ID ${res.trackingNumber}` : ''})`;
        this.errorMessage = '';
        this.showCardForm = false;
        this.loadUnpaidBookings(null);
        if (res.invoiceId) {
          this.router.navigate(['/invoice'], {
            queryParams: {
              invoiceId: res.invoiceId,
              invoiceNumber: res.invoiceNumber ?? undefined,
              trackingNumber: res.trackingNumber ?? undefined
            }
          });
        } else {
          // Fallback if invoice was not created
          this.errorMessage = 'Payment succeeded but invoice was not generated. Please check invoice history.';
        }
      },
      error: (err) => {
        this.loadingPayment = false;
        this.successMessage = '';
        this.errorMessage = typeof err?.error === 'string' ? err.error : 'Payment failed. Please try again.';
      }
    });
  }

  goHome(): void {
    this.router.navigate(['/home']);
  }

  goInvoices(): void {
    this.router.navigate(['/invoices']);
  }

  onCardNumberInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = this.toDigitsOnly(input.value).slice(0, 16);
    const grouped = digits.replace(/(\d{4})(?=\d)/g, '$1 ').trim();
    this.cardForm.patchValue({ cardNumber: grouped }, { emitEvent: false });
    input.value = grouped;
  }

  handlePasswordClipboardEvent(event: ClipboardEvent, action: 'copy' | 'paste'): void {
    event.preventDefault();
    this.errorMessage = action === 'copy' ? "Can't copy password." : "Can't paste password.";
  }

  private toDigitsOnly(value: unknown): string {
    return String(value ?? '').replace(/\D/g, '');
  }
}
