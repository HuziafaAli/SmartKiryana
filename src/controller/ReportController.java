package controller;

import service.ReportService;
import model.Employee;
import model.MonthlyReport;
import model.PerformanceReport;
import model.Bill;
import model.ReturnTransaction;
import model.SalesRecord;
import java.util.List;

public class ReportController {

    private ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    public void assignTarget(Employee employee, int month, int year, double targetAmount) {
        boolean success = reportService.assignTarget(employee, month, year, targetAmount);
        
        if (success) {
            System.out.println("Target of " + targetAmount + " assigned to " + employee.getFullName() + " for " + month + "/" + year);
        } else {
            System.out.println("Access Denied: Only Admins can assign sales targets.");
        }
    }

    public void generatePerformanceReport(Employee emp, int month, int year, List<Bill> allBills) {
        PerformanceReport report = reportService.generatePerformanceReport(emp, month, year, allBills);
        System.out.println("Performance Report Generated for " + emp.getFullName());
    }

    public void generateMonthlyReport(int month, int year, List<Bill> allBills, List<ReturnTransaction> allReturns) {
        MonthlyReport report = reportService.generateMonthlyReport(month, year, allBills, allReturns);
        System.out.println("Monthly Report Generated for " + month + "/" + year);
    }

    public List<SalesRecord> getSalesHistory(List<Bill> allBills) {
        return reportService.getSalesHistory(allBills);
    }
}
