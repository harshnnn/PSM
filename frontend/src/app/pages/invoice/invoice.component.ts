import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-invoice',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './invoice.component.html',
  styleUrl: './invoice.component.css'
})
export class InvoiceComponent implements OnInit {
  bookingId: number | null = null;
  amount: number | null = null;
  transactionRef = '';

  constructor(private readonly route: ActivatedRoute, private readonly router: Router) {}

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    this.bookingId = Number(params.get('bookingId')) || null;
    this.amount = Number(params.get('amount')) || null;
    this.transactionRef = params.get('transactionRef') || '';
  }

  goHome(): void {
    this.router.navigate(['/home']);
  }

  viewHistory(): void {
    this.router.navigate(['/previous-booking']);
  }
}
