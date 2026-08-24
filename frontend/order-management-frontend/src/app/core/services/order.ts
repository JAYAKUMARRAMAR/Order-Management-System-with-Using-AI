import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Order } from '../../models/order';


@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private readonly apiUrl =`${environment.apiUrl}/orders`;
  constructor(private http: HttpClient) {}

  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.apiUrl); }

  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/${id}`);}

  createOrder(order: Order): Observable<Order> {
    return this.http.post<Order>(this.apiUrl,order);}
}