import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { PaymentApiService, PaymentBillResponse, PaymentMode } from '../../services/payment-api.service';
import { SessionData, SessionService } from '../../services/session.service';

@Component({
  selector: 'app-pay-bill',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './pay-bill.component.html',
  styleUrl: './pay-bill.component.css'
})
export class PayBillComponent implements OnInit, OnDestroy {
  session: SessionData | null = null;
  billForm!: FormGroup;
  cardForm!: FormGroup;
  bill: PaymentBillResponse | null = null;
  loadingBill = false;
  loadingPayment = false;
  showCardForm = false;
  successMessage = '';
  errorMessage = '';

  private billSub?: Subscription;
  private paySub?: Subscription;

  constructor(
    private readonly fb: FormBuilder,
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

    this.billForm = this.fb.group({
      bookingId: [null, [Validators.required, Validators.min(1)]],
      paymentMode: ['DEBIT' as PaymentMode, Validators.required]
    });

    this.cardForm = this.fb.group({
      cardNumber: ['', [Validators.required, Validators.pattern(/^\d{16}$/)]],
      cardHolderName: ['', [Validators.required, Validators.maxLength(60)]],
      expiry: ['', [Validators.required, Validators.pattern(/^(0[1-9]|1[0-2])\/\d{2}$/)]],
      cvv: ['', [Validators.required, Validators.pattern(/^\d{3}$/)]]
    });

    const bookingIdParam = Number(this.route.snapshot.queryParamMap.get('bookingId'));
    if (bookingIdParam) {
      this.billForm.patchValue({ bookingId: bookingIdParam });
      this.loadBill();
    }
  }

  ngOnDestroy(): void {
    this.billSub?.unsubscribe();
    this.paySub?.unsubscribe();
  }

  loadBill(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.showCardForm = false;
    if (this.billForm.invalid) {
      this.billForm.markAllAsTouched();
      this.errorMessage = 'Enter a valid booking ID to fetch the bill.';
      return;
    }

    const bookingId = this.billForm.value.bookingId;
    this.loadingBill = true;
    this.billSub?.unsubscribe();
    this.billSub = this.paymentApi.bill(bookingId).subscribe({
      next: (bill) => {
        this.bill = bill;
        this.loadingBill = false;
      },
      error: (err) => {
        this.bill = null;
        this.loadingBill = false;
        this.errorMessage = typeof err?.error === 'string' ? err.error : 'Unable to fetch bill. Check the booking ID.';
      }
    });
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

    const payload = {
      bookingId: this.bill.bookingId,
      amount: this.bill.amount,
      paymentMode: this.billForm.value.paymentMode as PaymentMode,
      ...(this.cardForm.getRawValue())
    };

    this.loadingPayment = true;
    this.paySub?.unsubscribe();
    this.paySub = this.paymentApi.pay(payload).subscribe({
      next: (res) => {
        this.loadingPayment = false;
        this.successMessage = `${res.message} (Booking #${res.bookingId}, Txn ${res.transactionRef})`;
        this.errorMessage = '';
        this.showCardForm = false;
        this.router.navigate(['/invoice'], {
          queryParams: {
            bookingId: res.bookingId,
            amount: res.amount,
            transactionRef: res.transactionRef
          }
        });
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

  logout(): void {
    this.sessionService.clear();
    this.router.navigate(['/login']);
  }
}
