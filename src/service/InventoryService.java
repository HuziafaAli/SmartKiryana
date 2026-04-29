package service;

import model.InventoryItem;
import model.Product;
import model.ProductCategory;
import observer.StockObserver;
import dao.ProductCategoryDAO;
import dao.InventoryItemDAO;

import java.util.ArrayList;
import java.util.List;
import util.Validator;

public class InventoryService {

    private ProductCategoryDAO categoryDAO;
    private InventoryItemDAO inventoryDAO;
    private List<StockObserver> observers;

    public InventoryService(ProductCategoryDAO categoryDAO, InventoryItemDAO inventoryDAO) {
        this.categoryDAO = categoryDAO;
        this.inventoryDAO = inventoryDAO;
        this.observers = new ArrayList<>();
    }

    // Category CRUD

    public boolean addCategory(String name) {
        if (!Validator.isNotEmpty(name))
            return false;

        return categoryDAO.save(new ProductCategory(0, name));
    }

    public boolean updateCategory(int id, String newName) {
        if (!Validator.isNotEmpty(newName))
            return false;

        List<ProductCategory> allCategories = categoryDAO.findAll();
        for (ProductCategory c : allCategories) {
            if (c.getCategoryId() == id) {
                c.setCategoryName(newName);
                return categoryDAO.update(c);
            }
        }
        return false;
    }

    public boolean deleteCategory(int id) {
        List<ProductCategory> allCategories = categoryDAO.findAll();
        List<InventoryItem> allItems = inventoryDAO.findAll();

        boolean found = false;
        for (ProductCategory c : allCategories) {
            if (c.getCategoryId() == id) {
                found = true;
                break;
            }
        }
        if (!found)
            return false;

        for (InventoryItem item : allItems) {
            if (item.getProduct().getCategory().getCategoryId() == id) {
                return false;
            }
        }

        return categoryDAO.delete(id);
    }

    public List<ProductCategory> getAllCategories() {
        return categoryDAO.findAll();
    }

    // Product CRUD

    public boolean addProduct(String barcode, String name, int categoryId,
            double price, double costPrice, int minStock, int maxStock) {

        if (!Validator.isValidBarcode(barcode) || !Validator.isNotEmpty(name)
                || !Validator.isPositiveAmount(price) || !Validator.isPositiveAmount(costPrice)) {
            return false;
        }

        // Check if barcode already exists
        if (inventoryDAO.findByBarcode(barcode) != null) {
            return false;
        }

        // Find the category
        List<ProductCategory> allCategories = categoryDAO.findAll();
        for (ProductCategory c : allCategories) {
            if (c.getCategoryId() == categoryId) {
                ProductCategory category = new ProductCategory(categoryId, c.getCategoryName());
                Product p = new Product(0, barcode, name, category, price, costPrice);
                InventoryItem item = new InventoryItem(0, p, 0, minStock, maxStock);
                return inventoryDAO.save(item);
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

        return inventoryDAO.updateProduct(barcode, newName, newPrice, newCostPrice);
    }

    public boolean deleteProduct(String barcode) {
        if (!Validator.isValidBarcode(barcode))
            return false;

        InventoryItem itemExist = isProductExists(barcode);
        if (itemExist == null) {
            return false;
        }

        return inventoryDAO.delete(barcode);
    }

    // Stock Management

    public boolean addStock(String barcode, int quantityToAdd) {
        if (!Validator.isValidBarcode(barcode) || !Validator.isPositiveQuantity(quantityToAdd)) {
            return false;
        }

        InventoryItem itemExist = isProductExists(barcode);
        if (itemExist == null) {
            return false;
        }

        // Business logic: calculate new quantity in Java
        int newQuantity = itemExist.getStockQuantity() + quantityToAdd;
        boolean success = inventoryDAO.updateStock(barcode, newQuantity);

        if (success) {
            InventoryItem updatedItem = isProductExists(barcode);
            if (updatedItem.isOverStock()) {
                for (StockObserver obs : observers) {
                    obs.onStockOver(updatedItem);
                }
            } else if (!updatedItem.isLowStock()) {
                for (StockObserver obs : observers) {
                    obs.onStockNormal(updatedItem);
                }
            }
        }
        return success;
    }

    public boolean restoreDeletedStock(String barcode, int quantity) {
        if (!Validator.isValidBarcode(barcode) || !Validator.isPositiveQuantity(quantity)) {
            return false;
        }

        model.Product product = inventoryDAO.findProductByBarcode(barcode);
        if (product == null) {
            return false;
        }

        InventoryItem item = new InventoryItem(0, product, quantity, 10, 100);
        return inventoryDAO.insertInventoryOnly(item);
    }

    public boolean reduceStock(String barcode, int quantityToReduce) {
        if (!Validator.isValidBarcode(barcode) || !Validator.isPositiveQuantity(quantityToReduce)) {
            return false;
        }

        InventoryItem itemExist = isProductExists(barcode);
        if (itemExist == null) {
            return false;
        }

        // Business logic: check if enough stock exists
        if (itemExist.getStockQuantity() < quantityToReduce) {
            return false;
        }

        // Business logic: calculate new quantity in Java
        int newQuantity = itemExist.getStockQuantity() - quantityToReduce;
        boolean success = inventoryDAO.updateStock(barcode, newQuantity);

        // Observer trigger: notify if stock is now low or normal
        if (success) {
            InventoryItem updatedItem = isProductExists(barcode);
            if (updatedItem.isLowStock()) {
                for (StockObserver obs : observers) {
                    obs.onStockLow(updatedItem);
                }
            } else if (!updatedItem.isOverStock()) {
                for (StockObserver obs : observers) {
                    obs.onStockNormal(updatedItem);
                }
            }
        }

        return success;
    }

    // Observer Management

    public void addObserver(StockObserver observer) {
        observers.add(observer);
    }

    // Lookup & Queries

    public InventoryItem isProductExists(String barcode) {
        if (!Validator.isValidBarcode(barcode))
            return null;

        return inventoryDAO.findByBarcode(barcode);
    }

    public List<InventoryItem> getLowStockItems() {
        List<InventoryItem> allItems = inventoryDAO.findAll();
        List<InventoryItem> lowStockItems = new ArrayList<>();
        for (InventoryItem i : allItems) {
            if (i.isLowStock()) {
                lowStockItems.add(i);
            }
        }
        return lowStockItems;
    }

    public List<InventoryItem> getAllInventoryItems() {
        return inventoryDAO.findAll();
    }

    public void checkAllStockLevels() {
        List<InventoryItem> allItems = inventoryDAO.findAll();
        for (InventoryItem item : allItems) {
            if (item.isLowStock()) {
                for (StockObserver obs : observers) {
                    obs.onStockLow(item);
                }
            } else if (item.isOverStock()) {
                for (StockObserver obs : observers) {
                    obs.onStockOver(item);
                }
            } else {
                for (StockObserver obs : observers) {
                    obs.onStockNormal(item);
                }
            }
        }
    }
}
