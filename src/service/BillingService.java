package service;

import model.Bill;
import model.BillItem;
import model.User;
import model.InventoryItem;
import model.Product;
import model.ReturnItem;
import model.ReturnTransaction;

import java.util.ArrayList;
import java.util.List;

public class BillingService {

    // Runtime Database
    private List<Bill> billDatabase;
    private List<ReturnTransaction> returnDatabase;

    private Bill currentBill;
    private InventoryService inventoryService;

    public BillingService(InventoryService inventoryService) {
        this.billDatabase = new ArrayList<>();
        this.returnDatabase = new ArrayList<>();
        this.currentBill = null;
        this.inventoryService = inventoryService;
    }

    public void startNewBill(User cashier) {
        int billId = billDatabase.size() + 1;
        currentBill = new Bill(billId, cashier);
    }

    public Bill getCurrentBill() {
        return currentBill;
    }

    public String scanItem(String barcode, int quantity) {
        if (currentBill == null) {
            return "No active bill. Please start a new bill first.";
        }

        InventoryItem itemExist = inventoryService.isProductExists(barcode);

        if (itemExist == null) {
            return "Product does not exist.";
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
            return "Not Enough stock. Stock = " + itemExist.getStockQuantity() + ", Quantity Req = "
                    + totalQuantityRequested;
        }

        if (existingBillItem != null) {
            existingBillItem.setQuantity(totalQuantityRequested);
            currentBill.calculateTotal();
            return "Updated quantity of " + itemExist.getProduct().getName() + " to " + totalQuantityRequested;
        } else {
            Product p = itemExist.getProduct();
            int newID = currentBill.getItems().size() + 1;
            BillItem currentBillItem = new BillItem(newID, p, quantity, p.getPrice());
            currentBill.addItem(currentBillItem);
            return "Product scanned successfully.";
        }
    }

    public void applyDiscount(double amount) {
        if (currentBill != null) {
            currentBill.setDiscountAmount(amount);
        }
    }

    public boolean checkOut(double cashProvided) {
        if (currentBill == null)
            return false;

        currentBill.calculateTotal();
        if (cashProvided < currentBill.getTotalAmount()) {
            return false;
        }

        currentBill.setCashProvided(cashProvided);
        double returnCash = cashProvided - currentBill.getTotalAmount();
        currentBill.setReturnCash(returnCash);

        List<BillItem> billItems = currentBill.getItems();
        for (BillItem b : billItems) {
            inventoryService.reduceStock(b.getProduct().getBarcode(), b.getQuantity());
        }

        billDatabase.add(currentBill);
        currentBill = null;
        return true;
    }

    public List<ReturnTransaction> getAllReturns() {
        return returnDatabase;
    }

    public List<Bill> getAllBills() {
        return billDatabase;
    }

    public ReturnTransaction processReturn(Bill originalBill, List<ReturnItem> items, String reason) {
        int returnId = returnDatabase.size() + 1;

        ReturnTransaction returnTx = new ReturnTransaction(returnId, originalBill, reason);

        for (ReturnItem item : items) {
            returnTx.addReturnedItem(item);
            inventoryService.addStock(item.getOriginalItem().getProduct().getBarcode(), item.getReturnQuantity());
        }

        returnDatabase.add(returnTx);
        return returnTx;
    }
}
