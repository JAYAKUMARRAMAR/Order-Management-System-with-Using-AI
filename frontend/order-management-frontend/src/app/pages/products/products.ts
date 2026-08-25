import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ProductService } from '../../core/services/product';
import { Product } from '../../models/product';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './products.html',
  styleUrl: './products.css'
})
export class ProductsComponent implements OnInit {

  products: Product[] = [];

  loading = true;

  errorMessage = '';

  constructor(
    private productService: ProductService
  ) {}

  ngOnInit(): void {
    console.log('ProductsComponent initialized');

    this.loadProducts();
  }

  loadProducts(): void {

    this.loading = true;

    this.productService.getProducts().subscribe({

      next: (response) => {

        console.log('PRODUCT API RESPONSE:', response);

        this.products = response.content ?? [];

        console.log('PRODUCTS:', this.products);

        this.loading = false;
      },

      error: (error) => {

        console.error('PRODUCT API ERROR:', error);

        this.errorMessage =
          'Unable to load products.';

        this.loading = false;
      }
      ,complete: () => {

        console.log('Product API request completed');

        this.loading = false;
      }
    });
  }
}