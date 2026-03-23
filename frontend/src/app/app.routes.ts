import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { RegistrationAckComponent } from './pages/registration-ack/registration-ack.component';
import { HomeComponent } from './pages/home/home.component';
import { BookingComponent } from './pages/booking/booking.component';
import { BookingHistoryComponent } from './pages/booking-history/booking-history.component';
import { PayBillComponent } from './pages/pay-bill/pay-bill.component';
import { InvoiceComponent } from './pages/invoice/invoice.component';
import { InvoiceListComponent } from './pages/invoice-list/invoice-list.component';
import { TrackingComponent } from './pages/tracking/tracking.component';
import { PickupSchedulingComponent } from './pages/pickup-scheduling/pickup-scheduling.component';
import { DeliveryStatusComponent } from './pages/delivery-status/delivery-status.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { AdminUsersComponent } from './pages/admin-users/admin-users.component';
import { SupportChatComponent } from './pages/support-chat/support-chat.component';
import { authGuard, customerGuard, officerGuard, redirectIfLoggedInGuard } from './services/auth.guard';

export const routes: Routes = [
	{ path: '', redirectTo: 'login', pathMatch: 'full' },
	{ path: 'login', component: LoginComponent, canActivate: [redirectIfLoggedInGuard] },
	{ path: 'register', component: RegisterComponent, canActivate: [redirectIfLoggedInGuard] },
	{ path: 'registration-ack', component: RegistrationAckComponent, canActivate: [redirectIfLoggedInGuard] },
	{
		path: 'admin',
		canActivate: [authGuard, officerGuard],
		children: [
			{ path: '', component: HomeComponent },
			{ path: 'users', component: AdminUsersComponent },
			{ path: 'invoices', component: InvoiceListComponent },
			{ path: 'invoice', component: InvoiceComponent },
			{ path: 'previous-booking', component: BookingHistoryComponent },
			{ path: 'tracking', component: TrackingComponent },
			{ path: 'support', component: SupportChatComponent },
			{ path: 'delivery-status', component: DeliveryStatusComponent },
			{ path: 'pickup-scheduling', component: PickupSchedulingComponent }
		]
	},
	{ path: 'home', component: HomeComponent, canActivate: [authGuard] },
	{ path: 'profile', component: ProfileComponent, canActivate: [authGuard, customerGuard] },
	{ path: 'booking', component: BookingComponent, canActivate: [authGuard, customerGuard] },
	{ path: 'pay-bill', component: PayBillComponent, canActivate: [authGuard, customerGuard] },
	{ path: 'invoices', component: InvoiceListComponent, canActivate: [authGuard, customerGuard] },
	{ path: 'invoice', component: InvoiceComponent, canActivate: [authGuard, customerGuard] },
	{ path: 'tracking', component: TrackingComponent, canActivate: [authGuard, customerGuard] },
	{ path: 'previous-booking', component: BookingHistoryComponent, canActivate: [authGuard, customerGuard] },
	{ path: 'previous-bookings', redirectTo: 'previous-booking', pathMatch: 'full' },
	{ path: 'support', component: SupportChatComponent, canActivate: [authGuard, customerGuard] },
	{ path: 'delivery-status', redirectTo: 'admin/delivery-status', pathMatch: 'full' },
	{ path: 'pickup-scheduling', redirectTo: 'admin/pickup-scheduling', pathMatch: 'full' },
	{ path: '**', redirectTo: 'home' }
];
