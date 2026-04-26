package service;

import model.Employee;
import model.User;
import model.MonthlyReport;
import model.PerformanceReport;
import model.SalesTarget;
import model.Bill;
import model.ReturnTransaction;
import model.SalesRecord;
import dao.SalesTargetDAO;

import java.util.ArrayList;
import java.util.List;

import template.MonthlyReportGenerator;
import template.PerformanceReportGenerator;
import template.ReportTemplate;
import util.Validator;

public class ReportService {

    private SalesTargetDAO salesTargetDAO;
    private AuthService authService;

    public ReportService(AuthService authService, SalesTargetDAO salesTargetDAO) {
        this.authService = authService;
        this.salesTargetDAO = salesTargetDAO;
    }

    public boolean assignTarget(Employee employee, int month, int year, double targetAmount) {
        User currentUser = authService.getCurrentUser();

        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("ADMIN")) {
            return false;
        }

        if (!Validator.isNotNull(employee) || !Validator.isValidMonth(month)
                || !Validator.isValidYear(year) || !Validator.isPositiveAmount(targetAmount)) {
            return false;
        }

        SalesTarget newTarget = new SalesTarget(0, employee, month, year, targetAmount);
        return salesTargetDAO.save(newTarget);
    }

    public PerformanceReport generatePerformanceReport(Employee emp, int month, int year,
            List<Bill> allBills) {

        if (!Validator.isNotNull(emp) || !Validator.isValidMonth(month)
                || !Validator.isValidYear(year)) {
            return null;
        }

        List<SalesTarget> targetDatabase = salesTargetDAO.findAll();

        ReportTemplate<PerformanceReport> generator = new PerformanceReportGenerator(emp, month, year, allBills,
                targetDatabase);
        PerformanceReport report = generator.generate();

        return report;
    }

    public MonthlyReport generateMonthlyReport(int month, int year, List<Bill> allBills,
            List<ReturnTransaction> allReturns) {

        if (!Validator.isValidMonth(month) || !Validator.isValidYear(year)) {
            return null;
        }

        ReportTemplate<MonthlyReport> generator = new MonthlyReportGenerator(month, year, allBills, allReturns);
        MonthlyReport report = generator.generate();

        return report;
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

