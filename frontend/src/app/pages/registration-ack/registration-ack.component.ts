import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

interface AckData {
  message: string;
  customerUsername: string;
  customerName: string;
  email: string;
}

@Component({
  selector: 'app-registration-ack',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './registration-ack.component.html',
  styleUrl: './registration-ack.component.css'
})
export class RegistrationAckComponent implements OnInit {
  ackData: AckData | null = null;

  constructor(private readonly router: Router) {}

  ngOnInit(): void {
    const raw = sessionStorage.getItem('registration-ack');
    this.ackData = raw ? (JSON.parse(raw) as AckData) : null;

    if (!this.ackData) {
      this.router.navigate(['/register']);
    }
  }

  openHome(): void {
    this.router.navigate(['/home']);
  }

  openLogin(): void {
    this.router.navigate(['/login']);
  }
}
