import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RegisterRequest {
  customerName: string;
  email: string;
  countryCode: string;
  mobileNumber: string;
  address: string;
  userId: string;
  password: string;
  confirmPassword: string;
  preferences: string;
}

export interface RegisterResponse {
  message: string;
  customerUsername: string;
  customerName: string;
  email: string;
  token: string;
}

export interface LoginRequest {
  userId: string;
  password: string;
}

export interface LoginResponse {
  message: string;
  role: 'CUSTOMER' | 'OFFICER';
  username: string;
  token: string;
}

export interface ProfileResponse {
  customerUsername: string;
  customerName: string;
  address: string;
  countryCode: string;
  mobileNumber: string;
  email: string;
  preferences?: string;
}

export interface UpdateProfileRequest {
  customerName: string;
  email: string;
  countryCode: string;
  mobileNumber: string;
  address: string;
  preferences?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmNewPassword: string;
}

export interface AdminUserSummaryResponse {
  customerUsername: string;
  customerName: string;
  email: string;
  countryCode: string;
  mobileNumber: string;
  role: string;
  accountLocked: boolean;
}

export interface AdminUserActionResponse {
  message: string;
  customerUsername: string;
  accountLocked: boolean;
}

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly baseUrl = 'http://localhost:8080/auth';

  constructor(private readonly http: HttpClient) {}

  register(payload: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.baseUrl}/register`, payload);
  }

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, payload);
  }

  profile(customerUsername: string): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.baseUrl}/profile/${customerUsername}`);
  }

  updateProfile(customerUsername: string, payload: UpdateProfileRequest): Observable<ProfileResponse> {
    return this.http.put<ProfileResponse>(`${this.baseUrl}/profile/${customerUsername}`, payload);
  }

  changePassword(customerUsername: string, payload: ChangePasswordRequest): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/profile/${customerUsername}/password`, payload);
  }

  listManagedUsers(): Observable<AdminUserSummaryResponse[]> {
    return this.http.get<AdminUserSummaryResponse[]>(`${this.baseUrl}/admin/users`);
  }

  lockUserAccount(customerUsername: string): Observable<AdminUserActionResponse> {
    return this.http.put<AdminUserActionResponse>(`${this.baseUrl}/admin/users/${customerUsername}/lock`, {});
  }

  unlockUserAccount(customerUsername: string): Observable<AdminUserActionResponse> {
    return this.http.put<AdminUserActionResponse>(`${this.baseUrl}/admin/users/${customerUsername}/unlock`, {});
  }

  deleteUserAccount(customerUsername: string): Observable<AdminUserActionResponse> {
    return this.http.delete<AdminUserActionResponse>(`${this.baseUrl}/admin/users/${customerUsername}`);
  }
}
