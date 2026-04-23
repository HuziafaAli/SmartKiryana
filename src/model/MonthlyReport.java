package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MonthlyReport {

    private int reportId;
    private int month;
    private int year;
    private double totalSales;
    private double totalReturns;
    private List<Product> topProducts;
    private LocalDateTime generatedDate;

    public MonthlyReport() {
        this.topProducts = new ArrayList<>();
        this.generatedDate = LocalDateTime.now();
    }

    public MonthlyReport(int reportId, int month, int year, double totalSales, double totalReturns, List<Product> topProducts) {
        this.reportId = reportId;
        this.month = month;
        this.year = year;
        this.totalSales = totalSales;
        this.totalReturns = totalReturns;
        this.topProducts = topProducts != null ? topProducts : new ArrayList<>();
        this.generatedDate = LocalDateTime.now();
    }

    // Business Logic
    
    public double getNetRevenue() {
        return totalSales - totalReturns;
    }

    // Getters & Setters

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }

    public double getTotalReturns() {
        return totalReturns;
    }

    public void setTotalReturns(double totalReturns) {
        this.totalReturns = totalReturns;
    }

    public List<Product> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<Product> topProducts) {
        this.topProducts = topProducts;
    }

    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }

    @Override
    public String toString() {
        return "MonthlyReport{" +
                "month=" + month + "/" + year +
                ", netRevenue=" + getNetRevenue() +
                '}';
    }
}
