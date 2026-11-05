import { Component, DestroyRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ProductService } from '../../core/services/product';
import { OrderService } from '../../core/services/order';
import { CreateOrderRequest } from '../../models/order';
import { Product } from '../../models/product';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.html',
  styleUrl: './products.css'
})
export class ProductsComponent implements OnInit {

  products: Product[] = [];
  loading = true;
  errorMessage = '';
  selectedProduct: Product | null = null;
  quantity = 1;
  ordering = false;
  orderError = '';
  successMessage = '';
  readonly customerId = 1001;
  readonly fallbackImageUrl =
    'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=960&q=85';

  constructor(
    private productService: ProductService,
    private orderService: OrderService,
    private destroyRef: DestroyRef
  ) {}

  ngOnInit(): void {
    console.log('ProductsComponent initialized');

    this.loadProducts();
  }

  loadProducts(): void {

    this.loading = true;

    this.productService.getProducts().subscribe({

      next: (response) => {
        this.products = response.content ?? [];
        this.loading = false;
      },

      error: (error) => {

        console.error('PRODUCT API ERROR:', error);

        this.errorMessage =
          'Unable to load products.';

        this.loading = false;
      }
    });
  }

  showDetails(product: Product): void {
    this.selectedProduct = product;
    this.quantity = 1;
    this.orderError = '';
  }

  closeDetails(): void {
    if (!this.ordering) {
      this.selectedProduct = null;
      this.orderError = '';
    }
  }

  placeOrder(): void {
    if (!this.selectedProduct || this.ordering ||
        !Number.isInteger(this.quantity) || this.quantity < 1) {
      return;
    }

    const request: CreateOrderRequest = {
      customerId: this.customerId,
      items: [{
        productId: this.selectedProduct.id,
        quantity: this.quantity
      }]
    };

    this.ordering = true;
    this.orderError = '';
    this.successMessage = '';

    this.orderService.createOrder(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (order) => {
          this.successMessage =
            `Order #${order.id} placed successfully.`;
          this.selectedProduct = null;
          this.ordering = false;
        },
        error: (error) => {
          console.error('ORDER API ERROR:', error);
          this.orderError =
            error?.error?.message || 'Unable to place this order.';
          this.ordering = false;
        }
      });
  }
}