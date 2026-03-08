import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SessionData, SessionService } from '../../services/session.service';

interface MenuItem {
  label: string;
  path: string;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  session: SessionData | null = null;
  menuItems: MenuItem[] = [];

  constructor(private readonly sessionService: SessionService, private readonly router: Router) {}

  ngOnInit(): void {
    this.session = this.sessionService.get();

    if (!this.session) {
      this.router.navigate(['/login']);
      return;
    }

    // if (this.session.role === 'CUSTOMER') {
    //   this.menuItems = [
    //     { label: 'Home', path: '/home' },
    //     { label: 'Booking Service', path: '/booking' },
    //     { label: 'Tracking', path: '/tracking' },
    //     { label: 'Previous Booking', path: '/previous-booking' },
    //     { label: 'Contact Support', path: '/support' }
    //   ];
    // } else {
    //   this.menuItems = [
    //     { label: 'Home', path: '/home' },
    //     { label: 'Booking Service', path: '/booking' },
    //     { label: 'Tracking', path: '/tracking' },
    //     { label: 'Delivery Status', path: '/delivery-status' },
    //     { label: 'Pickup Scheduling', path: '/pickup-scheduling' },
    //     { label: 'Previous Booking', path: '/previous-booking' }
    //   ];
    // }
  }

  navigate(path: string): void {
    this.router.navigate([path]);
  }

  logout(): void {
    this.sessionService.clear();
    sessionStorage.removeItem('registration-ack');
    this.router.navigate(['/login']);
  }
}
