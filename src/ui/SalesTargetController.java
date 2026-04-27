package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import facade.SystemFacade;
import model.*;

import java.time.LocalDate;
import java.util.List;

public class SalesTargetController implements FacadeAware {

    @FXML private ComboBox<Employee> empSelector;
    @FXML private ComboBox<String> monthSelector;
    @FXML private ComboBox<String> yearSelector;
    @FXML private TextField targetField;

    @FXML private TableView<PerformanceReport> targetsTable;
    @FXML private TableColumn<PerformanceReport, String> colMonth;
    @FXML private TableColumn<PerformanceReport, String> colTarget;
    @FXML private TableColumn<PerformanceReport, String> colAchieved;
    @FXML private TableColumn<PerformanceReport, String> colProgress;

    @FXML private Label selectedEmpLabel;
    @FXML private Label progressLabel;
    @FXML private Label progressInfoLabel;
    @FXML private Label currentTargetLabel;
    @FXML private Label remainingLabel;

    private SystemFacade systemFacade;

    private static final String[] MONTHS = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    public void initialize() {
        monthSelector.setItems(FXCollections.observableArrayList(MONTHS));
        monthSelector.setValue(MONTHS[LocalDate.now().getMonthValue() - 1]);

        int year = LocalDate.now().getYear();
        yearSelector.setItems(FXCollections.observableArrayList(
                String.valueOf(year - 1), String.valueOf(year), String.valueOf(year + 1)));
        yearSelector.setValue(String.valueOf(year));

        colMonth.setCellValueFactory(c -> new SimpleStringProperty(MONTHS[c.getValue().getMonth() - 1] + " " + c.getValue().getYear()));
        colTarget.setCellValueFactory(c -> new SimpleStringProperty(String.format("Rs. %,.0f", c.getValue().getTargetAmount())));
        colAchieved.setCellValueFactory(c -> new SimpleStringProperty(String.format("Rs. %,.0f", c.getValue().getTotalSales())));
        colProgress.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.0f%%", c.getValue().getAchievementPercentage())));
    }

    @Override
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;

        List<Employee> employees = facade.getAllEmployees();
        empSelector.setItems(FXCollections.observableArrayList(employees));

        empSelector.setCellFactory(lv -> new ListCell<Employee>() {
            @Override
            protected void updateItem(Employee e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? "" : e.getFullName() + " (EMP" + String.format("%03d", e.getUserId()) + ")");
            }
        });
        empSelector.setButtonCell(new ListCell<Employee>() {
            @Override
            protected void updateItem(Employee e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? "" : e.getFullName());
            }
        });

        empSelector.setOnAction(e -> loadEmployeeProgress());
    }

    private void loadEmployeeProgress() {
        Employee emp = empSelector.getValue();
        if (emp == null) return;

        selectedEmpLabel.setText(emp.getFullName());

        List<Bill> allBills = systemFacade.getAllBills();
        List<PerformanceReport> reports = systemFacade.getPerformanceComparison(
                java.util.Collections.singletonList(emp),
                monthSelector.getSelectionModel().getSelectedIndex() + 1,
                Integer.parseInt(yearSelector.getValue()),
                allBills);

        if (!reports.isEmpty()) {
            PerformanceReport current = reports.get(0);
            double pct = current.getAchievementPercentage();
            progressLabel.setText(String.format("%.0f%%", pct));
            currentTargetLabel.setText(String.format("Rs. %,.0f", current.getTargetAmount()));
            progressInfoLabel.setText(String.format("Rs. %,.0f / Rs. %,.0f", current.getTotalSales(), current.getTargetAmount()));

            double remaining = Math.max(0, current.getTargetAmount() - current.getTotalSales());
            remainingLabel.setText(String.format("Remaining: Rs. %,.0f", remaining));

            String borderColor = pct >= 100 ? "#10b981" : pct >= 50 ? "#3b82f6" : "#ef4444";
            progressLabel.getParent().setStyle(
                    "-fx-background-color: #141929; -fx-background-radius: 200; " +
                    "-fx-min-width: 180; -fx-min-height: 180; -fx-max-width: 180; -fx-max-height: 180; " +
                    "-fx-alignment: CENTER; -fx-border-color: " + borderColor + "; -fx-border-width: 6; -fx-border-radius: 200;");

            targetsTable.setItems(FXCollections.observableArrayList(reports));
        } else {
            progressLabel.setText("0%");
            currentTargetLabel.setText("Rs. 0");
            progressInfoLabel.setText("No target set");
            remainingLabel.setText("");
            targetsTable.getItems().clear();
        }
    }

    @FXML
    private void handleSetTarget() {
        Employee emp = empSelector.getValue();
        if (emp == null) {
            showAlert("Select Employee", "Please select an employee.");
            return;
        }

        String amountText = targetField.getText().trim();
        if (amountText.isEmpty()) {
            showAlert("Enter Amount", "Please enter a target amount.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            int month = monthSelector.getSelectionModel().getSelectedIndex() + 1;
            int year = Integer.parseInt(yearSelector.getValue());

            boolean success = systemFacade.assignTarget(emp, month, year, amount);
            if (success) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Success");
                info.setContentText("Target of Rs. " + String.format("%,.0f", amount) + " assigned to " + emp.getFullName());
                info.showAndWait();
                targetField.clear();
                loadEmployeeProgress();
            } else {
                showAlert("Failed", "Failed to assign target. Only admins can assign targets.");
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Amount", "Please enter a valid number.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setContentText(msg); a.showAndWait();
    }
}
