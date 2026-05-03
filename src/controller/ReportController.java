package controller;

import service.ReportService;
import model.Employee;
import model.MonthlyReport;
import model.PerformanceReport;
import model.Bill;
import model.ReturnTransaction;
import java.util.List;
import java.util.ArrayList;

public class ReportController {

    private ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Assigns or updates a monthly sales target for an employee
    public boolean assignTarget(Employee employee, int month, int year, double targetAmount) {
        boolean success = reportService.assignTarget(employee, month, year, targetAmount);

        if (success) {
            System.out.println("Target of " + targetAmount + " assigned to " + employee.getFullName() + " for " + month
                    + "/" + year);
        } else {
            System.out.println("Access Denied: Only Admins can assign sales targets.");
        }
        return success;
    }

    // Generates a single employee's performance report for a given month
    public PerformanceReport generatePerformanceReport(Employee emp, int month, int year, List<Bill> allBills) {
        PerformanceReport report = reportService.generatePerformanceReport(emp, month, year, allBills);
        if (report != null) {
            System.out.println("Performance Report Generated for " + emp.getFullName());
        } else {
            System.out.println("Failed to generate report. Invalid data.");
        }
        return report;
    }

    // Generates the store-wide monthly sales summary
    public MonthlyReport generateMonthlyReport(int month, int year, List<Bill> allBills,
            List<ReturnTransaction> allReturns) {

        MonthlyReport report = reportService.generateMonthlyReport(month, year, allBills, allReturns);
        if (report != null) {
            System.out.println("Monthly Report Generated for " + month + "/" + year);
        } else {
            System.out.println("Failed to generate report. Invalid data.");
        }
        return report;
    }

    // Builds performance reports for multiple employees at once
    public List<PerformanceReport> getPerformanceComparison(List<Employee> employees, int month, int year,
            List<Bill> allBills) {

        List<PerformanceReport> reports = new ArrayList<>();
        for (Employee emp : employees) {
            PerformanceReport report = reportService.generatePerformanceReport(emp, month, year, allBills);
            if (report != null) {
                reports.add(report);
            }
        }
        return reports;
    }

    public List<model.SalesTarget> getAllTargets() {
        return reportService.getAllTargets();
    }


}
