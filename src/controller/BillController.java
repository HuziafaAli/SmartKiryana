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

    public void startNewBill(User cashier) {
        billingService.startNewBill(cashier);
        System.out.println("New bill started for cashier: " + cashier.getFullName());
    }

    public void scanItem(String barcode, int quantity) {
        String resultMessage = billingService.scanItem(barcode, quantity);
        System.out.println(resultMessage);
    }

    public void applyDiscount(double amount) {
        billingService.applyDiscount(amount);
        System.out.println("Discount Applied.");
    }

    public void checkOut(double cashProvided) {
        boolean success = billingService.checkOut(cashProvided);
        if (success) {
            System.out.println("Checkout Successful. Thank you for shopping!");
        } else {
            System.out.println("Insufficient cash provided for checkout.");
        }
    }

    public void processReturn(Bill originalBill, List<ReturnItem> items, String reason) {
        ReturnTransaction tx = billingService.processReturn(originalBill, items, reason);
        System.out.println("Return processed successfully. Refund amount: " + tx.getRefundAmount());
    }

    public List<Bill> getAllBills() {
        return billingService.getAllBills();
    }

    public void filterByDateRange(LocalDateTime from, LocalDateTime to) {
        List<Bill> filtered = billingService.filterByDateRange(from, to);
        if (filtered.isEmpty()) {
            System.out.println("No sales found in the specified date range.");
        } else {
            System.out.println("Found " + filtered.size() + " sales between " + from + " and " + to + ":");
            for (Bill b : filtered) {
                System.out.println("  Bill #" + b.getBillId() + " | Date: " + b.getBillDate() + " | Total: " + b.getTotalAmount());
            }
        }
    }

    public void filterByCategory(int categoryId) {
        List<Bill> filtered = billingService.filterByCategory(categoryId);
        if (filtered.isEmpty()) {
            System.out.println("No sales found for the specified category.");
        } else {
            System.out.println("Found " + filtered.size() + " sales containing products from category ID " + categoryId + ":");
            for (Bill b : filtered) {
                System.out.println("  Bill #" + b.getBillId() + " | Date: " + b.getBillDate() + " | Total: " + b.getTotalAmount());
            }
        }
    }
}
