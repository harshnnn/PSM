import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription, catchError, finalize, forkJoin, of } from 'rxjs';
import { BookingApiService, BookingResponse } from '../../services/booking-api.service';
import { BookingHistoryApiService, BookingHistoryEntry, PageResponse } from '../../services/booking-history-api.service';
import { InvoiceApiService } from '../../services/invoice-api.service';
import { SessionData, SessionService } from '../../services/session.service';
import { TrackingApiService, TrackingResponse } from '../../services/tracking-api.service';

interface DashboardMetric {
  label: string;
  value: string;
  hint: string;
  tone: 'cyan' | 'green' | 'amber' | 'rose';
}

interface StatusInsight {
  label: string;
  count: number;
  percent: number;
}

interface OfficerActionItem {
  label: string;
  count: number;
  detail: string;
  tone: 'amber' | 'violet' | 'rose';
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
  loading = false;
  loadError = '';
  lastUpdatedAt: Date | null = null;

  customerMetrics: DashboardMetric[] = [];
  customerStatusInsights: StatusInsight[] = [];
  customerRecentBookings: BookingHistoryEntry[] = [];
  customerUpcomingPickups: BookingResponse[] = [];

  officerMetrics: DashboardMetric[] = [];
  officerStatusInsights: StatusInsight[] = [];
  officerRecentShipments: TrackingResponse[] = [];
  officerRecentBookings: BookingHistoryEntry[] = [];
  officerActionItems: OfficerActionItem[] = [];

  private dashboardSub?: Subscription;

  constructor(
    private readonly sessionService: SessionService,
    private readonly router: Router,
    private readonly bookingApi: BookingApiService,
    private readonly historyApi: BookingHistoryApiService,
    private readonly invoiceApi: InvoiceApiService,
    private readonly trackingApi: TrackingApiService
  ) {}

  ngOnInit(): void {
    this.session = this.sessionService.get();

    if (!this.session) {
      this.router.navigate(['/login']);
      return;
    }

    this.refreshDashboard();
  }

  refreshDashboard(): void {
    if (!this.session) {
      return;
    }

    if (this.session.role === 'CUSTOMER') {
      this.loadCustomerDashboard(this.session.username);
      return;
    }

    this.loadOfficerDashboard();
  }

