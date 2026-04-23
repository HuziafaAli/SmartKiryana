package model;

public class InventoryItem {

    private int inventoryId;
    private Product product;
    private int stockQuantity;
    private int minStockThreshold;
    private int maxStockThreshold;

    public InventoryItem() {
    }

    public InventoryItem(int inventoryId, Product product, int stockQuantity, int minStockThreshold,
            int maxStockThreshold) {
        this.inventoryId = inventoryId;
        this.product = product;
        this.stockQuantity = stockQuantity;
        this.minStockThreshold = minStockThreshold;
        this.maxStockThreshold = maxStockThreshold;
    }

    public boolean isLowStock() {
        return stockQuantity <= minStockThreshold;
    }

    public boolean isOverStock() {
        return stockQuantity >= maxStockThreshold;
    }

    public void addStockQuantity(int amount) {
        this.stockQuantity += amount;
    }

    public boolean reduceStockQuantity(int amount) {
        if (this.stockQuantity >= amount) {
            this.stockQuantity -= amount;
            return true;
        }
        return false;
    }

    // Getters & Setters

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getMinStockThreshold() {
        return minStockThreshold;
    }

    public void setMinStockThreshold(int minStockThreshold) {
        this.minStockThreshold = minStockThreshold;
    }

    public int getMaxStockThreshold() {
        return maxStockThreshold;
    }

    public void setMaxStockThreshold(int maxStockThreshold) {
        this.maxStockThreshold = maxStockThreshold;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "product=" + (product != null ? product.getName() : "None") +
                ", stockQuantity=" + stockQuantity +
                '}';
    }
}
