package template;

import model.PerformanceReport;
import model.Employee;
import model.Bill;
import model.SalesTarget;

import java.util.List;

public class PerformanceReportGenerator extends ReportTemplate<PerformanceReport> {

    private Employee emp;
    private int month;
    private int year;
    private List<Bill> allBills;
    private List<SalesTarget> targetDatabase;

    private double totalSales;
    private double targetAmt;
    private double bonus;
    private SalesTarget foundTarget;

    public PerformanceReportGenerator(Employee emp, int month, int year, List<Bill> allBills, List<SalesTarget> targetDatabase) {
        this.emp = emp;
        this.month = month;
        this.year = year;
        this.allBills = allBills;
        this.targetDatabase = targetDatabase;
        
        this.totalSales = 0;
        this.targetAmt = 0;
        this.bonus = 0;
        this.foundTarget = null;
    }

    // Totals the employee's sales for the period and finds their assigned target
    @Override
    protected void gatherData() {
        for (Bill b : allBills) {
            if (b.getUser() != null && b.getUser().getUserId() == emp.getUserId()) {
                if (b.getBillDate().getMonthValue() == month && b.getBillDate().getYear() == year) {
                    totalSales += b.getTotalAmount();
                }
            }
        }

        for (SalesTarget t : targetDatabase) {
            if (t.getEmployee().getUserId() == emp.getUserId() && t.getMonth() == month && t.getYear() == year) {
                targetAmt = t.getTargetAmount();
                foundTarget = t;
                break;
            }
        }
    }

    // Calculates the 5% bonus if the employee exceeded their target
    @Override
    protected void calculateTotals() {
        if (foundTarget != null) {
            foundTarget.setAchievedAmount(totalSales);
        }

        if (targetAmt > 0 && totalSales > targetAmt) {
            bonus = (totalSales - targetAmt) * 0.05;
        }
    }

    @Override
    protected PerformanceReport buildReport() {
        return new PerformanceReport(0, emp, month, year, totalSales, targetAmt, bonus);
    }
}
