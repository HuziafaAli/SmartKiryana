package model;

import java.time.LocalDateTime;

public class PerformanceReport {

    private int reportId;
    private Employee employee;
    private int month;
    private int year;
    private double totalSales;
    private double targetAmount;
    private double achievementPercentage;
    private double bonusAmount;
    private LocalDateTime generatedDate;

    public PerformanceReport() {
        this.generatedDate = LocalDateTime.now();
    }

    public PerformanceReport(int reportId, Employee employee, int month, int year, double totalSales, double targetAmount, double bonusAmount) {
        this.reportId = reportId;
        this.employee = employee;
        this.month = month;
        this.year = year;
        this.totalSales = totalSales;
        this.targetAmount = targetAmount;
        this.bonusAmount = bonusAmount;
        this.generatedDate = LocalDateTime.now();
        
        if (targetAmount > 0) {
            this.achievementPercentage = (totalSales / targetAmount) * 100.0;
        } else {
            this.achievementPercentage = 0.0;
        }
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getAchievementPercentage() {
        return achievementPercentage;
    }

    public void setAchievementPercentage(double achievementPercentage) {
        this.achievementPercentage = achievementPercentage;
    }

    public double getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(double bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }

    @Override
    public String toString() {
        return "PerformanceReport{" +
                "employee=" + (employee != null ? employee.getFullName() : "None") +
                ", month=" + month + "/" + year +
                ", achievement=" + String.format("%.2f", achievementPercentage) + "%" +
                '}';
    }
}
