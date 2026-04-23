package controller;

import model.Bill;
import model.BillItem;
import model.User;
import model.InventoryItem;
import model.Product;
import model.ReturnItem;
import model.ReturnTransaction;

import java.util.ArrayList;
import java.util.List;

public class BillController {

    // Runtime Database
    private List<Bill> billDatabase;
    private List<ReturnTransaction> returnDatabase;

    private Bill currentBill;
    private InventoryController inventoryController;

    public BillController(InventoryController inventoryController) {
        this.billDatabase = new ArrayList<>();
        this.returnDatabase = new ArrayList<>();
        this.currentBill = null;
        this.inventoryController = inventoryController;
    }

    public void startNewBill(User cashier) {
        int billId = billDatabase.size() + 1;
        currentBill = new Bill(billId, cashier);
    }

    public void scanItem(String barcode, int quantity) {
        InventoryItem itemExist = inventoryController.isProductExists(barcode);

        if (itemExist == null) {
            System.out.println("Product does not exist");
            return;
        }

        BillItem existingBillItem = null;
        if (currentBill.getItems() != null) {
            for (BillItem b : currentBill.getItems()) {
                if (b.getProduct().getBarcode().equals(barcode)) {
                    existingBillItem = b;
                    break;
                }
            }
        }

        int totalQuantityRequested = quantity;
        if (existingBillItem != null) {
            totalQuantityRequested += existingBillItem.getQuantity();
        }

        if (!itemExist.checkStockQuantity(totalQuantityRequested)) {
            System.out.println("Not Enough stock , Stock = " + itemExist.getStockQuantity()
                    + " Quantity Req = " + totalQuantityRequested);
            return;
        }

        if (existingBillItem != null) {
            existingBillItem.setQuantity(totalQuantityRequested);
            currentBill.calculateTotal();
            System.out.println(
                    "Updated quantity of " + itemExist.getProduct().getName() + " to " + totalQuantityRequested);
        } else {
            Product p = itemExist.getProduct();
            int newID = currentBill.getItems().size() + 1;
            BillItem currentBillItem = new BillItem(newID, p, quantity, p.getPrice());
            currentBill.addItem(currentBillItem);
            System.out.println("Product scanned successfully");
        }
    }

    public void applyDiscount(double amount) {
        currentBill.setDiscountAmount(amount);
        System.out.println("Discount Applied.");
    }

    public Bill checkOut(double cashProvided) {
        currentBill.calculateTotal();
        if (cashProvided < currentBill.getTotalAmount()) {
            System.out.println("Inssufficent cash Provided.");
            return null;
        }
        currentBill.setCashProvided(cashProvided);
        double returnCash = cashProvided - currentBill.getTotalAmount();
        currentBill.setReturnCash(returnCash);

        List<BillItem> billItems = currentBill.getItems();
        for (BillItem b : billItems) {
            inventoryController.reduceStock(b.getProduct().getBarcode(), b.getQuantity());
        }

        billDatabase.add(currentBill);
        System.out.println("CheckOut Successfull.");
        return currentBill;
    }

    public List<ReturnTransaction> getAllReturns() {
        return returnDatabase;
    }

    public void processReturn(Bill originalBill, List<ReturnItem> items, String reason) {
        int returnId = returnDatabase.size() + 1;
        
        ReturnTransaction returnTx = new ReturnTransaction(returnId, originalBill, reason);
        
        for (ReturnItem item : items) {
            returnTx.addReturnedItem(item);
            
            inventoryController.addStock(item.getOriginalItem().getProduct().getBarcode(), item.getReturnQuantity());
        }
        
        returnDatabase.add(returnTx);
        System.out.println("Return processed successfully. Refund amount: " + returnTx.getRefundAmount());
    }

}
