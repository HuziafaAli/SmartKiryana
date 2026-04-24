package observer;

import model.InventoryItem;

public class StockAlert implements StockObserver {
    @Override
    public void onStockLow(InventoryItem item) {
        System.out.println("--------------------------------------------------");
        System.out.println("  URGENT STOCK ALERT ");
        System.out.println("Item: " + item.getProduct().getName());
        System.out.println("Current Stock: " + item.getStockQuantity() + " (Minimum: " + item.getMinStockThreshold() + ")");
        System.out.println("Action Required: Please order more inventory immediately.");
        System.out.println("--------------------------------------------------");
    }
}
