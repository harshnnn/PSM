import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { SessionService } from '../../services/session.service';
import { markAndFocusFirstInvalidControl } from '../../utils/form-validation';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  errorMessage = '';
  infoMessage = '';
  accountNoticeTitle = 'Account Locked';
  lockedAccountMessage = '';
  lockedSupportEmail = 'support@pmslogistics.demo';
  readonly form;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authApi: AuthApiService,
    private readonly sessionService: SessionService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {
    this.form = this.formBuilder.group({
      userId: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(20), Validators.pattern(/^\S+$/)]],
      password: [
        '',
        [
          Validators.required,
          Validators.maxLength(30),
          Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{6,30}$/)
        ]
      ]
    });

    this.route.queryParamMap.subscribe((params) => {
      if (params.get('accountRestricted') === '1') {
        const restrictionType = params.get('restrictionType');
        this.accountNoticeTitle = restrictionType === 'deleted' ? 'Account Deleted By Admin' : 'Account Locked';
        this.lockedAccountMessage = params.get('restrictionMessage') ||
          (restrictionType === 'deleted'
            ? 'Your account has been deleted by admin. Please contact support for assistance.'
            : 'Your account has been locked. Please contact support for help.');
        this.lockedSupportEmail = params.get('supportEmail') || 'support@pmslogistics.demo';
        this.infoMessage = '';
        return;
      }

      if (params.get('accountLocked') === '1') {
        this.accountNoticeTitle = 'Account Locked';
        this.lockedAccountMessage = params.get('lockMessage') || 'Your account has been locked. Please contact support.';
        this.lockedSupportEmail = params.get('supportEmail') || 'support@pmslogistics.demo';
        this.infoMessage = '';
        return;
      }

      if (params.get('passwordChanged') === '1') {
        this.lockedAccountMessage = '';
        this.infoMessage = 'Password changed successfully. Please login again.';
        return;
      }

      if (params.get('sessionExpired') === '1') {
        this.lockedAccountMessage = '';
        this.infoMessage = 'Session expired after service restart. Please login again.';
        return;
      }

      this.lockedAccountMessage = '';
      this.infoMessage = '';
    });
  }

  submit(): void {
    this.errorMessage = '';
    this.lockedAccountMessage = '';

    const userIdControl = this.form.controls.userId;
    const trimmedUserId = String(userIdControl.value ?? '').trim();
    if (trimmedUserId !== userIdControl.value) {
      userIdControl.setValue(trimmedUserId);
    }

    if (this.form.invalid) {
      markAndFocusFirstInvalidControl(this.form);
      this.errorMessage = 'Please fix validation errors before login.';
      return;
    }

    const raw = this.form.getRawValue();
    this.authApi.login({
      userId: String(raw.userId ?? '').trim(),
      password: String(raw.password ?? '')
    }).subscribe({
      next: (response) => {
        this.sessionService.save({ username: response.username, role: response.role, token: response.token });
        this.router.navigate([response.role === 'OFFICER' ? '/admin' : '/home']);
      },
      error: (error) => {
        const payload = error?.error;
        if (error?.status === 423 || payload?.code === 'ACCOUNT_LOCKED') {
          this.accountNoticeTitle = 'Account Locked';
          this.lockedAccountMessage =
            typeof payload?.error === 'string'
              ? payload.error
              : 'Your account has been locked. Please contact support.';
          this.lockedSupportEmail =
            typeof payload?.supportEmail === 'string' && payload.supportEmail.trim().length > 0
              ? payload.supportEmail
              : 'support@pmslogistics.demo';
          this.errorMessage = '';
          return;
        }

        if (typeof payload?.error === 'string') {
          this.errorMessage = payload.error;
          return;
        }

        if (payload && typeof payload === 'object') {
          const first = Object.values(payload)[0];
          this.errorMessage = typeof first === 'string' ? first : 'Invalid input.';
          return;
        }

        this.errorMessage = 'Login failed. Please try again.';
      }
    });
  }

  goToRegister(): void {
    this.router.navigate(['/register']);
  }

  handlePasswordClipboardEvent(event: ClipboardEvent, action: 'copy' | 'paste'): void {
    event.preventDefault();
    this.errorMessage = action === 'copy' ? "Can't copy password." : "Can't paste password.";
  }
}
