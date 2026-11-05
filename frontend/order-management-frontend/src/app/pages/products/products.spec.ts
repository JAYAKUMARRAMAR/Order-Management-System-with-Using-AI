import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { ProductsComponent } from './products';
import { ProductService } from '../../core/services/product';
import { OrderService } from '../../core/services/order';

describe('ProductsComponent', () => {
  let component: ProductsComponent;
  let fixture: ComponentFixture<ProductsComponent>;
  let createOrder: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    createOrder = vi.fn().mockReturnValue(of({
      id: 8,
      customerId: 1001,
      status: 'CONFIRMED',
      totalAmount: 1200,
      items: []
    }));

    await TestBed.configureTestingModule({
      imports: [ProductsComponent],
      providers: [
        {
          provide: ProductService,
          useValue: {
            getProducts: () => of({ content: [] })
          }
        },
        {
          provide: OrderService,
          useValue: { createOrder }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('opens product details when the photo is clicked', () => {
    component.products = [{
      id: 5,
      name: 'Laptop',
      description: 'Lightweight laptop',
      price: 1200
    }];
    fixture.detectChanges();

    fixture.nativeElement.querySelector('.product-photo-button').click();
    fixture.detectChanges();

    expect(component.selectedProduct?.id).toBe(5);
    expect(fixture.nativeElement.querySelector('[role="dialog"]')).toBeTruthy();
  });

  it('places an order for the selected product and quantity', () => {
    const product = { id: 5, name: 'Laptop', price: 1200 };
    component.showDetails(product);
    component.quantity = 2;

    component.placeOrder();

    expect(createOrder).toHaveBeenCalledWith({
      customerId: 1001,
      items: [{ productId: 5, quantity: 2 }]
    });
    expect(component.successMessage).toBe('Order #8 placed successfully.');
    expect(component.selectedProduct).toBeNull();
  });

  it('shows an error when the order request fails', () => {
    createOrder.mockReturnValue(throwError(() => new Error('Request failed')));
    component.showDetails({ id: 5, name: 'Laptop', price: 1200 });

    component.placeOrder();

    expect(component.orderError).toBe('Unable to place this order.');
    expect(component.selectedProduct?.id).toBe(5);
  });
});
