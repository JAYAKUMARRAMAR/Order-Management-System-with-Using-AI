import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Inventory as InventoryModel } from '../../models/inventory';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {

  private readonly apiUrl =`${environment.apiUrl}/inventory`;
  constructor(private http: HttpClient) {}

  getInventory(): Observable<InventoryModel[]> {
    return this.http.get<InventoryModel[]>(this.apiUrl);}

  getInventoryByProductId(productId: number
   ): Observable<InventoryModel> {
    return this.http.get<InventoryModel>(
      `${this.apiUrl}/${productId}`
    );
  }
}