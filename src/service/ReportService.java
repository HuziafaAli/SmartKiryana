package service;

import model.Employee;
import model.User;
import model.MonthlyReport;
import model.PerformanceReport;
import model.SalesTarget;
import model.Bill;
import model.BillItem;
import model.ReturnTransaction;
import model.SalesRecord;
import model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    public PerformanceReport generatePerformanceReport(Employee emp, int month, int year, List<Bill> allBills) {
        double totalSales = 0;

        for (Bill b : allBills) {
            if (b.getUser() != null && b.getUser().getUserId() == emp.getUserId()) {
                if (b.getBillDate().getMonthValue() == month && b.getBillDate().getYear() == year) {
                    totalSales += b.getTotalAmount();
                }
            }
        }

        double targetAmt = 0;
        for (SalesTarget t : targetDatabase) {
            if (t.getEmployee().getUserId() == emp.getUserId() && t.getMonth() == month && t.getYear() == year) {
                targetAmt = t.getTargetAmount();
                t.setAchievedAmount(totalSales);
                break;
            }
        }

        double bonus = 0;
        if (targetAmt > 0 && totalSales > targetAmt) {
            bonus = (totalSales - targetAmt) * 0.05;
        }

        int reportId = performanceReports.size() + 1;
        PerformanceReport report = new PerformanceReport(reportId, emp, month, year, totalSales, targetAmt, bonus);
        performanceReports.add(report);

        return report;
    }

    public MonthlyReport generateMonthlyReport(int month, int year, List<Bill> allBills,
            List<ReturnTransaction> allReturns) {
        double totalSales = 0;
        double totalReturns = 0;
        Map<Product, Integer> productSalesMap = new HashMap<>();

        for (Bill b : allBills) {
            if (b.getBillDate().getMonthValue() == month && b.getBillDate().getYear() == year) {
                totalSales += b.getTotalAmount();
                List<BillItem> i = b.getItems();
                for (BillItem item : i) {
                    Product p = item.getProduct();
                    productSalesMap.put(p, productSalesMap.getOrDefault(p, 0) + item.getQuantity());
                }
            }
        }

        for (ReturnTransaction r : allReturns) {
            if (r.getReturnDate().getMonthValue() == month && r.getReturnDate().getYear() == year) {
                totalReturns += r.getRefundAmount();
            }
        }

        List<Product> topProducts = new ArrayList<>();
        productSalesMap.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .limit(3)
                .forEach(entry -> topProducts.add(entry.getKey()));

        int reportId = monthlyReports.size() + 1;
        MonthlyReport report = new MonthlyReport(reportId, month, year, totalSales, totalReturns, topProducts);
        monthlyReports.add(report);

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
