import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProductPage } from '../../models/product-page';
import { Product } from '../../models/product';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private readonly apiUrl =`${environment.apiUrl}/products`;

  constructor(private http: HttpClient) {}

  
   getProducts(): Observable<ProductPage> {

    const params = new HttpParams()
      .set('name', 'laptop')
      .set('page', 0)
      .set('size', 10)
      .set('sortBy', 'id')
      .set('direction', 'asc');

    return this.http.get<ProductPage>(
      `${this.apiUrl}/search`,{params}
    );
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);}

}