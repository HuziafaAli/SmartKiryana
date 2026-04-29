package controller;

import service.InventoryService;
import model.InventoryItem;
import model.ProductCategory;

import java.util.List;

public class InventoryController {

    private InventoryService inventoryService;

    public InventoryController(InventoryService IS) {
        inventoryService = IS;
    }

    public boolean addCategory(String name) {
        boolean success = inventoryService.addCategory(name);
        if (success) {
            System.out.println("Product Category Added.");
        } else {
            System.out.println("Failed to add category. Invalid name.");
        }
        return success;
    }

    public boolean updateCategory(int id, String newName) {
        boolean success = inventoryService.updateCategory(id, newName);
        if (success) {
            System.out.println("Product Category Updated.");
        } else {
            System.out.println("Failed to update category. Invalid name or ID not found.");
        }
        return success;
    }

    public boolean deleteCategory(int id) {
        boolean success = inventoryService.deleteCategory(id);
        if (success) {
            System.out.println("Product Category Deleted.");
        } else {
            System.out.println("Failed to delete category. It may not exist or is in use by products.");
        }
        return success;
    }

    public List<ProductCategory> getAllCategories() {
        return inventoryService.getAllCategories();
    }

    public boolean addProduct(String barcode, String name, int categoryId,
            double price, double costPrice, int minStock, int maxStock) {
        boolean success = inventoryService.addProduct(barcode, name, categoryId, price, costPrice, minStock,
                maxStock);
        if (success) {
            System.out.println("Product Added.");
        } else {
            System.out.println("Product Category Not Found.");
        }
        return success;
    }

    public boolean updateProduct(String barcode, String newName, double newPrice, double newCostPrice) {
        boolean success = inventoryService.updateProduct(barcode, newName, newPrice, newCostPrice);
        if (success) {
            System.out.println("Product '" + newName + "' updated successfully.");
        } else {
            System.out.println("Product not found. Invalid barcode.");
        }
        return success;
    }

    public boolean deleteProduct(String barcode) {
        boolean success = inventoryService.deleteProduct(barcode);
        if (success) {
            System.out.println("Product deleted successfully.");
        } else {
            System.out.println("Product not found. Invalid barcode.");
        }
        return success;
    }

    public boolean addStock(String barcode, int quantityToAdd) {
        boolean success = inventoryService.addStock(barcode, quantityToAdd);
        if (success) {
            InventoryItem itemExist = inventoryService.isProductExists(barcode);
            if (itemExist != null) {
                System.out.println(itemExist.getProduct().getName() + " quantity Added.");
            }
        } else {
            System.out.println("Invalid Barcode or Quantity.");
        }
        return success;
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryService.getLowStockItems();
    }

    public List<InventoryItem> getAllInventoryItems() {
        return inventoryService.getAllInventoryItems();
    }

    public void checkAllStockLevels() {
        inventoryService.checkAllStockLevels();
        System.out.println("Stock check complete.");
    }
}
