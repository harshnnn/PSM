import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService, RegisterResponse } from '../../services/auth-api.service';
import { SessionService } from '../../services/session.service';
import { markAndFocusFirstInvalidControl } from '../../utils/form-validation';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;

  if (!password || !confirmPassword) {
    return null;
  }

  return password === confirmPassword ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  errorMessage = '';
  readonly form;
  private readonly personNamePattern = /^[A-Za-z]+(?:\s[A-Za-z]+)*$/;
  private readonly userIdPattern = /^[A-Za-z0-9._-]{5,20}$/;

  readonly countryCodes = ['+1', '+44', '+61', '+91'];

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authApi: AuthApiService,
    private readonly sessionService: SessionService,
    private readonly router: Router
  ) {
    this.form = this.formBuilder.group({
      customerName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50), Validators.pattern(this.personNamePattern)]],
      email: ['', [Validators.required, Validators.email]],
      countryCode: ['+91', [Validators.required]],
      mobileNumber: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      address: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
      userId: ['', [Validators.required, Validators.pattern(this.userIdPattern)]],
      password: [
        '',
        [Validators.required, Validators.minLength(6), Validators.maxLength(30), Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{6,30}$/)]
      ],
      confirmPassword: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(30)]],
      preferences: ['', [Validators.maxLength(200)]]
    }, { validators: passwordMatchValidator });
  }

  submit(): void {
    this.errorMessage = '';
    if (this.form.invalid) {
      markAndFocusFirstInvalidControl(this.form);
      this.errorMessage = 'Please fix validation errors before registration.';
      return;
    }

    const raw = this.form.getRawValue();
    const payload = {
      ...raw,
      customerName: this.normalizeText(raw.customerName),
      address: this.normalizeText(raw.address),
      userId: String(raw.userId ?? '').trim(),
      email: String(raw.email ?? '').trim().toLowerCase(),
      preferences: this.normalizeText(raw.preferences)
    };

    this.authApi.register(payload as any).subscribe({
      next: (response: RegisterResponse) => {
        sessionStorage.setItem(
          'registration-profile',
          JSON.stringify({
            customerName: payload.customerName,
            address: payload.address,
            countryCode: payload.countryCode,
            mobileNumber: payload.mobileNumber
          })
        );
        this.sessionService.save({ username: response.customerUsername, role: 'CUSTOMER' });
        sessionStorage.setItem('registration-ack', JSON.stringify(response));
        this.router.navigate(['/registration-ack']);
      },
      error: (error) => {
        const payloadError = error?.error;
        if (typeof payloadError?.error === 'string') {
          this.errorMessage = payloadError.error;
          return;
        }

        if (payloadError && typeof payloadError === 'object') {
          const first = Object.values(payloadError)[0];
          this.errorMessage = typeof first === 'string' ? first : 'Invalid registration input.';
          return;
        }

        this.errorMessage = 'Registration failed. Please try again.';
      }
    });
  }

  reset(): void {
    this.form.reset({ countryCode: '+91', preferences: '' });
    this.errorMessage = '';
  }

  backToLogin(): void {
    this.router.navigate(['/login']);
  }

  private normalizeText(value: unknown): string {
    return String(value ?? '').trim().replace(/\s+/g, ' ');
  }
}
