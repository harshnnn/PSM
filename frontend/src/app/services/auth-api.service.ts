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
}
