package service;

import model.InventoryItem;
import model.Product;
import model.ProductCategory;

import java.util.ArrayList;
import java.util.List;

public class InventoryService {
    // Runtime DB
    private List<InventoryItem> inventoryDatabase;
    private List<ProductCategory> categoryDatabase;

    public InventoryService() {
        inventoryDatabase = new ArrayList<>();
        categoryDatabase = new ArrayList<>();

        // For Testing
        ProductCategory tempCat = new ProductCategory(1, "Dairy");
        ProductCategory tempCat2 = new ProductCategory(1, "Snacks");

        categoryDatabase.add(tempCat);
        categoryDatabase.add(tempCat2);
    }

    public void addCategory(int id, String name) {
        categoryDatabase.add(new ProductCategory(id, name));
    }

    public List<ProductCategory> getAllCategories() {
        return categoryDatabase;
    }

    public boolean addProduct(int productId, String barcode, String name, int categoryId,
            double price, double costPrice, int minStock, int maxStock) {

        for (ProductCategory c : categoryDatabase) {
            if (c.getCategoryId() == categoryId) {
                ProductCategory category = new ProductCategory(categoryId, c.getCategoryName());
                Product p = new Product(productId, barcode, name, category, price, costPrice);
                int newInvId = inventoryDatabase.size() + 1;
                InventoryItem i = new InventoryItem(newInvId, p, 0, minStock, maxStock);
                inventoryDatabase.add(i);
                return true;
            }
        }
        return false;
    }

    public boolean addStock(String barcode, int quantityToAdd) {

        InventoryItem itemExist = isProductExists(barcode);
        if (itemExist == null) {
            return false;
        }

        itemExist.addStockQuantity(quantityToAdd);
        return true;
    }

    public boolean reduceStock(String barcode, int quantityToReduce) {

        InventoryItem itemExist = isProductExists(barcode);
        if (itemExist == null) {
            return false;
        }

        if (!itemExist.reduceStockQuantity(quantityToReduce)) {
            return false;
        }

        return true;
    }

    public InventoryItem isProductExists(String barcode) {
        for (InventoryItem i : inventoryDatabase) {
            if (i.getProduct().getBarcode().equals(barcode)) {
                return i;
            }
        }
        return null;
    }

    public List<InventoryItem> getLowStockItems() {
        List<InventoryItem> lowStockItems = new ArrayList<>();
        for (InventoryItem i : inventoryDatabase) {
            if (i.isLowStock()) {
                lowStockItems.add(i);
            }
        }
        return lowStockItems;
    }
}
