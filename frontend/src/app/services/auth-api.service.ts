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
}

export interface LoginRequest {
  userId: string;
  password: string;
}

export interface LoginResponse {
  message: string;
  role: 'CUSTOMER' | 'OFFICER';
  username: string;
}

export interface ProfileResponse {
  customerUsername: string;
  customerName: string;
  address: string;
  countryCode: string;
  mobileNumber: string;
  email: string;
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
}
