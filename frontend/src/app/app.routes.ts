import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { RegistrationAckComponent } from './pages/registration-ack/registration-ack.component';
import { HomeComponent } from './pages/home/home.component';
import { PlaceholderComponent } from './pages/placeholder/placeholder.component';
import { BookingComponent } from './pages/booking/booking.component';
import { BookingHistoryComponent } from './pages/booking-history/booking-history.component';
import { PayBillComponent } from './pages/pay-bill/pay-bill.component';
import { InvoiceComponent } from './pages/invoice/invoice.component';
import { InvoiceListComponent } from './pages/invoice-list/invoice-list.component';
import { TrackingComponent } from './pages/tracking/tracking.component';
import { PickupSchedulingComponent } from './pages/pickup-scheduling/pickup-scheduling.component';
import { DeliveryStatusComponent } from './pages/delivery-status/delivery-status.component';
import { authGuard, officerGuard, redirectIfLoggedInGuard } from './services/auth.guard';

export const routes: Routes = [
	{ path: '', redirectTo: 'login', pathMatch: 'full' },
	{ path: 'login', component: LoginComponent, canActivate: [redirectIfLoggedInGuard] },
	{ path: 'register', component: RegisterComponent, canActivate: [redirectIfLoggedInGuard] },
	{ path: 'registration-ack', component: RegistrationAckComponent, canActivate: [redirectIfLoggedInGuard] },
	{ path: 'home', component: HomeComponent, canActivate: [authGuard] },
	{ path: 'booking', component: BookingComponent, canActivate: [authGuard] },
	{ path: 'pay-bill', component: PayBillComponent, canActivate: [authGuard] },
	{ path: 'invoices', component: InvoiceListComponent, canActivate: [authGuard] },
	{ path: 'invoice', component: InvoiceComponent, canActivate: [authGuard] },
	{ path: 'tracking', component: TrackingComponent, canActivate: [authGuard] },
	{ path: 'previous-booking', component: BookingHistoryComponent, canActivate: [authGuard] },
	{ path: 'previous-bookings', redirectTo: 'previous-booking', pathMatch: 'full' },
	{ path: 'support', component: PlaceholderComponent, canActivate: [authGuard], data: { title: 'Contact Support' } },
	{ path: 'delivery-status', component: DeliveryStatusComponent, canActivate: [authGuard, officerGuard] },
	{ path: 'pickup-scheduling', component: PickupSchedulingComponent, canActivate: [authGuard, officerGuard] },
	{ path: '**', redirectTo: 'home' }
];
