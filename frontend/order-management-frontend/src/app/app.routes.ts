import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login';
import { DashboardComponent } from './pages/dashboard/dashboard';
import { ProductsComponent } from './pages/products/products';
import { InventoryComponent } from './pages/inventory/inventory';
import { OrdersComponent } from './pages/orders/orders';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  {
    path: 'login',
    component: LoginComponent
  },

  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [ authGuard ]
  },

  {
    path: 'products',
    component: ProductsComponent,
    canActivate: [ authGuard ]
  },

  {
    path: 'inventory',
    component: InventoryComponent,
    canActivate: [ authGuard]
  },

  {
    path: 'orders',
    component: OrdersComponent,
    canActivate: [ authGuard ]
  },

  {
    path: '**',
    redirectTo: 'login'
  }

];