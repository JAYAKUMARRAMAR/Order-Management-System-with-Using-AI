import { Component, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../core/services/auth';
import { InputValidators } from '../../core/validators/input-validators';

@Component({

  selector: 'app-login',
  standalone: true,
  imports: [ CommonModule,FormsModule ],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {

  username = '';
  password = '';
  errorMessage = '';
  loading = false;
  showPassword = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private destroyRef: DestroyRef
  ) {}

  get isFormValid(): boolean {
    return InputValidators.isValidUsername(this.username) &&
           InputValidators.isValidPassword(this.password);
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  login(): void {
    if (!this.isFormValid) {
      this.errorMessage = 'Please enter valid username and password';
      return;
    }

    this.errorMessage = '';
    this.loading = true;

    const sanitizedUsername = InputValidators.sanitizeInput(this.username);
    const sanitizedPassword = this.password; // Don't trim password

    this.authService
      .login({
        username: sanitizedUsername,
        password: sanitizedPassword
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate([
            '/dashboard'
          ]);
        },

        error: (error) => {
          this.loading = false;
          this.errorMessage = error.message || 
            'Invalid username or password';
        }
      });
  }
}