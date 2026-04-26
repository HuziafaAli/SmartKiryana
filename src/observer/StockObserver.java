package observer;

import model.InventoryItem;

public interface StockObserver {
    void onStockLow(InventoryItem item);
    void onStockRefilled(InventoryItem item);
}
