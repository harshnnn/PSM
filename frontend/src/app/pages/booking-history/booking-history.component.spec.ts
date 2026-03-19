import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Router } from '@angular/router';
import { BookingHistoryComponent } from './booking-history.component';
import { BookingHistoryApiService } from '../../services/booking-history-api.service';
import { SessionService } from '../../services/session.service';

describe('BookingHistoryComponent', () => {
  let fixture: ComponentFixture<BookingHistoryComponent>;

  const historyApiMock = {
    getCustomerHistory: jasmine.createSpy('getCustomerHistory'),
    getOfficerHistory: jasmine.createSpy('getOfficerHistory')
  };

  const sessionServiceMock = {
    get: jasmine.createSpy('get').and.returnValue({ username: 'cust-1', role: 'CUSTOMER' as const }),
    clear: jasmine.createSpy('clear')
  };

  const routerMock = {
    navigate: jasmine.createSpy('navigate')
  };

  beforeEach(async () => {
    historyApiMock.getCustomerHistory.calls.reset();
    historyApiMock.getOfficerHistory.calls.reset();
    historyApiMock.getCustomerHistory.and.returnValue(
      of({
        content: [
          {
            id: 1,
            customerId: 'cust-1',
            bookingId: 'BKG-1',
            trackingBookingId: '123456789012',
            bookingDate: '2026-03-19T09:00:00',
            receiverName: 'Receiver',
            deliveredAddress: 'Addr',
            amount: 120,
            status: 'CONFIRMED',
            trackingStatus: 'PICKED_UP'
          }
        ],
        page: 0,
        size: 5,
        totalElements: 1,
        totalPages: 1,
        last: true
      })
    );

    await TestBed.configureTestingModule({
      imports: [BookingHistoryComponent],
      providers: [
        { provide: BookingHistoryApiService, useValue: historyApiMock },
        { provide: SessionService, useValue: sessionServiceMock },
        { provide: Router, useValue: routerMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BookingHistoryComponent);
    fixture.detectChanges();
  });

  it('shows trackingStatus in status column when available', () => {
    const statusCell: HTMLElement | null = fixture.nativeElement.querySelector('tbody tr td:last-child .pill');
    expect(statusCell?.textContent?.trim()).toBe('PICKED_UP');
  });

  it('falls back to status when trackingStatus is missing', () => {
    historyApiMock.getCustomerHistory.and.returnValue(
      of({
        content: [
          {
            id: 2,
            customerId: 'cust-1',
            bookingId: 'BKG-2',
            trackingBookingId: null,
            bookingDate: '2026-03-19T09:00:00',
            receiverName: 'Receiver 2',
            deliveredAddress: 'Addr 2',
            amount: 80,
            status: 'IN_TRANSIT'
          }
        ],
        page: 0,
        size: 5,
        totalElements: 1,
        totalPages: 1,
        last: true
      })
    );

    fixture.componentInstance.search();
    fixture.detectChanges();

    const statusCell: HTMLElement | null = fixture.nativeElement.querySelector('tbody tr td:last-child .pill');
    expect(statusCell?.textContent?.trim()).toBe('IN_TRANSIT');
  });
});
