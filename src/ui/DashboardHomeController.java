package ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.chart.*;
import facade.SystemFacade;
import model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class DashboardHomeController implements FacadeAware {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label todaySalesLabel;
    @FXML
    private Label todaySalesChange;
    @FXML
    private Label monthlyRevenueLabel;
    @FXML
    private Label monthlyRevenueChange;
    @FXML
    private Label lowStockLabel;
    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label alertCountLabel;

    @FXML
    private TableView<Bill> recentBillsTable;
    @FXML
    private TableColumn<Bill, String> colBillId;
    @FXML
    private TableColumn<Bill, String> colBillDate;
    @FXML
    private TableColumn<Bill, String> colBillAmount;
    @FXML
    private TableColumn<Bill, String> colBillCashier;

    @FXML
    private VBox topProductsList;
    @FXML
    private VBox alertsList;
    @FXML
    private VBox salesChartArea;

    private SystemFacade systemFacade;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");

    public void initialize() {
        colBillId.setCellValueFactory(c -> new SimpleStringProperty("BLL-" + c.getValue().getBillId()));
        colBillDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBillDate().format(dtf)));
        colBillAmount.setCellValueFactory(
                c -> new SimpleStringProperty(String.format("Rs. %.2f", c.getValue().getTotalAmount())));
        colBillCashier.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getUser() != null ? c.getValue().getUser().getFullName() : "N/A"));
    }

    @Override
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
        loadDashboardData();
    }

    private void loadDashboardData() {
        User user = systemFacade.getCurrentUser();
        boolean isEmployee = false;
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName());
            isEmployee = user.getRole().equalsIgnoreCase("EMPLOYEE");
        }

        List<Bill> allBills = systemFacade.getAllBills();
        List<Bill> displayBills = allBills;

        // If employee, only show their own data
        if (isEmployee) {
            displayBills = allBills.stream()
                    .filter(b -> b.getUser() != null && b.getUser().getUserId() == user.getUserId())
                    .collect(Collectors.toList());
        }

        List<InventoryItem> allItems = systemFacade.getAllInventoryItems();
        List<InventoryItem> lowStockItems = systemFacade.getLowStockItems();

        // Stat Cards Row
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        // 1. Today's Sales
        double todaySales = displayBills.stream()
                .filter(b -> b.getBillDate().toLocalDate().equals(today))
                .mapToDouble(Bill::getTotalAmount).sum();

        // 2. Monthly Revenue / Contribution
        double monthlyRevenue = displayBills.stream()
                .filter(b -> b.getBillDate().getMonthValue() == currentMonth
                        && b.getBillDate().getYear() == currentYear)
                .mapToDouble(Bill::getTotalAmount).sum();

        if (isEmployee) {
            // Customize labels for employee
            ((Label) todaySalesLabel.getParent().getChildrenUnmodifiable().get(0)).setText("My Sales Today");
            ((Label) monthlyRevenueLabel.getParent().getChildrenUnmodifiable().get(0)).setText("My Monthly Sales");
            ((Label) lowStockLabel.getParent().getChildrenUnmodifiable().get(0)).setText("Performance Score");
            ((Label) totalProductsLabel.getParent().getChildrenUnmodifiable().get(0)).setText("My Transactions");

            // Calculate Performance Score (Achievement %)
            List<Employee> empList = new java.util.ArrayList<>();
            empList.add((Employee) user);
            List<PerformanceReport> reports = systemFacade.getPerformanceComparison(empList, currentMonth, currentYear,
                    allBills);
            double score = 0;
            if (!reports.isEmpty()) {
                score = reports.get(0).getAchievementPercentage();
            }

            todaySalesLabel.setText(String.format("Rs. %,.0f", todaySales));
            monthlyRevenueLabel.setText(String.format("Rs. %,.0f", monthlyRevenue));
            lowStockLabel.setText(String.format("%.0f%%", Math.min(100.0, score)));
            totalProductsLabel.setText(String.valueOf(displayBills.size()));

            // Update subtitles
            if (todaySalesChange != null)
                todaySalesChange.setText("Keep it up!");
            if (monthlyRevenueChange != null)
                monthlyRevenueChange.setText("Monthly total contribution");

            var lowStockChildren = lowStockLabel.getParent().getChildrenUnmodifiable();
            if (lowStockChildren.size() > 2 && lowStockChildren.get(2) instanceof Label) {
                ((Label) lowStockChildren.get(2)).setText("Target achievement");
            }

            var totalProdChildren = totalProductsLabel.getParent().getChildrenUnmodifiable();
            if (totalProdChildren.size() > 2 && totalProdChildren.get(2) instanceof Label) {
                ((Label) totalProdChildren.get(2)).setText("Total bills processed");
            }

        } else {
            // Admin default view
            todaySalesLabel.setText(String.format("Rs. %,.0f", todaySales));
            monthlyRevenueLabel.setText(String.format("Rs. %,.0f", monthlyRevenue));
            lowStockLabel.setText(String.valueOf(lowStockItems.size()));
            totalProductsLabel.setText(String.valueOf(allItems.size()));
        }

        // Recent Transactions (last 5)
        List<Bill> recent = displayBills.stream()
                .sorted(Comparator.comparing(Bill::getBillDate).reversed())
                .limit(5)
                .collect(Collectors.toList());
        recentBillsTable.setItems(FXCollections.observableArrayList(recent));

        // Top Selling Products OR Performance Summary
        if (isEmployee) {
            loadPerformanceSummary(user, allBills);
        } else {
            loadTopProducts(allBills);
        }

        // Stock Alerts
        loadStockAlerts(lowStockItems);

        // Sales Overview Chart
        loadSalesChart(displayBills);
    }

    private void loadPerformanceSummary(User user, List<Bill> allBills) {
        topProductsList.getChildren().clear();

        // Change title of the panel
        VBox parent = (VBox) topProductsList.getParent();
        HBox header = (HBox) parent.getChildren().get(0);
        ((javafx.scene.text.Text) header.getChildren().get(0)).setText("Performance Summary");

        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        List<Employee> empList = new java.util.ArrayList<>();
        empList.add((Employee) user);
        List<PerformanceReport> reports = systemFacade.getPerformanceComparison(empList, month, year, allBills);

        if (!reports.isEmpty()) {
            PerformanceReport report = reports.get(0);
            long txCount = allBills.stream()
                    .filter(b -> b.getUser() != null && b.getUser().getUserId() == user.getUserId())
                    .filter(b -> b.getBillDate().getMonthValue() == month && b.getBillDate().getYear() == year)
                    .count();

            double avgBill = txCount > 0 ? report.getTotalSales() / txCount : 0;
            double peakSale = allBills.stream()
                    .filter(b -> b.getUser() != null && b.getUser().getUserId() == user.getUserId())
                    .filter(b -> b.getBillDate().getMonthValue() == month && b.getBillDate().getYear() == year)
                    .mapToDouble(Bill::getTotalAmount)
                    .max().orElse(0);

            double bonusAmount = report.getBonusAmount();
            String statusMsg;
            if (bonusAmount > 0) {
                statusMsg = String.format("- Status: Rs. %,.0f Bonus Earned! (5%% of surplus)", bonusAmount);
            } else {
                double remaining = Math.max(0, report.getTargetAmount() - report.getTotalSales());
                statusMsg = remaining > 0 ? String.format("- Status: Rs. %,.0f left to earn bonus", remaining)
                        : "- Status: Target hit! Next sale earns bonus!";
            }

            String[] metrics = {
                    "- Score: " + String.format("%.1f%%", Math.min(100.0, report.getAchievementPercentage())),
                    "- Transactions: " + txCount,
                    "- Avg Bill: Rs. " + String.format("%,.0f", avgBill),
                    "- Peak Sale: Rs. " + String.format("%,.0f", peakSale),
                    statusMsg
            };

            for (String metric : metrics) {
                Label l = new Label(metric);
                l.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 0;");
                topProductsList.getChildren().add(l);
            }
        }
    }

    private void loadSalesChart(List<Bill> allBills) {
        salesChartArea.getChildren().clear();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        barChart.setTitle(null);
        barChart.setLegendVisible(false);
        barChart.setAnimated(true);
        barChart.getStyleClass().add("sales-bar-chart");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");

        // Last 7 days sales
        Map<LocalDate, Double> dailySales = new TreeMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            dailySales.put(today.minusDays(i), 0.0);
        }

        for (Bill b : allBills) {
            LocalDate date = b.getBillDate().toLocalDate();
            if (dailySales.containsKey(date)) {
                dailySales.put(date, dailySales.get(date) + b.getTotalAmount());
            }
        }

        dailySales.forEach((date, amount) -> {
            series.getData().add(new XYChart.Data<>(date.format(dayFormatter), amount));
        });

        barChart.getData().add(series);
        barChart.setPrefHeight(250);

        // CSS for chart styling (if not in CSS file)
        barChart.lookupAll(".default-color0.chart-bar").forEach(n -> n.setStyle("-fx-bar-fill: #3b82f6;"));

        salesChartArea.getChildren().add(barChart);
    }

    private void loadTopProducts(List<Bill> allBills) {
        topProductsList.getChildren().clear();
        Map<String, Integer> productSales = new HashMap<>();

        for (Bill b : allBills) {
            for (BillItem item : b.getItems()) {
                String name = item.getProduct().getName();
                productSales.put(name, productSales.getOrDefault(name, 0) + item.getQuantity());
            }
        }

        productSales.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .forEach(entry -> {
                    HBox row = new HBox(10);
                    row.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 8 0;");

                    Label rank = new Label(String.valueOf(topProductsList.getChildren().size() + 1) + ".");
                    rank.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 13px; -fx-min-width: 25;");

                    Label name = new Label(entry.getKey());
                    name.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

                    Label qty = new Label(entry.getValue() + " sold");
                    qty.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 12px;");

                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    row.getChildren().addAll(rank, name, spacer, qty);
                    topProductsList.getChildren().add(row);
                });

        if (topProductsList.getChildren().isEmpty()) {
            Label empty = new Label("No sales data yet");
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 13px;");
            topProductsList.getChildren().add(empty);
        }
    }

    private void loadStockAlerts(List<InventoryItem> lowStockItems) {
        alertsList.getChildren().clear();
        alertCountLabel.setText(lowStockItems.size() + " alerts");

        for (InventoryItem item : lowStockItems) {
            VBox alertItem = new VBox(3);
            alertItem.getStyleClass().add("alert-item");

            Label name = new Label(item.getProduct().getName());
            name.getStyleClass().add("alert-item-name");

            Label stock;
            if (item.isOverStock()) {
                stock = new Label(
                        "Over Stock: " + item.getStockQuantity() + " (Max: " + item.getMaxStockThreshold() + ")");
                stock.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 11px;"); // orange-ish for overstock
            } else {
                stock = new Label(
                        "Low Stock: " + item.getStockQuantity() + " (Min: " + item.getMinStockThreshold() + ")");
                stock.getStyleClass().add("alert-item-stock");
            }

            alertItem.getChildren().addAll(name, stock);
            alertsList.getChildren().add(alertItem);
        }

        if (lowStockItems.isEmpty()) {
            Label empty = new Label("No stock alerts");
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 13px;");
            alertsList.getChildren().add(empty);
        }
    }
}
