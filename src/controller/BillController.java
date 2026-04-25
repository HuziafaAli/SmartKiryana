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

    public Bill startNewBill(User cashier) {
        Bill b = billingService.startNewBill(cashier);
        System.out.println("Started new bill session.");
        return b;
    }

    public String scanItem(String barcode, int quantity) {
        String resultMessage = billingService.scanItem(barcode, quantity);
        System.out.println(resultMessage);
        return resultMessage;
    }

    public boolean applyDiscount(double amount) {
        boolean success = billingService.applyDiscount(amount);
        if (success) {
            System.out.println("Discount Applied.");
        } else {
            System.out.println("Failed to apply discount.");
        }
        return success;
    }

    public Bill checkOut(double cashProvided) {
        Bill finalizedBill = billingService.checkOut(cashProvided);
        if (finalizedBill != null) {
            System.out.println("Checkout Successful. Thank you for shopping!");
        } else {
            System.out.println("Checkout failed. Insufficient cash or no active bill.");
        }
        return finalizedBill;
    }

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

    public List<Bill> filterByDateRange(LocalDateTime from, LocalDateTime to) {
        List<Bill> filtered = billingService.filterByDateRange(from, to);
        System.out.println("Found " + filtered.size() + " sales in the specified date range.");
        return filtered;
    }

    public List<Bill> filterByCategory(int categoryId) {
        List<Bill> filtered = billingService.filterByCategory(categoryId);
        System.out.println("Found " + filtered.size() + " sales for category ID " + categoryId + ".");
        return filtered;
    }

    public List<Bill> filterByEmployee(int employeeId) {
        List<Bill> filtered = billingService.filterByEmployee(employeeId);
        System.out.println("Found " + filtered.size() + " sales processed by employee ID " + employeeId + ".");
        return filtered;
    }

    public List<Bill> filterByAmountRange(double minAmount, double maxAmount) {
        List<Bill> filtered = billingService.filterByAmountRange(minAmount, maxAmount);
        System.out.println("Found " + filtered.size() + " sales in the amount range " + minAmount + " - " + maxAmount + ".");
        return filtered;
    }
}
