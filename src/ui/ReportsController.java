package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import facade.SystemFacade;
import model.*;
import command.GenerateReportCommand;

import java.time.LocalDate;
import java.util.List;

public class ReportsController implements FacadeAware {

    @FXML private Text pageTitle;
    @FXML private TabPane reportTabs;
    @FXML private ComboBox<Employee> employeeSelector;
    @FXML private HBox employeeSelectionBox;
    @FXML private ComboBox<String> monthSelector;
    @FXML private ComboBox<String> yearSelector;

    @FXML private Label empNameLabel;
    @FXML private Label empRoleLabel;
    @FXML private Label totalSalesLabel;
    @FXML private Label transactionsLabel;
    @FXML private Label avgBillLabel;
    @FXML private Label progressPercent;
    @FXML private Label salesTargetInfo;
    @FXML private Label targetAmountLabel;
    @FXML private Label achievementLabel;
    @FXML private Label bonusLabel;
    @FXML private Label peakSaleLabel;
    @FXML private Label summaryLabel;

    @FXML private Label monthlyTotalSales;
    @FXML private Label monthlyTotalReturns;
    @FXML private Label monthlyNetRevenue;
    @FXML private Label monthlyAvgDaily;
    @FXML private VBox categoryBreakdownBox;
    @FXML private FlowPane topProductsGrid;

    private SystemFacade systemFacade;
    private Employee currentEmployee;

    private static final String[] MONTHS = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    public void initialize() {
        monthSelector.setItems(FXCollections.observableArrayList(MONTHS));
        monthSelector.setValue(MONTHS[LocalDate.now().getMonthValue() - 1]);

        int currentYear = LocalDate.now().getYear();
        yearSelector.setItems(FXCollections.observableArrayList(
                String.valueOf(currentYear - 1), String.valueOf(currentYear), String.valueOf(currentYear + 1)));
        yearSelector.setValue(String.valueOf(currentYear));
    }

    @Override
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
        User user = facade.getCurrentUser();

