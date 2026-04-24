package controller;

import service.BillingService;
import model.Bill;
import model.User;
import model.ReturnItem;
import model.ReturnTransaction;
import java.util.List;

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
}
