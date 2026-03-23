import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function repeatedDigitLimitValidator(maxConsecutive: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const raw = String(control.value ?? '');
    if (!raw) {
      return null;
    }

    const digitsOnly = raw.replace(/\D/g, '');
    if (!digitsOnly) {
      return null;
    }

    // Disallow runs longer than maxConsecutive (e.g., 6 or more when maxConsecutive is 5).
    const runTooLongPattern = new RegExp(`(\\d)\\1{${maxConsecutive},}`);
    return runTooLongPattern.test(digitsOnly)
      ? { repeatDigits: { maxConsecutive } }
      : null;
  };
}