        if (user != null && user.getRole().equalsIgnoreCase("EMPLOYEE")) {
            currentEmployee = (Employee) user;
            employeeSelectionBox.setVisible(false);
            employeeSelectionBox.setManaged(false);
            
            reportTabs.getTabs().removeIf(tab -> tab.getText().equals("Monthly Report"));
            
            empNameLabel.setText(user.getFullName());
            empRoleLabel.setText("Cashier");
            pageTitle.setText("My Performance");
            handleGenerate();
        } else {
            employeeSelectionBox.setVisible(true);
            employeeSelectionBox.setManaged(true);
            List<Employee> employees = facade.getAllEmployees();
            employeeSelector.setItems(FXCollections.observableArrayList(employees));

            employeeSelector.setCellFactory(lv -> new ListCell<Employee>() {
                @Override
                protected void updateItem(Employee e, boolean empty) {
                    super.updateItem(e, empty);
                    setText(empty || e == null ? "" : e.getFullName());
                }
            });
            employeeSelector.setButtonCell(new ListCell<Employee>() {
                @Override
                protected void updateItem(Employee e, boolean empty) {
                    super.updateItem(e, empty);
                    setText(empty || e == null ? "" : e.getFullName());
                }
            });

            if (!employees.isEmpty()) {
                employeeSelector.setValue(employees.get(0));
                currentEmployee = employees.get(0);
                empNameLabel.setText(currentEmployee.getFullName());
                empRoleLabel.setText("Employee");
            }

            employeeSelector.setOnAction(e -> {
                currentEmployee = employeeSelector.getValue();
                if (currentEmployee != null) {
                    empNameLabel.setText(currentEmployee.getFullName());
                }
            });
        }
    }

    @FXML
    private void handleGenerate() {
        int month = monthSelector.getSelectionModel().getSelectedIndex() + 1;
        int year = Integer.parseInt(yearSelector.getValue());

        if (reportTabs.getSelectionModel().getSelectedIndex() == 0) {
            generatePerformanceReport(month, year);
        } else {
            generateMonthlyReport(month, year);
        }
    }

    // Generates a PDF version of the monthly report and opens a save dialog
    @FXML
    private void handleExportPDF() {
        int monthIndex = monthSelector.getSelectionModel().getSelectedIndex() + 1;
        int year = Integer.parseInt(yearSelector.getValue());
        String monthName = monthSelector.getValue();

        if (reportTabs.getSelectionModel().getSelectedIndex() == 0) {
            showAlert("Unsupported", "PDF Export is currently only available for the Monthly Report tab.");
            return;
        }

        List<Bill> allBills = systemFacade.getAllBills();
        List<ReturnTransaction> allReturns = systemFacade.getAllReturns();

        GenerateReportCommand cmd = new GenerateReportCommand(
                systemFacade.getReportController(), null, 1, monthIndex, year);
        cmd.setBill(allBills);
        cmd.setReturn(allReturns);
        systemFacade.executeCommand(cmd);

        MonthlyReport report = cmd.getMonthlyReport();
        if (report != null) {
            String path = util.ReportPDFService.exportMonthlyReport(report, monthName, year);
            if (path != null) {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Export Success");
                a.setContentText("Monthly report exported successfully to:\n" + path);
                DialogStyler.style(a);
                a.showAndWait();
            } else {
                showAlert("Export Failed", "Could not generate PDF report.");
            }
        } else {
            showAlert("No Data", "No data available for the selected month to export.");
        }
    }

    // Calculates and displays employee performance against their monthly target
    private void generatePerformanceReport(int month, int year) {
        if (currentEmployee == null) {
            showAlert("Select Employee", "Please select an employee first.");
            return;
        }

        List<Bill> allBills = systemFacade.getAllBills();
        GenerateReportCommand cmd = new GenerateReportCommand(
                systemFacade.getReportController(), currentEmployee, 2, month, year);
        cmd.setBill(allBills);
        systemFacade.executeCommand(cmd);

        PerformanceReport report = cmd.getPerformanceReport();

        if (report != null) {
            long txCount = allBills.stream()
                    .filter(b -> b.getUser() != null && b.getUser().getUserId() == currentEmployee.getUserId())
                    .filter(b -> b.getBillDate().getMonthValue() == month && b.getBillDate().getYear() == year)
                    .count();

            double avgBill = txCount > 0 ? report.getTotalSales() / txCount : 0;

            totalSalesLabel.setText(String.format("Rs. %,.0f", report.getTotalSales()));
            transactionsLabel.setText(String.valueOf(txCount));
            avgBillLabel.setText(String.format("Rs. %,.2f", avgBill));

            double pct = report.getAchievementPercentage();
            double displayPct = Math.min(100.0, pct);
            progressPercent.setText(String.format("%.0f%%", displayPct));
            targetAmountLabel.setText(String.format("Rs. %,.0f", report.getTargetAmount()));
            salesTargetInfo.setText(String.format("Rs. %,.0f / Rs. %,.0f", report.getTotalSales(), report.getTargetAmount()));
            achievementLabel.setText(String.format("%.1f%%", displayPct));
            bonusLabel.setText(String.format("Rs. %,.0f", report.getBonusAmount()));

            String borderColor = (pct >= 100) ? "#10b981" : (pct >= 50) ? "#3b82f6" : "#ef4444";
            progressPercent.getParent().setStyle(
                    "-fx-background-color: #141929; -fx-background-radius: 200; " +
                    "-fx-min-width: 180; -fx-min-height: 180; -fx-max-width: 180; -fx-max-height: 180; " +
                    "-fx-alignment: CENTER; -fx-border-color: " + borderColor + "; -fx-border-width: 6; -fx-border-radius: 200;");

            double peakSale = allBills.stream()
                    .filter(b -> b.getUser() != null && b.getUser().getUserId() == currentEmployee.getUserId())
                    .filter(b -> b.getBillDate().getMonthValue() == month && b.getBillDate().getYear() == year)
                    .mapToDouble(Bill::getTotalAmount)
                    .max().orElse(0);
            peakSaleLabel.setText(String.format("Rs. %,.0f", peakSale));

            double bonusAmount = report.getBonusAmount();
            String status;
            if (report.getTargetAmount() <= 0) {
                status = "Status: No sales target assigned for this month.";
            } else if (bonusAmount > 0) {
                status = String.format("Status: Great job! You've earned a bonus of Rs. %,.0f. " +
                        "Every additional sale increases your bonus by 5%% of the amount!", bonusAmount);
            } else {
                double remaining = Math.max(0, report.getTargetAmount() - report.getTotalSales());
                if (remaining > 0) {
                    status = String.format("Status: No bonus yet. You need Rs. %,.0f more in sales " +
                            "to hit your target and start earning a 5%% bonus!", remaining);
                } else {
                    status = "Status: Target reached! Your next sale will start earning you a 5% bonus!";
                }
            }

            summaryLabel.setText(String.format(
                    "- Performance Score: %.1f%%\n" +
                    "- Transactions Processed: %d\n" +
                    "- Average Transaction Value: Rs. %,.2f\n" +
                    "- Monthly Peak Sale: Rs. %,.0f\n" +
                    "- Total Sales Contribution: Rs. %,.0f\n\n" +
                    "%s",
                    pct, txCount, avgBill, peakSale, report.getTotalSales(), status));
        } else {
            summaryLabel.setText("No performance data available for the selected period.");
            clearPerformanceUI();
        }
    }

    private void clearPerformanceUI() {
        totalSalesLabel.setText("Rs. 0");
        transactionsLabel.setText("0");
        avgBillLabel.setText("Rs. 0");
        progressPercent.setText("0%");
        targetAmountLabel.setText("Rs. 0");
        achievementLabel.setText("0%");
        bonusLabel.setText("Rs. 0");
    }

    // Calculates overall store sales, returns, and lists top performing products
    private void generateMonthlyReport(int month, int year) {
        List<Bill> allBills = systemFacade.getAllBills();
        List<ReturnTransaction> allReturns = systemFacade.getAllReturns();

        GenerateReportCommand cmd = new GenerateReportCommand(
                systemFacade.getReportController(), null, 1, month, year);
        cmd.setBill(allBills);
        cmd.setReturn(allReturns);
        systemFacade.executeCommand(cmd);

        MonthlyReport report = cmd.getMonthlyReport();

        if (report != null) {
            monthlyTotalSales.setText(String.format("Rs. %,.0f", report.getTotalSales()));
            monthlyTotalReturns.setText(String.format("Rs. %,.0f", report.getTotalReturns()));
            monthlyNetRevenue.setText(String.format("Rs. %,.0f", report.getNetRevenue()));
            
            int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
            double avgDaily = report.getTotalSales() / daysInMonth;
            monthlyAvgDaily.setText(String.format("Rs. %,.0f", avgDaily));

            renderTopProductsGrid(report.getTopProducts());
            populateCategoryBreakdown(allBills, month, year);
        } else {
            monthlyTotalSales.setText("Rs. 0");
            monthlyTotalReturns.setText("Rs. 0");
            monthlyNetRevenue.setText("Rs. 0");
            monthlyAvgDaily.setText("Rs. 0");
            topProductsGrid.getChildren().clear();
            categoryBreakdownBox.getChildren().clear();
        }
    }

    private void renderTopProductsGrid(List<Product> products) {
        topProductsGrid.getChildren().clear();
        for (Product p : products) {
            VBox card = new VBox(8);
            card.setStyle("-fx-background-color: #1e293b; -fx-padding: 15; -fx-background-radius: 10; -fx-min-width: 180; -fx-max-width: 180; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");
            
            Label nameLabel = new Label(p.getName());
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            nameLabel.setWrapText(true);
            nameLabel.setMinHeight(40);
            
            Label catLabel = new Label(p.getCategory() != null ? p.getCategory().getCategoryName() : "General");
            catLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-background-color: rgba(255,255,255,0.05); -fx-padding: 2 6; -fx-background-radius: 4;");
            
            HBox priceQty = new HBox(10);
            Label price = new Label(String.format("Rs. %,.0f", p.getPrice()));
            price.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label qty = new Label(p.getSalesQuantity() + " sold");
            qty.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px;");
            
            priceQty.getChildren().addAll(price, spacer, qty);
            
            card.getChildren().addAll(catLabel, nameLabel, priceQty);
            topProductsGrid.getChildren().add(card);
        }
    }

    private void populateCategoryBreakdown(List<Bill> allBills, int month, int year) {
        categoryBreakdownBox.getChildren().clear();
        java.util.Map<String, Double> catSales = new java.util.HashMap<>();
        
        allBills.stream()
            .filter(b -> b.getBillDate().getMonthValue() == month && b.getBillDate().getYear() == year)
            .forEach(b -> {
                for (BillItem item : b.getItems()) {
                    String catName = item.getProduct().getCategory() != null ? item.getProduct().getCategory().getCategoryName() : "General";
                    catSales.put(catName, catSales.getOrDefault(catName, 0.0) + item.getSubtotal());
                }
            });
            
        double total = catSales.values().stream().mapToDouble(Double::doubleValue).sum();
        
        catSales.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .forEach(entry -> {
                double percent = total > 0 ? (entry.getValue() / total) * 100 : 0;
                
                VBox item = new VBox(5);
                HBox top = new HBox();
                Label name = new Label(entry.getKey());
                name.setStyle("-fx-text-fill: #e2e8f0; -fx-font-weight: bold;");
                Region s = new Region();
                HBox.setHgrow(s, Priority.ALWAYS);
                Label val = new Label(String.format("Rs. %,.0f", entry.getValue()));
                val.setStyle("-fx-text-fill: #94a3b8;");
                top.getChildren().addAll(name, s, val);
                
                ProgressBar pb = new ProgressBar(percent / 100.0);
                pb.setMaxWidth(Double.MAX_VALUE);
                pb.setPrefHeight(6);
                pb.setStyle("-fx-accent: #3b82f6;");
                
                item.getChildren().addAll(top, pb);
                categoryBreakdownBox.getChildren().add(item);
            });
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setContentText(msg); DialogStyler.style(a); a.showAndWait();
    }
}