  private loadCustomerDashboard(customerId: string): void {
    this.loading = true;
    this.loadError = '';
    this.dashboardSub?.unsubscribe();

    this.dashboardSub = forkJoin({
      bookings: this.bookingApi.getAll().pipe(catchError(() => of([] as BookingResponse[]))),
      unpaidBookings: this.bookingApi.getUnpaid(customerId).pipe(catchError(() => of([] as BookingResponse[]))),
      invoices: this.invoiceApi.list(customerId).pipe(catchError(() => of([])),
      ),
      history: this.historyApi.getCustomerHistory(customerId, 0, 100).pipe(
        catchError(() => of(this.emptyPage<BookingHistoryEntry>(0, 100)))
      )
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe(({ bookings, unpaidBookings, invoices, history }) => {
        const normalizedHistoryStatuses = history.content.map((entry) => this.normalizeStatus(entry.status));
        const paidBookings = invoices.length;
        const deliveredCount = normalizedHistoryStatuses.filter((status) => status === 'DELIVERED').length;
        const activeCount = normalizedHistoryStatuses.filter(
          (status) => status === 'CONFIRMED' || status === 'IN_TRANSIT'
        ).length;
        const totalSpend = invoices.reduce((sum, invoice) => sum + Number(invoice.parcelServiceCost ?? 0), 0);

        const bookingIdSet = new Set(history.content.map((item) => Number(item.bookingId)).filter((value) => !Number.isNaN(value)));
        const customerBookings = bookings.filter((booking) => {
          const sameCustomer = (booking.customerId ?? '').toLowerCase() === customerId.toLowerCase();
          const inHistory = bookingIdSet.has(booking.id);
          return sameCustomer || inHistory;
        });

        const now = Date.now();
        this.customerUpcomingPickups = customerBookings
          .filter((booking) => {
            const time = Date.parse(booking.preferredPickup);
            return Number.isFinite(time) && time >= now;
          })
          .sort((a, b) => Date.parse(a.preferredPickup) - Date.parse(b.preferredPickup))
          .slice(0, 4);

        const totalBookings = history.totalElements;
        this.customerMetrics = [
          {
            label: 'Total Bookings',
            value: String(totalBookings),
            hint: 'Bookings placed by your account.',
            tone: 'cyan'
          },
          {
            label: 'Awaiting Payment',
            value: String(unpaidBookings.length),
            hint: 'Bookings pending bill settlement.',
            tone: 'amber'
          },
          {
            label: 'In Delivery Flow',
            value: String(activeCount),
            hint: 'Currently confirmed or in transit.',
            tone: 'green'
          },
          {
            label: 'Total Spend',
            value: this.formatCurrency(totalSpend),
            hint: `${paidBookings} paid booking${paidBookings === 1 ? '' : 's'} on record.`,
            tone: 'rose'
          }
        ];

        this.customerStatusInsights = this.buildStatusInsights([
          { label: 'Confirmed', count: normalizedHistoryStatuses.filter((status) => status === 'CONFIRMED').length },
          { label: 'In Transit', count: normalizedHistoryStatuses.filter((status) => status === 'IN_TRANSIT').length },
          { label: 'Delivered', count: deliveredCount },
          { label: 'Cancelled', count: normalizedHistoryStatuses.filter((status) => status === 'CANCELLED').length }
        ]);

        this.customerRecentBookings = history.content.slice(0, 6);
        this.lastUpdatedAt = new Date();
      });
  }

  private loadOfficerDashboard(): void {
    this.loading = true;
    this.loadError = '';
    this.dashboardSub?.unsubscribe();

    this.dashboardSub = forkJoin({
      shipments: this.trackingApi.officerShipments().pipe(catchError(() => of([] as TrackingResponse[]))),
      history: this.historyApi.getOfficerHistory(null, null, null, 0, 8).pipe(
        catchError(() => of(this.emptyPage<BookingHistoryEntry>(0, 8)))
      )
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe(({ shipments, history }) => {
        const normalizedStatuses = shipments.map((shipment) => this.normalizeStatus(shipment.trackingStatus));
        const confirmedCount = normalizedStatuses.filter((status) => status === 'CONFIRMED').length;
        const inTransitCount = normalizedStatuses.filter((status) => status === 'IN_TRANSIT').length;
        const deliveredCount = normalizedStatuses.filter((status) => status === 'DELIVERED').length;
        const cancelledCount = normalizedStatuses.filter((status) => status === 'CANCELLED').length;

        const now = new Date();
        const pickupsToday = shipments.filter(
          (shipment) => shipment.pickupScheduledAt && this.isSameDay(shipment.pickupScheduledAt, now)
        ).length;
        const deliveredToday = shipments.filter(
          (shipment) =>
            this.normalizeStatus(shipment.trackingStatus) === 'DELIVERED' && this.isSameDay(shipment.lastUpdatedAt, now)
        ).length;
        const pendingPickup = shipments.filter(
          (shipment) =>
            this.normalizeStatus(shipment.trackingStatus) === 'CONFIRMED' &&
            (!shipment.pickupScheduledAt || shipment.pickupScheduledAt.trim().length === 0)
        ).length;
        const agedTransit = shipments.filter((shipment) => {
          if (this.normalizeStatus(shipment.trackingStatus) !== 'IN_TRANSIT') {
            return false;
          }

          const updatedAt = Date.parse(shipment.lastUpdatedAt);
          if (!Number.isFinite(updatedAt)) {
            return false;
          }

          return Date.now() - updatedAt > 48 * 60 * 60 * 1000;
        }).length;

        this.officerMetrics = [
          {
            label: 'Total Shipments',
            value: String(shipments.length),
            hint: 'All shipments visible to officer operations.',
            tone: 'cyan'
          },
          {
            label: 'Pending Pickup',
            value: String(pendingPickup),
            hint: `${pickupsToday} pickup${pickupsToday === 1 ? '' : 's'} scheduled today.`,
            tone: 'amber'
          },
          {
            label: 'In Transit',
            value: String(inTransitCount),
            hint: 'Shipments currently moving through network.',
            tone: 'green'
          },
          {
            label: 'Delivered Today',
            value: String(deliveredToday),
            hint: 'Completed deliveries in the current day.',
            tone: 'rose'
          }
        ];

        this.officerStatusInsights = this.buildStatusInsights([
          { label: 'Confirmed', count: confirmedCount },
          { label: 'In Transit', count: inTransitCount },
          { label: 'Delivered', count: deliveredCount },
          { label: 'Cancelled', count: cancelledCount }
        ]);

        this.officerActionItems = [
          {
            label: 'Schedule pickups',
            count: pendingPickup,
            detail: 'Confirmed shipments still waiting for pickup date/time.',
            tone: 'amber'
          },
          {
            label: 'Transit aging watch',
            count: agedTransit,
            detail: 'Shipments in transit for more than 48 hours.',
            tone: 'violet'
          },
          {
            label: 'Exception handling',
            count: cancelledCount,
            detail: 'Cancelled or returned shipments needing closure updates.',
            tone: 'rose'
          }
        ];

        this.officerRecentShipments = [...shipments]
          .sort((a, b) => Date.parse(b.lastUpdatedAt) - Date.parse(a.lastUpdatedAt))
          .slice(0, 6);
        this.officerRecentBookings = history.content.slice(0, 6);

        if (shipments.length === 0 && history.content.length === 0) {
          this.loadError = 'No officer dashboard data is available right now.';
        }

        this.lastUpdatedAt = new Date();
        this.customerUpcomingPickups = [];
      });
  }

  statusClass(status: string): string {
    const normalized = this.normalizeStatus(status);
    if (normalized === 'DELIVERED') {
      return 'status-delivered';
    }
    if (normalized === 'IN_TRANSIT') {
      return 'status-transit';
    }
    if (normalized === 'CANCELLED') {
      return 'status-cancelled';
    }
    return 'status-confirmed';
  }

  trackByHistoryEntry(_: number, entry: BookingHistoryEntry): number {
    return entry.id;
  }

  trackByShipment(_: number, shipment: TrackingResponse): string {
    return shipment.bookingId;
  }

  private buildStatusInsights(items: Array<{ label: string; count: number }>): StatusInsight[] {
    const total = items.reduce((sum, item) => sum + item.count, 0);
    if (total === 0) {
      return items.map((item) => ({ ...item, percent: 0 }));
    }

    return items.map((item) => ({
      ...item,
      percent: Math.round((item.count / total) * 100)
    }));
  }

  private normalizeStatus(status: string | null | undefined): string {
    const value = (status ?? '').trim().toUpperCase();
    if (value === 'SHIPPED') {
      return 'CONFIRMED';
    }
    if (value === 'PICKED_UP') {
      return 'IN_TRANSIT';
    }
    if (value === 'RETURNED') {
      return 'CANCELLED';
    }
    return value;
  }

  private isSameDay(value: string | null | undefined, referenceDate: Date): boolean {
    if (!value) {
      return false;
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return false;
    }

    return (
      date.getFullYear() === referenceDate.getFullYear() &&
      date.getMonth() === referenceDate.getMonth() &&
      date.getDate() === referenceDate.getDate()
    );
  }

  private formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount);
  }

  private emptyPage<T>(page: number, size: number): PageResponse<T> {
    return {
      content: [],
      page,
      size,
      totalElements: 0,
      totalPages: 0,
      last: true
    };
  }
}
