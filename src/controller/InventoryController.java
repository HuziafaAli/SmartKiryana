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

    public void addCategory(int id, String name) {
        inventoryService.addCategory(id, name);
        System.out.println("Product Category Added.");
    }

    public List<ProductCategory> getAllCategories() {
        return inventoryService.getAllCategories();
    }

    public void addProduct(int productId, String barcode, String name, int categoryId,
            double price, double costPrice, int minStock, int maxStock) {

        boolean success = inventoryService.addProduct(productId, barcode, name, categoryId, price, costPrice, minStock,
                maxStock);
        if (success) {
            System.out.println("Product Added.");
        } else {
            System.out.println("Product Category Not Found.");
        }

    }

    public void addStock(String barcode, int quantityToAdd) {

        boolean success = inventoryService.addStock(barcode, quantityToAdd);
        InventoryItem itemExist = inventoryService.isProductExists(barcode);
        if (success) {
            System.out.println(itemExist.getProduct().getName() + " quantity Added.");
        } else {
            System.out.println("Invalid Barcode.");
        }
    }

    public void reduceStock(String barcode, int quantityToReduce) {
        InventoryItem itemExist = inventoryService.isProductExists(barcode);
        boolean success = inventoryService.reduceStock(barcode, quantityToReduce);

        if (success) {
            System.out.println(itemExist.getProduct().getName() + " quantity reduced.");
        } else if (itemExist != null) {
            System.out.println(itemExist.getProduct().getName() + " does not have enough stock.");
        } else {
            System.out.println("Invalid Barcode.");
        }
    }

    public InventoryItem isProductExists(String barcode) {
        return inventoryService.isProductExists(barcode);
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryService.getLowStockItems();
    }
}
