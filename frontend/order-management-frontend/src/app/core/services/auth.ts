import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest } from '../../models/login-request';
import { LoginResponse } from '../../models/login-response';

/**
 * AuthService manages user authentication and token storage.
 * 
 * Security Notes:
 * - Tokens are stored in localStorage (consider sessionStorage for better security)
 * - HTTPS should be enforced in production
 * - Implement token refresh mechanism for long-lived sessions
 * - Consider implementing CSRF protection
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl = `${environment.authUrl}/login`;
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USERNAME_KEY = 'auth_username';

  constructor(private http: HttpClient) {
    this.validateTokenExpiration();
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    if (!request.username || !request.password) {
      throw new Error('Username and password are required');
    }

    return this.http.post<LoginResponse>(this.apiUrl, request).pipe(
      tap(response => {
        if (response && response.token) {
          this.setToken(response.token);
          this.setUsername(response.username);
        }
      })
    );
  }

  logout(): void {
    this.clearToken();
    this.clearUsername();
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getUsername(): string | null {
    return localStorage.getItem(this.USERNAME_KEY);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    return !!token && !this.isTokenExpired(token);
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  private clearToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  private setUsername(username: string): void {
    localStorage.setItem(this.USERNAME_KEY, username);
  }

  private clearUsername(): void {
    localStorage.removeItem(this.USERNAME_KEY);
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = this.parseJwt(token);
      if (!payload.exp) return false;
      return Date.now() >= payload.exp * 1000;
    } catch (error) {
      console.warn('Error parsing token:', error);
      return true;
    }
  }

  private parseJwt(token: string): any {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64).split('').map((c) => {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join('')
    );
    return JSON.parse(jsonPayload);
  }

  private validateTokenExpiration(): void {
    const token = this.getToken();
    if (token && this.isTokenExpired(token)) {
      this.logout();
    }
  }
}