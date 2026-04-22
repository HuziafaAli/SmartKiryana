package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Bill {

    private int billId;
    private User user; // The employee or admin who processed the sale
    private List<BillItem> items;
    private LocalDateTime billDate;
    private double totalAmount;
    private double taxAmount;
    private double discountAmount;

    public Bill() {
        this.items = new ArrayList<>();
        this.billDate = LocalDateTime.now(); // Automatically set the current time
    }

    public Bill(int billId, User user) {
        this.billId = billId;
        this.user = user;
        this.items = new ArrayList<>();
        this.billDate = LocalDateTime.now();
    }

    // --- Business Logic ---

    public void addItem(BillItem item) {
        this.items.add(item);
        calculateTotal(); // Recalculate total whenever an item is added
    }

    public void calculateTotal() {
        double sum = 0;
        for (BillItem item : items) {
            sum += item.getSubtotal();
        }
        
        sum += taxAmount;
        sum -= discountAmount;
        
        this.totalAmount = sum;
    }

    // --- Getters & Setters ---

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<BillItem> getItems() {
        return items;
    }

    public void setItems(List<BillItem> items) {
        this.items = items;
        calculateTotal();
    }

    public LocalDateTime getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDateTime billDate) {
        this.billDate = billDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
        calculateTotal();
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
        calculateTotal();
    }

    @Override
    public String toString() {
        return "Bill{" +
                "billId=" + billId +
                ", user=" + (user != null ? user.getFullName() : "None") +
                ", itemsCount=" + items.size() +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
