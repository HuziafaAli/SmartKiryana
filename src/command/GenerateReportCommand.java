package command;

import controller.ReportController;
import model.Employee;
import model.Bill;
import model.ReturnTransaction;
import model.MonthlyReport;
import model.PerformanceReport;

import java.util.List;

public class GenerateReportCommand implements Command {

    private ReportController reportController;
    private Employee emp;
    private List<Bill> allBills;
    private List<ReturnTransaction> allReturns;
    private int month;
    private int year;
    private int reportType; // 1 for monthlyReport
                            // 2 for performanceReport
    private MonthlyReport monthlyReport;
    private PerformanceReport performanceReport;

    public GenerateReportCommand(ReportController reportController, Employee emp, int reportType, int month, int year) {
        this.reportController = reportController;
        this.emp = emp;
        this.reportType = reportType;
        this.month = month;
        this.year = year;
    }

    public void setBill(List<Bill> allBills) {
        this.allBills = allBills;
    }

    public void setReturn(List<ReturnTransaction> allReturns) {
        this.allReturns = allReturns;
    }

    @Override
    public void execute() {
        if (reportType == 1) {
            monthlyReport = reportController.generateMonthlyReport(month, year, allBills, allReturns);
        } else if (reportType == 2) {
            performanceReport = reportController.generatePerformanceReport(emp, month, year, allBills);
        }
    }

    public model.MonthlyReport getMonthlyReport() {
        return monthlyReport;
    }

    public model.PerformanceReport getPerformanceReport() {
        return performanceReport;
    }
}
