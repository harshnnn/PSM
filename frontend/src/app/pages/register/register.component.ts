import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService, RegisterResponse } from '../../services/auth-api.service';
import { SessionService } from '../../services/session.service';

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

  readonly countryCodes = ['+1', '+44', '+61', '+91'];

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authApi: AuthApiService,
    private readonly sessionService: SessionService,
    private readonly router: Router
  ) {
    this.form = this.formBuilder.group({
      customerName: ['', [Validators.required, Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      countryCode: ['+91', [Validators.required]],
      mobileNumber: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      address: ['', [Validators.required, Validators.maxLength(200)]],
      userId: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(20)]],
      password: [
        '',
        [Validators.required, Validators.maxLength(30), Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{6,30}$/)]
      ],
      confirmPassword: ['', [Validators.required, Validators.maxLength(30)]],
      preferences: ['']
    });
  }

  submit(): void {
    this.errorMessage = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage = 'Please fix validation errors before registration.';
      return;
    }

    const payload = this.form.getRawValue();
    if (payload.password !== payload.confirmPassword) {
      this.errorMessage = 'Password and Confirm Password must match.';
      return;
    }

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
}
