import { FormGroup } from '@angular/forms';

function isVisible(element: HTMLElement): boolean {
  return !!(element.offsetWidth || element.offsetHeight || element.getClientRects().length);
}

export function markAndFocusFirstInvalidControl(form: FormGroup): void {
  form.markAllAsTouched();

  if (typeof document === 'undefined') {
    return;
  }

  setTimeout(() => {
    const invalidControls = Array.from(
      document.querySelectorAll<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>(
        'input.ng-invalid, textarea.ng-invalid, select.ng-invalid'
      )
    );

    const firstInvalid = invalidControls.find((control) => !control.disabled && isVisible(control));
    if (!firstInvalid) {
      return;
    }

    firstInvalid.focus({ preventScroll: true });
    firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }, 0);
}
