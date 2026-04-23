package model;

import java.time.LocalDateTime;

public class SalesRecord {

    private int recordId;
    private int billId;
    private String employeeName;
    private LocalDateTime saleDate;
    private double totalAmount;
    private int itemCount;

    public SalesRecord() {
    }

    public SalesRecord(int recordId, int billId, String employeeName, LocalDateTime saleDate, double totalAmount, int itemCount) {
        this.recordId = recordId;
        this.billId = billId;
        this.employeeName = employeeName;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
    }

    // Getters

    public int getRecordId() {
        return recordId;
    }

    public int getBillId() {
        return billId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public int getItemCount() {
        return itemCount;
    }

    @Override
    public String toString() {
        return "SalesRecord{" +
                "billId=" + billId +
                ", employee='" + employeeName + '\'' +
                ", total=" + totalAmount +
                '}';
    }
}
