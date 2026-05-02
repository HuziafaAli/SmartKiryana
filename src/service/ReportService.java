package service;

import model.Employee;
import model.User;
import model.MonthlyReport;
import model.PerformanceReport;
import model.SalesTarget;
import model.Bill;
import model.ReturnTransaction;
import dao.SalesTargetDAO;

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
        List<SalesTarget> targetDatabase = salesTargetDAO.findAll();
        return generatePerformanceReport(emp, month, year, allBills, targetDatabase);
    }

    private PerformanceReport generatePerformanceReport(Employee emp, int month, int year,
            List<Bill> allBills, List<SalesTarget> targetDatabase) {

        if (!Validator.isNotNull(emp) || !Validator.isValidMonth(month)
                || !Validator.isValidYear(year)) {
            return null;
        }

        ReportTemplate<PerformanceReport> generator = new PerformanceReportGenerator(emp, month, year, allBills,
                targetDatabase);
        return generator.generate();
    }

    public List<SalesTarget> getAllTargets() {
        return salesTargetDAO.findAll();
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


}
