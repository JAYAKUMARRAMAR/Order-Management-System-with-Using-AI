/**
 * Common validators for form inputs
 */

export class InputValidators {
  static isValidUsername(username: string): boolean {
    if (!username || username.trim().length === 0) {
      return false;
    }
    // Username should be 3-20 characters, alphanumeric + underscore
    const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/;
    return usernameRegex.test(username.trim());
  }

  static isValidPassword(password: string): boolean {
    if (!password || password.length === 0) {
      return false;
    }
    // Password should be at least 6 characters
    return password.length >= 6;
  }

  static getPasswordStrength(password: string): 'weak' | 'medium' | 'strong' {
    if (!password) return 'weak';

    let strength = 0;
    if (password.length >= 8) strength++;
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++;
    if (/\d/.test(password)) strength++;
    if (/[^a-zA-Z\d]/.test(password)) strength++;

    if (strength <= 1) return 'weak';
    if (strength <= 2) return 'medium';
    return 'strong';
  }

  static sanitizeInput(input: string): string {
    return input.trim();
  }
}
