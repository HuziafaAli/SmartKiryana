package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import facade.SystemFacade;
import model.*;
import command.GenerateReportCommand;

import java.time.LocalDate;
import java.util.List;

public class ReportsController implements FacadeAware {

    @FXML private Text pageTitle;
    @FXML private ComboBox<Employee> employeeSelector;
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
    @FXML private Label summaryLabel;

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
            // Employee sees own performance only
            currentEmployee = (Employee) user;
            employeeSelector.setVisible(false);
            empNameLabel.setText(user.getFullName());
            empRoleLabel.setText("Cashier");
            pageTitle.setText("My Performance");
            handleGenerate();
        } else {
            // Admin can select any employee
            employeeSelector.setVisible(true);
            List<Employee> employees = facade.getAllEmployees();
            employeeSelector.setItems(FXCollections.observableArrayList(employees));

            // Custom display
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
        if (currentEmployee == null) {
            showAlert("Select Employee", "Please select an employee first.");
            return;
        }

        int month = monthSelector.getSelectionModel().getSelectedIndex() + 1;
        int year = Integer.parseInt(yearSelector.getValue());

        List<Bill> allBills = systemFacade.getAllBills();

        // Use Command Pattern
        GenerateReportCommand cmd = new GenerateReportCommand(
                systemFacade.getReportController(), currentEmployee, 2, month, year);
        cmd.setBill(allBills);
        systemFacade.executeCommand(cmd);

        PerformanceReport report = cmd.getPerformanceReport();

        if (report != null) {
            // Count transactions for this employee in the month
            long txCount = allBills.stream()
                    .filter(b -> b.getUser() != null && b.getUser().getUserId() == currentEmployee.getUserId())
                    .filter(b -> b.getBillDate().getMonthValue() == month && b.getBillDate().getYear() == year)
                    .count();

            double avgBill = txCount > 0 ? report.getTotalSales() / txCount : 0;

            totalSalesLabel.setText(String.format("Rs. %,.0f", report.getTotalSales()));
            transactionsLabel.setText(String.valueOf(txCount));
            avgBillLabel.setText(String.format("Rs. %,.2f", avgBill));

            double pct = report.getAchievementPercentage();
            progressPercent.setText(String.format("%.0f%%", pct));
            targetAmountLabel.setText(String.format("Rs. %,.0f", report.getTargetAmount()));
            salesTargetInfo.setText(String.format("Rs. %,.0f / Rs. %,.0f", report.getTotalSales(), report.getTargetAmount()));
            achievementLabel.setText(String.format("%.1f%%", pct));
            bonusLabel.setText(String.format("Rs. %,.0f", report.getBonusAmount()));

            // Update progress circle border color based on achievement
            String borderColor;
            if (pct >= 100) borderColor = "#10b981";
            else if (pct >= 50) borderColor = "#3b82f6";
            else borderColor = "#ef4444";

            progressPercent.getParent().setStyle(
                    "-fx-background-color: #141929; -fx-background-radius: 200; " +
                    "-fx-min-width: 180; -fx-min-height: 180; -fx-max-width: 180; -fx-max-height: 180; " +
                    "-fx-alignment: CENTER; -fx-border-color: " + borderColor + "; -fx-border-width: 6; -fx-border-radius: 200;");

            summaryLabel.setText(String.format(
                    "%s achieved Rs. %,.0f out of Rs. %,.0f target (%.1f%%) in %s %d.\n" +
                    "Total transactions: %d | Average bill: Rs. %,.2f\n" +
                    "Bonus earned: Rs. %,.0f",
                    currentEmployee.getFullName(), report.getTotalSales(), report.getTargetAmount(),
                    pct, MONTHS[month - 1], year, txCount, avgBill, report.getBonusAmount()));
        } else {
            summaryLabel.setText("No performance data available for the selected period.");
            totalSalesLabel.setText("Rs. 0");
            transactionsLabel.setText("0");
            avgBillLabel.setText("Rs. 0");
            progressPercent.setText("0%");
            targetAmountLabel.setText("Rs. 0");
            achievementLabel.setText("0%");
            bonusLabel.setText("Rs. 0");
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setContentText(msg); a.showAndWait();
    }
}
