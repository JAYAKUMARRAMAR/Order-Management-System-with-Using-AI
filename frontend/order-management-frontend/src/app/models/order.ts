export interface Order {

  id: number;
  productId: number;
  quantity: number;
  status?: string;
}

// export interface OrderItem {
//   productId: number;
//   quantity: number;
//   price: number;
//   subtotal: number;
// }

// export interface Order {
//   id: number;
//   customerId: number;
//   status: string;
//   totalAmount: number;
//   items: OrderItem[];
// }

