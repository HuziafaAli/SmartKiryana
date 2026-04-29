package observer;

import model.InventoryItem;

public interface StockObserver {
    void onStockLow(InventoryItem item);
    void onStockOver(InventoryItem item);
    void onStockNormal(InventoryItem item);
}
