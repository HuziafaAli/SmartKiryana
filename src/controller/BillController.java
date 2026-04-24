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

    public String scanItem(String barcode, int quantity) {
        String resultMessage = billingService.scanItem(barcode, quantity);
        System.out.println(resultMessage);
        return resultMessage;
    }

    public void applyDiscount(double amount) {
        billingService.applyDiscount(amount);
        System.out.println("Discount Applied.");
    }

    public boolean checkOut(double cashProvided) {
        boolean success = billingService.checkOut(cashProvided);
        if (success) {
            System.out.println("Checkout Successful. Thank you for shopping!");
        } else {
            System.out.println("Insufficient cash provided for checkout.");
        }
        return success;
    }

    public ReturnTransaction processReturn(Bill originalBill, List<ReturnItem> items, String reason) {
        ReturnTransaction tx = billingService.processReturn(originalBill, items, reason);
        System.out.println("Return processed successfully. Refund amount: " + tx.getRefundAmount());
        return tx;
    }

    public List<Bill> getAllBills() {
        return billingService.getAllBills();
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
}
