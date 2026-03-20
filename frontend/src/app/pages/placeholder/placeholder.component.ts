import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-placeholder',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './placeholder.component.html',
  styleUrl: './placeholder.component.css'
})
export class PlaceholderComponent {
  title = '';
  isSupportPage = false;

  readonly supportChannels = [
    { label: 'Email Support', value: 'support@psmlogistics.com', note: 'Best for billing, account, and non-urgent issues.' },
    { label: 'Customer Helpline', value: '+1 (800) 555-0198', note: 'Mon-Sat, 08:00-20:00 local time.' },
    { label: 'Operations Desk', value: '+1 (800) 555-0172', note: 'For pickup, transit, and delivery exceptions.' }
  ];

  readonly supportHours = [
    { team: 'General Support', hours: '08:00 - 20:00 (Mon-Sat)', target: 'First response within 4 business hours' },
    { team: 'Billing and Invoices', hours: '09:00 - 18:00 (Mon-Fri)', target: 'First response within 1 business day' },
    { team: 'Critical Shipment Escalation', hours: '24x7', target: 'First response within 30 minutes' }
  ];

  readonly faqItems = [
    {
      question: 'My tracking status is not updating. What should I do?',
      answer: 'Please share your Tracking Booking ID and last visible timestamp. Our operations desk will verify scan events and update you.'
    },
    {
      question: 'I paid, but invoice is missing.',
      answer: 'Share your Booking ID and transaction reference. Billing support can regenerate or resend the invoice quickly.'
    },
    {
      question: 'Can I change receiver details after booking?',
      answer: 'For security, changes depend on shipment stage. Contact support with Booking ID and revised details for eligibility check.'
    }
  ];

  constructor(private readonly route: ActivatedRoute, private readonly router: Router) {
    this.title = this.route.snapshot.data['title'] ?? 'Screen';
    this.isSupportPage = this.title.toLowerCase().includes('support');
  }

  goHome(): void {
    this.router.navigate(['/home']);
  }
}
