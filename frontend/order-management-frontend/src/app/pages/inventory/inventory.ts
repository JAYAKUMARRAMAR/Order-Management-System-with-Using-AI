import { Component, DestroyRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { InventoryService } from '../../core/services/inventory';
import { Inventory } from '../../models/inventory';

@Component({

  selector: 'app-inventory',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './inventory.html',
  styleUrl: './inventory.css'
})
export class InventoryComponent
  implements OnInit {

  inventory: Inventory[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private inventoryService: InventoryService,
    private destroyRef: DestroyRef
  ) {}

  ngOnInit(): void { this.loadInventory(); }

  loadInventory(): void {

    this.loading = true;
    this.inventoryService.getInventory()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data: Inventory[]) => {
          this.inventory = data;
          this.loading = false;
        },
        error: (error: any) => {
          console.error(error);
          this.errorMessage = 'Unable to load inventory';
          this.loading = false;
        }
      });
  }
}