import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription, finalize } from 'rxjs';
import { AuthApiService, ChangePasswordRequest, ProfileResponse, UpdateProfileRequest } from '../../services/auth-api.service';
import { SessionService } from '../../services/session.service';
import { markAndFocusFirstInvalidControl } from '../../utils/form-validation';

function confirmPasswordValidator(control: AbstractControl): ValidationErrors | null {
  const newPassword = control.get('newPassword')?.value;
  const confirmNewPassword = control.get('confirmNewPassword')?.value;

  if (!newPassword || !confirmNewPassword) {
    return null;
  }

  return newPassword === confirmNewPassword ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit, OnDestroy {
  readonly form;
  readonly passwordForm;
  readonly countryCodes = ['+1', '+44', '+61', '+91'];

  loading = false;
  saving = false;
  loadError = '';
  saveError = '';
  saveSuccess = '';
  passwordError = '';
  passwordSuccess = '';
  customerUsername = '';
  changingPassword = false;

  private profileSub?: Subscription;
  private saveSub?: Subscription;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authApi: AuthApiService,
    private readonly sessionService: SessionService,
    private readonly router: Router
  ) {
    this.form = this.formBuilder.group({
      customerName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50), Validators.pattern(/^[A-Za-z][A-Za-z .'-]*$/)]],
      email: ['', [Validators.required, Validators.email]],
      countryCode: ['+91', [Validators.required]],
      mobileNumber: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      address: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
      preferences: ['', [Validators.maxLength(200)]]
    });

    this.passwordForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required, Validators.maxLength(30)]],
      newPassword: [
        '',
        [
          Validators.required,
          Validators.maxLength(30),
          Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{6,30}$/)
        ]
      ],
      confirmNewPassword: ['', [Validators.required, Validators.maxLength(30)]]
    }, { validators: confirmPasswordValidator });
  }

  ngOnInit(): void {
    const session = this.sessionService.get();
    if (!session || session.role !== 'CUSTOMER') {
      this.router.navigate(['/home']);
      return;
    }

    this.customerUsername = session.username;
    this.fetchProfile();
  }

  ngOnDestroy(): void {
    this.profileSub?.unsubscribe();
    this.saveSub?.unsubscribe();
  }

  fetchProfile(): void {
    this.loading = true;
    this.loadError = '';
    this.saveSuccess = '';
    this.profileSub?.unsubscribe();

    this.profileSub = this.authApi.profile(this.customerUsername)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (profile: ProfileResponse) => {
          this.form.reset({
            customerName: profile.customerName ?? '',
            email: profile.email ?? '',
            countryCode: profile.countryCode ?? '+91',
            mobileNumber: profile.mobileNumber ?? '',
            address: profile.address ?? '',
            preferences: profile.preferences ?? ''
          });
          this.form.markAsPristine();
        },
        error: () => {
          this.loadError = 'Unable to load profile right now. Please try again.';
        }
      });
  }

  saveProfile(): void {
    this.saveError = '';
    this.saveSuccess = '';

    if (this.form.invalid) {
      markAndFocusFirstInvalidControl(this.form);
      this.saveError = 'Please fix validation errors before saving profile details.';
      return;
    }

    const raw = this.form.getRawValue();
    const payload: UpdateProfileRequest = {
      customerName: this.normalizeText(raw.customerName),
      email: String(raw.email ?? '').trim().toLowerCase(),
      countryCode: String(raw.countryCode ?? '').trim(),
      mobileNumber: String(raw.mobileNumber ?? '').trim(),
      address: this.normalizeText(raw.address),
      preferences: this.normalizeText(raw.preferences)
    };

    this.saving = true;
    this.saveSub?.unsubscribe();
    this.saveSub = this.authApi.updateProfile(this.customerUsername, payload)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: (updated) => {
          this.form.patchValue({
            customerName: updated.customerName,
            email: updated.email,
            countryCode: updated.countryCode,
            mobileNumber: updated.mobileNumber,
            address: updated.address,
            preferences: updated.preferences ?? ''
          });
          this.form.markAsPristine();

          sessionStorage.setItem(
            'registration-profile',
            JSON.stringify({
              customerName: updated.customerName,
              address: updated.address,
              countryCode: updated.countryCode,
              mobileNumber: updated.mobileNumber
            })
          );

          this.saveSuccess = 'Profile updated successfully.';
        },
        error: (error) => {
          const payloadError = error?.error;
          if (typeof payloadError?.error === 'string') {
            this.saveError = payloadError.error;
            return;
          }

          if (payloadError && typeof payloadError === 'object') {
            const first = Object.values(payloadError)[0];
            this.saveError = typeof first === 'string' ? first : 'Unable to update profile details.';
            return;
          }

          this.saveError = 'Unable to update profile details. Please try again.';
        }
      });
  }

  resetChanges(): void {
    this.fetchProfile();
  }

  changePassword(): void {
    this.passwordError = '';
    this.passwordSuccess = '';

    if (this.passwordForm.invalid) {
      markAndFocusFirstInvalidControl(this.passwordForm);
      this.passwordError = 'Please fix validation errors before changing password.';
      return;
    }

    const raw = this.passwordForm.getRawValue();
    if (raw.currentPassword === raw.newPassword) {
      this.passwordError = 'New password must be different from current password.';
      return;
    }

    const confirmed = window.confirm('Changing your password will sign you out for security. Continue?');
    if (!confirmed) {
      return;
    }

    const payload: ChangePasswordRequest = {
      currentPassword: String(raw.currentPassword ?? ''),
      newPassword: String(raw.newPassword ?? ''),
      confirmNewPassword: String(raw.confirmNewPassword ?? '')
    };

    this.changingPassword = true;
    this.authApi.changePassword(this.customerUsername, payload)
      .pipe(finalize(() => (this.changingPassword = false)))
      .subscribe({
        next: () => {
          this.passwordSuccess = 'Password changed successfully. Redirecting to login...';
          this.passwordForm.reset();
          this.passwordForm.markAsPristine();
          this.sessionService.clear();
          this.router.navigate(['/login'], { queryParams: { passwordChanged: '1' } });
        },
        error: (error) => {
          const payloadError = error?.error;
          if (typeof payloadError?.error === 'string') {
            this.passwordError = payloadError.error;
            return;
          }

          if (payloadError && typeof payloadError === 'object') {
            const first = Object.values(payloadError)[0];
            this.passwordError = typeof first === 'string' ? first : 'Unable to change password right now.';
            return;
          }

          this.passwordError = 'Unable to change password right now. Please try again.';
        }
      });
  }

  private normalizeText(value: unknown): string {
    return String(value ?? '').trim().replace(/\s+/g, ' ');
  }
}
