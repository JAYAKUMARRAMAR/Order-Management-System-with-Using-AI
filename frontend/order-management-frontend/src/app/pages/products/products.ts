import { Component, DestroyRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ProductService } from '../../core/services/product';
import { Product } from '../../models/product';

@Component({

  selector: 'app-products',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './products.html',
  styleUrl: './products.css'
})
export class ProductsComponent
  implements OnInit {

  products: Product[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private productService: ProductService,
    private destroyRef: DestroyRef
  ) {}

  ngOnInit(): void { this.loadProducts(); }

  loadProducts(): void {
    this.loading = true;
    this.productService
      .getProducts()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data: Product[]) => {
          this.products = data;
          this.loading = false;
        },
        error: (error: any) => {
          console.error(error);
          this.errorMessage =
            'Unable to load products';
          this.loading = false;
        }
      });
  }
}