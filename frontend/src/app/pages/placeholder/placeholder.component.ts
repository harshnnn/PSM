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

  constructor(private readonly route: ActivatedRoute, private readonly router: Router) {
    this.title = this.route.snapshot.data['title'] ?? 'Screen';
  }

  goHome(): void {
    this.router.navigate(['/home']);
  }
}
