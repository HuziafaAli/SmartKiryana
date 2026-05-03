package controller;

import service.BillingService;
import model.Bill;
import model.User;
import model.ReturnItem;
import model.ReturnTransaction;
import java.util.List;
import java.time.LocalDateTime;

public class BillController {

    private BillingService billingService;

    public BillController(BillingService billingService) {
        this.billingService = billingService;
    }

    // Creates a fresh bill session for the given cashier
    public Bill startNewBill(User cashier) {
        Bill b = billingService.startNewBill(cashier);
        System.out.println("Started new bill session.");
        return b;
    }

    // Adds or updates an item in the current bill by barcode
    public String scanItem(String barcode, int quantity) {
        String resultMessage = billingService.scanItem(barcode, quantity);
        System.out.println(resultMessage);
        return resultMessage;
    }

    // Applies a flat discount to the current bill
    public boolean applyDiscount(double amount) {
        boolean success = billingService.applyDiscount(amount);
        if (success) {
            System.out.println("Discount Applied.");
        } else {
            System.out.println("Failed to apply discount.");
        }
        return success;
    }

    // Finalizes the bill, deducts stock, and saves to database
    public Bill checkOut(double cashProvided) {
        Bill finalizedBill = billingService.checkOut(cashProvided);
        if (finalizedBill != null) {
            System.out.println("Checkout Successful. Thank you for shopping!");
        } else {
            System.out.println("Checkout failed. Insufficient cash or no active bill.");
        }
        return finalizedBill;
    }

    // Handles returning items from a previous bill and restoring stock
    public ReturnTransaction processReturn(Bill originalBill, List<ReturnItem> items, String reason) {
        ReturnTransaction tx = billingService.processReturn(originalBill, items, reason);
        if (tx != null) {
            System.out.println("Return processed successfully. Refund amount: " + tx.getRefundAmount());
        } else {
            System.out.println("Return failed. Items are ineligible or not from this bill.");
        }
        return tx;
    }

    public Bill getCurrentBill() {
        return billingService.getCurrentBill();
    }

    // Removes a single item from the active bill
    public boolean removeItem(String barcode) {
        boolean success = billingService.removeItem(barcode);
        if (success) {
            System.out.println("Item removed from bill.");
        } else {
            System.out.println("Item not found in current bill.");
        }
        return success;
    }

    public List<Bill> getAllBills() {
        return billingService.getAllBills();
    }

    public List<ReturnTransaction> getAllReturns() {
        return billingService.getAllReturns();
    }

    // Fetches bills within a specific date window
    public List<Bill> filterByDateRange(LocalDateTime from, LocalDateTime to) {
        List<Bill> filtered = billingService.filterByDateRange(from, to);
        System.out.println("Found " + filtered.size() + " sales in the specified date range.");
        return filtered;
    }


}
