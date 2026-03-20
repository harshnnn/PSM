import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
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
  readonly form;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authApi: AuthApiService,
    private readonly sessionService: SessionService,
    private readonly router: Router
  ) {
    this.form = this.formBuilder.group({
      userId: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(20)]],
      password: [
        '',
        [
          Validators.required,
          Validators.maxLength(30),
          Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{6,30}$/)
        ]
      ]
    });
  }

  submit(): void {
    this.errorMessage = '';
    if (this.form.invalid) {
      markAndFocusFirstInvalidControl(this.form);
      this.errorMessage = 'Please fix validation errors before login.';
      return;
    }

    this.authApi.login(this.form.getRawValue() as { userId: string; password: string }).subscribe({
      next: (response) => {
        this.sessionService.save({ username: response.username, role: response.role });
        this.router.navigate(['/home']);
      },
      error: (error) => {
        const payload = error?.error;
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
}
