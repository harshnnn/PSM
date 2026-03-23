import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AdminUserSummaryResponse,
  AuthApiService
} from '../../services/auth-api.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.css'
})
export class AdminUsersComponent implements OnInit {
  users: AdminUserSummaryResponse[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(private readonly authApi: AuthApiService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authApi.listManagedUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Unable to load users right now. Please try again.';
      }
    });
  }

  lockUser(user: AdminUserSummaryResponse): void {
    this.errorMessage = '';
    this.successMessage = '';

    const confirmed = window.confirm(
      `Lock account ${user.customerUsername}? The user will be logged out and cannot sign in until unlocked.`
    );
    if (!confirmed) {
      return;
    }

    this.authApi.lockUserAccount(user.customerUsername).subscribe({
      next: (response) => {
        user.accountLocked = true;
        this.successMessage = response.message;
      },
      error: (error) => {
        this.errorMessage = this.extractErrorMessage(error, 'Unable to lock account.');
      }
    });
  }

  unlockUser(user: AdminUserSummaryResponse): void {
    this.errorMessage = '';
    this.successMessage = '';

    this.authApi.unlockUserAccount(user.customerUsername).subscribe({
      next: (response) => {
        user.accountLocked = false;
        this.successMessage = response.message;
      },
      error: (error) => {
        this.errorMessage = this.extractErrorMessage(error, 'Unable to unlock account.');
      }
    });
  }

  deleteUser(user: AdminUserSummaryResponse): void {
    this.errorMessage = '';
    this.successMessage = '';

    const confirmed = window.confirm(
      `Delete account ${user.customerUsername}? This action cannot be undone.`
    );
    if (!confirmed) {
      return;
    }

    this.authApi.deleteUserAccount(user.customerUsername).subscribe({
      next: (response) => {
        this.users = this.users.filter((item) => item.customerUsername !== user.customerUsername);
        this.successMessage = response.message;
      },
      error: (error) => {
        this.errorMessage = this.extractErrorMessage(error, 'Unable to delete account.');
      }
    });
  }

  private extractErrorMessage(error: unknown, fallback: string): string {
    const payload = (error as { error?: unknown })?.error;

    if (payload && typeof payload === 'object') {
      const objectPayload = payload as Record<string, unknown>;
      if (typeof objectPayload['error'] === 'string') {
        return objectPayload['error'];
      }
      const first = Object.values(objectPayload)[0];
      if (typeof first === 'string') {
        return first;
      }
    }

    return fallback;
  }
}
