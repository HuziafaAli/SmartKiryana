package service;

import model.Employee;
import model.User;
import model.MonthlyReport;
import model.PerformanceReport;
import model.SalesTarget;
import model.Bill;
import model.ReturnTransaction;
import model.SalesRecord;

import java.util.ArrayList;
import java.util.List;

import template.MonthlyReportGenerator;
import template.PerformanceReportGenerator;
import template.ReportTemplate;

public class ReportService {

    private List<MonthlyReport> monthlyReports;
    private List<PerformanceReport> performanceReports;
    private List<SalesTarget> targetDatabase;

    private AuthService authService;

    public ReportService(AuthService authService) {
        this.monthlyReports = new ArrayList<>();
        this.performanceReports = new ArrayList<>();
        this.targetDatabase = new ArrayList<>();
        this.authService = authService;
    }

    public boolean assignTarget(Employee employee, int month, int year, double targetAmount) {
        User currentUser = authService.getCurrentUser();

        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("ADMIN")) {
            return false;
        }

        int targetId = targetDatabase.size() + 1;
        SalesTarget newTarget = new SalesTarget(targetId, employee, month, year, targetAmount);
        targetDatabase.add(newTarget);
        return true;
    }

    public PerformanceReport generatePerformanceReport(Employee emp, int month, int year, 
            List<Bill> allBills) {
        ReportTemplate<PerformanceReport> generator = new PerformanceReportGenerator(emp, month, year, allBills, targetDatabase);
        PerformanceReport report = generator.generate();

        int reportId = performanceReports.size() + 1;
        
        PerformanceReport finalReport = new PerformanceReport(reportId, report.getEmployee(), report.getMonth(), report.getYear(), 
                                                              report.getTotalSales(), report.getTargetAmount(), report.getBonusAmount());
        
        performanceReports.add(finalReport);
        return finalReport;
    }

    public MonthlyReport generateMonthlyReport(int month, int year, List<Bill> allBills,
            List<ReturnTransaction> allReturns) {
        
        ReportTemplate<MonthlyReport> generator = new MonthlyReportGenerator(month, year, allBills, allReturns);
        MonthlyReport report = generator.generate();

        int reportId = monthlyReports.size() + 1;
        
        MonthlyReport finalReport = new MonthlyReport(reportId, report.getMonth(), report.getYear(), 
                                                      report.getTotalSales(), report.getTotalReturns(), report.getTopProducts());
                                                      
        monthlyReports.add(finalReport);
        return finalReport;
    }

    public List<SalesRecord> getSalesHistory(List<Bill> allBills) {
        List<SalesRecord> history = new ArrayList<>();

        for (Bill bill : allBills) {
            SalesRecord record = new SalesRecord(
                    bill.getBillId(),
                    bill.getBillId(),
                    bill.getUser() != null ? bill.getUser().getFullName() : "Unknown",
                    bill.getBillDate(),
                    bill.getTotalAmount(),
                    bill.getItems().size());
            history.add(record);
        }

        return history;
    }
}
