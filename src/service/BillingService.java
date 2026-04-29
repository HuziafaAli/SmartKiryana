package service;

import model.Bill;
import model.BillItem;
import model.User;
import model.InventoryItem;
import model.Product;
import model.ReturnItem;
import model.ReturnTransaction;
import dao.BillDAO;
import dao.ReturnTransactionDAO;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import util.Validator;

public class BillingService {

    private BillDAO billDAO;
    private ReturnTransactionDAO returnDAO;

    private Bill currentBill;
    private InventoryService inventoryService;

    public BillingService(InventoryService inventoryService, BillDAO billDAO, ReturnTransactionDAO returnDAO) {
        this.billDAO = billDAO;
        this.returnDAO = returnDAO;
        this.currentBill = null;
        this.inventoryService = inventoryService;
    }

    public Bill startNewBill(User cashier) {
        currentBill = new Bill(0, cashier);
        return currentBill;
    }

    public Bill getCurrentBill() {
        return currentBill;
    }

    public String scanItem(String barcode, int quantity) {
        if (currentBill == null) {
            return "No active bill. Please start a new bill first.";
        }

        if (!Validator.isValidBarcode(barcode) || !Validator.isPositiveQuantity(quantity)) {
            return "Invalid barcode or quantity.";
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

    public boolean applyDiscount(double amount) {
        if (!Validator.isPositiveAmount(amount))
            return false;

        if (currentBill != null) {
            currentBill.setDiscountAmount(amount);
            return true;
        }
        return false;
    }

    public boolean removeItem(String barcode) {
        if (currentBill == null || !Validator.isValidBarcode(barcode)) {
            return false;
        }

        BillItem toRemove = null;
        for (BillItem b : currentBill.getItems()) {
            if (b.getProduct().getBarcode().equals(barcode)) {
                toRemove = b;
                break;
            }
        }

        if (toRemove == null) {
            return false;
        }

        currentBill.getItems().remove(toRemove);
        currentBill.calculateTotal();
        return true;
    }

    public Bill checkOut(double cashProvided) {
        if (currentBill == null)
            return null;

        if (!Validator.isPositiveAmount(cashProvided)) {
            return null;
        }

        currentBill.calculateTotal();
        if (cashProvided < currentBill.getTotalAmount()) {
            return null;
        }

        currentBill.setCashProvided(cashProvided);
        double returnCash = cashProvided - currentBill.getTotalAmount();
        currentBill.setReturnCash(returnCash);

        List<BillItem> billItems = currentBill.getItems();
        for (BillItem b : billItems) {
            inventoryService.reduceStock(b.getProduct().getBarcode(), b.getQuantity());
        }

        billDAO.save(currentBill);
        Bill finalizedBill = currentBill;
        currentBill = null;
        return finalizedBill;
    }

    public List<ReturnTransaction> getAllReturns() {
        return returnDAO.findAll();
    }

    public List<Bill> getAllBills() {
        return billDAO.findAll();
    }

    public ReturnTransaction processReturn(Bill originalBill, List<ReturnItem> items, String reason) {
        if (originalBill == null || items == null || items.isEmpty()) {
            return null;
        }

        for (ReturnItem rItem : items) {
            if (rItem.getOriginalItem() == null || rItem.getReturnQuantity() <= 0) {
                return null;
            }

            boolean itemFoundInBill = false;
            for (BillItem bItem : originalBill.getItems()) {
                if (bItem.getBillItemId() == rItem.getOriginalItem().getBillItemId()) {
                    itemFoundInBill = true;
                    if (rItem.getReturnQuantity() > bItem.getQuantity()) {
                        return null;
                    }
                    break;
                }
            }

            if (!itemFoundInBill) {
                return null;
            }
        }

        ReturnTransaction returnTx = new ReturnTransaction(0, originalBill, reason);

        for (ReturnItem item : items) {
            returnTx.addReturnedItem(item);
            String barcode = item.getOriginalItem().getProduct().getBarcode();
            int qty = item.getReturnQuantity();
            
            boolean stockAdded = inventoryService.addStock(barcode, qty);
            if (!stockAdded) {
                // The item was deleted from inventory! Recreate the row so stock isn't lost.
                inventoryService.restoreDeletedStock(barcode, qty);
            }
        }

        returnDAO.save(returnTx);
        return returnTx;
    }

    // Filtering: Service fetches all bills then applies logic in Java

    public List<Bill> filterByDateRange(LocalDateTime from, LocalDateTime to) {
        List<Bill> allBills = billDAO.findAll();
        List<Bill> filtered = new ArrayList<>();
        for (Bill b : allBills) {
            if (!b.getBillDate().isBefore(from) && !b.getBillDate().isAfter(to)) {
                filtered.add(b);
            }
        }
        return filtered;
    }


}

