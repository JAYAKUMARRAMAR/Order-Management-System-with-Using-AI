import { Component, DestroyRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { OrderService } from '../../core/services/order';
import { Order } from '../../models/order';

@Component({

  selector: 'app-orders',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './orders.html',
  styleUrl: './orders.css'
})
export class OrdersComponent
  implements OnInit {

  orders: Order[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private orderService: OrderService,
    private destroyRef: DestroyRef
  ) {}

  ngOnInit(): void { this.loadOrders(); }

  loadOrders(): void {
    this.loading = true;
    this.orderService.getOrders()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data: Order[]) => {
          this.orders = data;
          this.loading = false;
        },
        error: (error: any) => {
          console.error(error);
          this.errorMessage =
            'Unable to load orders';
          this.loading = false;
        }
      });
  }
}