package observer;

import model.InventoryItem;
import java.util.HashSet;
import java.util.Set;

public class StockAlert implements StockObserver {

    private Set<InventoryItem> pendingAlerts;

    public StockAlert() {
        this.pendingAlerts = new HashSet<>();
    }

    @Override
    public void onStockLow(InventoryItem item) {
        if (pendingAlerts.add(item)) {
            System.out.println("--------------------------------------------------");
            System.out.println("  URGENT STOCK ALERT ");
            System.out.println("Item: " + item.getProduct().getName());
            System.out.println(
                    "Current Stock: " + item.getStockQuantity() + " (Minimum: " + item.getMinStockThreshold() + ")");
            System.out.println("Action Required: Please order more inventory immediately.");
            System.out.println("--------------------------------------------------");
        }
    }

    public Set<InventoryItem> getPendingAlerts() {
        return pendingAlerts;
    }

    public void clearAlerts() {
        pendingAlerts.clear();
        System.out.println("All alerts have been acknowledged and cleared.");
    }
}
