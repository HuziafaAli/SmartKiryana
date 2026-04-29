package observer;

import model.InventoryItem;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class StockAlert implements StockObserver {

    private Map<String, InventoryItem> pendingAlerts;

    public StockAlert() {
        this.pendingAlerts = new HashMap<>();
    }

    @Override
    public void onStockLow(InventoryItem item) {
        if (!pendingAlerts.containsKey(item.getProduct().getBarcode()) || 
            pendingAlerts.get(item.getProduct().getBarcode()).getStockQuantity() != item.getStockQuantity()) {
            
            pendingAlerts.put(item.getProduct().getBarcode(), item);
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
        return new HashSet<>(pendingAlerts.values());
    }

    public void clearAlerts() {
        pendingAlerts.clear();
        System.out.println("All alerts have been acknowledged and cleared.");
    }

    @Override
    public void onStockOver(InventoryItem item) {
        if (!pendingAlerts.containsKey(item.getProduct().getBarcode()) || 
            pendingAlerts.get(item.getProduct().getBarcode()).getStockQuantity() != item.getStockQuantity()) {
            
            pendingAlerts.put(item.getProduct().getBarcode(), item);
            System.out.println("--------------------------------------------------");
            System.out.println("  OVER STOCK ALERT ");
            System.out.println("Item: " + item.getProduct().getName());
            System.out.println(
                    "Current Stock: " + item.getStockQuantity() + " (Maximum: " + item.getMaxStockThreshold() + ")");
            System.out.println("Action Required: Consider halting reorders or putting item on sale.");
            System.out.println("--------------------------------------------------");
        }
    }

    @Override
    public void onStockNormal(InventoryItem item) {
        if (pendingAlerts.remove(item.getProduct().getBarcode()) != null) {
            System.out.println("--------------------------------------------------");
            System.out.println("  STOCK RETURNED TO NORMAL ");
            System.out.println("Item: " + item.getProduct().getName());
            System.out.println("New Stock: " + item.getStockQuantity() + " - Alert cleared.");
            System.out.println("--------------------------------------------------");
        }
    }
}
