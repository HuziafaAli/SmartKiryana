package template;

import model.MonthlyReport;
import model.Bill;
import model.BillItem;
import model.ReturnTransaction;
import model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MonthlyReportGenerator extends ReportTemplate<MonthlyReport> {

    private int month;
    private int year;
    private List<Bill> allBills;
    private List<ReturnTransaction> allReturns;

    private double totalSales;
    private double totalReturns;
    private Map<Product, Integer> productSalesMap;
    private List<Product> topProducts;

    public MonthlyReportGenerator(int month, int year, List<Bill> allBills, List<ReturnTransaction> allReturns) {
        this.month = month;
        this.year = year;
        this.allBills = allBills;
        this.allReturns = allReturns;
        
        this.totalSales = 0;
        this.totalReturns = 0;
        this.productSalesMap = new HashMap<>();
        this.topProducts = new ArrayList<>();
    }

    @Override
    protected void gatherData() {
        for (Bill b : allBills) {
            if (b.getBillDate().getMonthValue() == month && b.getBillDate().getYear() == year) {
                totalSales += b.getTotalAmount();
                
                for (BillItem item : b.getItems()) {
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
    }

    @Override
    protected void calculateTotals() {
        productSalesMap.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .limit(3)
                .forEach(entry -> topProducts.add(entry.getKey()));
    }

    @Override
    protected MonthlyReport buildReport() {
        return new MonthlyReport(0, month, year, totalSales, totalReturns, topProducts);
    }
}
