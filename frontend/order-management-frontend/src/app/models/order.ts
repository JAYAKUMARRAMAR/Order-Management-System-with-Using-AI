export interface Order {
  id: number;
  customerId: number;
  status: string;
  totalAmount: number;
  items: OrderItem[];
}

export interface OrderItem {
  itemId: number;
  productId: number;
  orderId: number;
  quantity: number;
  price: number;
  subtotal: number;
}

