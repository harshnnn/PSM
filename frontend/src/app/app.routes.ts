import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { RegistrationAckComponent } from './pages/registration-ack/registration-ack.component';
import { HomeComponent } from './pages/home/home.component';
import { PlaceholderComponent } from './pages/placeholder/placeholder.component';
import { authGuard } from './services/auth.guard';

export const routes: Routes = [
	{ path: '', redirectTo: 'login', pathMatch: 'full' },
	{ path: 'login', component: LoginComponent },
	{ path: 'register', component: RegisterComponent },
	{ path: 'registration-ack', component: RegistrationAckComponent },
	{ path: 'home', component: HomeComponent, canActivate: [authGuard] },
	{ path: 'booking', component: PlaceholderComponent, canActivate: [authGuard], data: { title: 'Booking Service' } },
	{ path: 'tracking', component: PlaceholderComponent, canActivate: [authGuard], data: { title: 'Tracking' } },
	{ path: 'previous-booking', component: PlaceholderComponent, canActivate: [authGuard], data: { title: 'Previous Booking' } },
	{ path: 'support', component: PlaceholderComponent, canActivate: [authGuard], data: { title: 'Contact Support' } },
	{ path: 'delivery-status', component: PlaceholderComponent, canActivate: [authGuard], data: { title: 'Delivery Status' } },
	{ path: 'pickup-scheduling', component: PlaceholderComponent, canActivate: [authGuard], data: { title: 'Pickup Scheduling' } },
	{ path: '**', redirectTo: 'login' }
];
