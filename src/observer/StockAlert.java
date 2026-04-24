package observer;

import model.InventoryItem;
import java.util.ArrayList;
import java.util.List;

public class StockAlert implements StockObserver {

    private List<String> pendingAlerts;

    public StockAlert() {
        this.pendingAlerts = new ArrayList<>();
    }

    @Override
    public void onStockLow(InventoryItem item) {
        String alertMessage = "ALERT: '" + item.getProduct().getName() 
                + "' stock is " + item.getStockQuantity() 
                + " (Minimum: " + item.getMinStockThreshold() + "). Please restock.";
        pendingAlerts.add(alertMessage);
        System.out.println("--------------------------------------------------");
        System.out.println("  URGENT STOCK ALERT ");
        System.out.println("Item: " + item.getProduct().getName());
        System.out.println("Current Stock: " + item.getStockQuantity() + " (Minimum: " + item.getMinStockThreshold() + ")");
        System.out.println("Action Required: Please order more inventory immediately.");
        System.out.println("--------------------------------------------------");
    }

    public List<String> getPendingAlerts() {
        return pendingAlerts;
    }

    public void clearAlerts() {
        pendingAlerts.clear();
        System.out.println("All alerts have been acknowledged and cleared.");
    }
}
