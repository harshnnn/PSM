import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { RegistrationAckComponent } from './pages/registration-ack/registration-ack.component';
import { HomeComponent } from './pages/home/home.component';
import { PlaceholderComponent } from './pages/placeholder/placeholder.component';
import { BookingComponent } from './pages/booking/booking.component';
import { BookingHistoryComponent } from './pages/booking-history/booking-history.component';
import { authGuard, redirectIfLoggedInGuard } from './services/auth.guard';

export const routes: Routes = [
	{ path: '', redirectTo: 'login', pathMatch: 'full' },
	{ path: 'login', component: LoginComponent, canActivate: [redirectIfLoggedInGuard] },
	{ path: 'register', component: RegisterComponent, canActivate: [redirectIfLoggedInGuard] },
	{ path: 'registration-ack', component: RegistrationAckComponent, canActivate: [redirectIfLoggedInGuard] },
	{ path: 'home', component: HomeComponent, canActivate: [authGuard] },
	{ path: 'booking', component: BookingComponent, canActivate: [authGuard] },
	{ path: 'tracking', component: PlaceholderComponent, canActivate: [authGuard], data: { title: 'Tracking' } },
	{ path: 'previous-booking', component: BookingHistoryComponent, canActivate: [authGuard] },
	{ path: 'previous-bookings', redirectTo: 'previous-booking', pathMatch: 'full' },
	{ path: 'support', component: PlaceholderComponent, canActivate: [authGuard], data: { title: 'Contact Support' } },
	{ path: 'delivery-status', component: PlaceholderComponent, canActivate: [authGuard], data: { title: 'Delivery Status' } },
	{ path: 'pickup-scheduling', component: PlaceholderComponent, canActivate: [authGuard], data: { title: 'Pickup Scheduling' } },
	{ path: '**', redirectTo: 'home' }
];
