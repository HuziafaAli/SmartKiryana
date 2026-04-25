package service;

import model.InventoryItem;
import model.Product;
import model.ProductCategory;
import observer.StockObserver;

import java.util.ArrayList;
import java.util.List;
import util.Validator;

public class InventoryService {
    // Runtime DB
    private List<InventoryItem> inventoryDatabase;
    private List<ProductCategory> categoryDatabase;
    private List<StockObserver> observers;

    public InventoryService() {
        inventoryDatabase = new ArrayList<>();
        categoryDatabase = new ArrayList<>();
        observers = new ArrayList<>();

        // For Testing
        ProductCategory tempCat = new ProductCategory(1, "Dairy");
        ProductCategory tempCat2 = new ProductCategory(1, "Snacks");

        categoryDatabase.add(tempCat);
        categoryDatabase.add(tempCat2);
    }

    public boolean addCategory(int id, String name) {
        if (!Validator.isNotEmpty(name))
            return false;
        for (ProductCategory c : categoryDatabase) {
            if (c.getCategoryId() == id)
                return false; // Duplicate ID
        }
        categoryDatabase.add(new ProductCategory(id, name));
        return true;
    }

    public boolean updateCategory(int id, String newName) {
        if (!Validator.isNotEmpty(newName))
            return false;
        for (ProductCategory c : categoryDatabase) {
            if (c.getCategoryId() == id) {
                c.setCategoryName(newName);
                return true;
            }
        }
        return false;
    }

    public boolean deleteCategory(int id) {
        for (int i = 0; i < categoryDatabase.size(); i++) {
            if (categoryDatabase.get(i).getCategoryId() == id) {
                for (InventoryItem item : inventoryDatabase) {
                    if (item.getProduct().getCategory().getCategoryId() == id) {
                        return false;
                    }
                }
                categoryDatabase.remove(i);
                return true;
            }
        }
        return false;
    }

    public List<ProductCategory> getAllCategories() {
        return categoryDatabase;
    }

    public boolean addProduct(int productId, String barcode, String name, int categoryId,
            double price, double costPrice, int minStock, int maxStock) {

        if (!Validator.isValidBarcode(barcode) || !Validator.isNotEmpty(name)
                || !Validator.isPositiveAmount(price) || !Validator.isPositiveAmount(costPrice)) {
            return false;
        }

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

    public boolean updateProduct(String barcode, String newName, double newPrice, double newCostPrice) {
        if (!Validator.isValidBarcode(barcode) || !Validator.isNotEmpty(newName)
                || !Validator.isPositiveAmount(newPrice) || !Validator.isPositiveAmount(newCostPrice)) {
            return false;
        }

        InventoryItem itemExist = isProductExists(barcode);
        if (itemExist == null) {
            return false;
        }

        itemExist.getProduct().setName(newName);
        itemExist.getProduct().setPrice(newPrice);
        itemExist.getProduct().setCostPrice(newCostPrice);
        return true;
    }

    public boolean deleteProduct(String barcode) {
        if (!Validator.isValidBarcode(barcode))
            return false;

        InventoryItem itemExist = isProductExists(barcode);
        if (itemExist == null) {
            return false;
        }

        inventoryDatabase.remove(itemExist);
        return true;
    }

    public boolean addStock(String barcode, int quantityToAdd) {
        if (!Validator.isValidBarcode(barcode) || !Validator.isPositiveQuantity(quantityToAdd)) {
            return false;
        }

        InventoryItem itemExist = isProductExists(barcode);
        if (itemExist == null) {
            return false;
        }

        itemExist.addStockQuantity(quantityToAdd);
        return true;
    }

    public boolean reduceStock(String barcode, int quantityToReduce) {
        if (!Validator.isValidBarcode(barcode) || !Validator.isPositiveQuantity(quantityToReduce)) {
            return false;
        }

        InventoryItem itemExist = isProductExists(barcode);
        if (itemExist == null) {
            return false;
        }

        if (!itemExist.reduceStockQuantity(quantityToReduce)) {
            return false;
        }

        // OBSERVER TRIGGER: Notify all observers if stock is now low
        if (itemExist.isLowStock()) {
            for (StockObserver obs : observers) {
                obs.onStockLow(itemExist);
            }
        }

        return true;
    }

    public void addObserver(StockObserver observer) {
        observers.add(observer);
    }

    public InventoryItem isProductExists(String barcode) {
        if (!Validator.isValidBarcode(barcode))
            return null;

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

    public List<InventoryItem> getAllInventoryItems() {
        return inventoryDatabase;
    }

    public void checkAllStockLevels() {
        for (InventoryItem item : inventoryDatabase) {
            if (item.isLowStock()) {
                for (StockObserver obs : observers) {
                    obs.onStockLow(item);
                }
            }
        }
    }
}
