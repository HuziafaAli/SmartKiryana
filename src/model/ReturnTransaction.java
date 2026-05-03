package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReturnTransaction {

    private int returnId;
    private Bill originalBill; 
    private List<ReturnItem> returnedItems; 
    private double refundAmount; 
    private LocalDateTime returnDate;
    private String reason;

    public ReturnTransaction() {
        this.returnedItems = new ArrayList<>();
        this.returnDate = LocalDateTime.now();
    }

    public ReturnTransaction(int returnId, Bill originalBill, String reason) {
        this.returnId = returnId;
        this.originalBill = originalBill;
        this.returnedItems = new ArrayList<>();
        this.reason = reason;
        this.returnDate = LocalDateTime.now();
        this.refundAmount = 0.0;
    }

    // Adds a returned item and recalculates the total refund
    public void addReturnedItem(ReturnItem item) {
        returnedItems.add(item);
        calculateRefund();
    }

    // Sums up refund subtotals from all returned items
    public void calculateRefund() {
        double sum = 0;
        for (ReturnItem item : returnedItems) {
            sum += item.getRefundSubtotal(); 
        }
        this.refundAmount = sum;
    }

    public int getReturnId() {
        return returnId;
    }

    public void setReturnId(int returnId) {
        this.returnId = returnId;
    }

    public Bill getOriginalBill() {
        return originalBill;
    }

    public void setOriginalBill(Bill originalBill) {
        this.originalBill = originalBill;
    }

    public List<ReturnItem> getReturnedItems() {
        return returnedItems;
    }

    public void setReturnedItems(List<ReturnItem> returnedItems) {
        this.returnedItems = returnedItems;
        calculateRefund();
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "ReturnTransaction{" +
                "returnId=" + returnId +
                ", itemsReturned=" + returnedItems.size() +
                ", totalRefund=" + refundAmount +
                '}';
    }
}
