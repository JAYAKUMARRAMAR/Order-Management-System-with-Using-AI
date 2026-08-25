import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { OrdersComponent } from './orders';
import { OrderService } from '../../core/services/order';

describe('OrdersComponent', () => {
  let component: OrdersComponent;
  let fixture: ComponentFixture<OrdersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdersComponent],
      providers: [
        {
          provide: OrderService,
          useValue: { getOrders: () => of([]) },
        },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    fixture = TestBed.createComponent(OrdersComponent);
    component = fixture.componentInstance;

    expect(component).toBeTruthy();
  });

  it('renders every item in an order', () => {
    fixture = TestBed.createComponent(OrdersComponent);
    component = fixture.componentInstance;
    component.loadOrders = () => {};
    component.loading = false;
    component.orders = [
      {
        id: 1,
        customerId: 10,
        status: 'NEW',
        totalAmount: 100,
        items: [1, 2, 3, 4].map((itemId) => ({
          itemId,
          productId: itemId,
          orderId: 1,
          quantity: 1,
          price: 25,
          subtotal: 25,
        })),
      },
    ];

    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('tbody tr')).toHaveLength(4);
  });
});
