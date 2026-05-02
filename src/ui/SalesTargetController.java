package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import facade.SystemFacade;
import model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import javafx.concurrent.Task;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public class SalesTargetController implements FacadeAware {

    @FXML
    private ComboBox<Employee> empSelector;
    @FXML
    private ComboBox<String> monthSelector;
    @FXML
    private ComboBox<String> yearSelector;
    @FXML
    private TextField targetField;

    @FXML
    private Label selectedEmpLabel;
    @FXML
    private Label periodLabel;
    @FXML
    private Label progressLabel;
    @FXML
    private Label progressInfoLabel;
    @FXML
    private Label currentTargetLabel;
    @FXML
    private Label achievedLabel;
    @FXML
    private Label remainingLabel;
    @FXML
    private Label bonusLabel;
    @FXML
    private Label achievementMessageLabel;
    @FXML
    private Label historyCountLabel;
    @FXML
    private VBox progressRing;
    @FXML
    private FlowPane targetHistoryFlow;

    private List<Bill> allBillsCache;
    private List<SalesTarget> allTargetsCache;
    private List<PerformanceReport> currentHistoryReports;

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

        monthSelector.setOnAction(e -> loadEmployeeProgress());
        yearSelector.setOnAction(e -> loadEmployeeProgress());
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
                setText(empty || e == null ? ""
                        : e.getFullName() + " (EMP" + String.format("%03d", e.getUserId()) + ")");
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
        loadEmployeeProgress();
    }

    private void loadEmployeeProgress() {
        if (systemFacade == null || empSelector == null) {
            return;
        }

        Employee emp = empSelector.getValue();
        int month = Math.max(1, monthSelector.getSelectionModel().getSelectedIndex() + 1);
        int year = Integer.parseInt(yearSelector.getValue());
        periodLabel.setText(MONTHS[month - 1] + " " + year);

        if (emp == null) {
            selectedEmpLabel.setText("Select an employee");
            setEmptyProgress();
            renderTargetHistoryUI(null, null);
            return;
        }

        selectedEmpLabel.setText(emp.getFullName());

        if (allBillsCache == null || allTargetsCache == null) {
            progressLabel.setText("...");
            Task<Void> initialFetchTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    allBillsCache = systemFacade.getAllBills();
                    allTargetsCache = systemFacade.getAllTargets();
                    return null;
                }
            };
            initialFetchTask.setOnSucceeded(e -> {
                calculateAndShowProgressLocally(emp, month, year);
            });
            new Thread(initialFetchTask).start();
        } else {
            calculateAndShowProgressLocally(emp, month, year);
        }
    }

    private void calculateAndShowProgressLocally(Employee emp, int month, int year) {
        // Instant Calculation on UI thread or small background task
        double totalSales = 0;
        for (Bill b : allBillsCache) {
            if (b.getUser() != null && b.getUser().getUserId() == emp.getUserId() &&
                    b.getBillDate().getMonthValue() == month && b.getBillDate().getYear() == year) {
                totalSales += b.getTotalAmount();
            }
        }

        double targetAmt = 0;
        for (SalesTarget t : allTargetsCache) {
            if (t.getEmployee().getUserId() == emp.getUserId() && t.getMonth() == month && t.getYear() == year) {
                targetAmt = t.getTargetAmount();
                break;
            }
        }

        double bonus = (targetAmt > 0 && totalSales > targetAmt) ? (totalSales - targetAmt) * 0.05 : 0;
        PerformanceReport currentReport = new PerformanceReport(0, emp, month, year, totalSales, targetAmt, bonus);

        updateCurrentProgressUI(Collections.singletonList(currentReport));

        // History calculation locally
        List<PerformanceReport> history = new ArrayList<>();
        Set<String> seenPeriods = new HashSet<>();
        for (SalesTarget t : allTargetsCache) {
            if (t.getEmployee().getUserId() == emp.getUserId()) {
                String key = t.getMonth() + "-" + t.getYear();
                if (seenPeriods.add(key)) {
                    double hTotalSales = 0;
                    for (Bill b : allBillsCache) {
                        if (b.getUser() != null && b.getUser().getUserId() == emp.getUserId() &&
                                b.getBillDate().getMonthValue() == t.getMonth()
                                && b.getBillDate().getYear() == t.getYear()) {
                            hTotalSales += b.getTotalAmount();
                        }
                    }
                    double hBonus = (t.getTargetAmount() > 0 && hTotalSales > t.getTargetAmount())
                            ? (hTotalSales - t.getTargetAmount()) * 0.05
                            : 0;
                    history.add(new PerformanceReport(0, emp, t.getMonth(), t.getYear(), hTotalSales,
                            t.getTargetAmount(), hBonus));
                }
            }
        }
        currentHistoryReports = history;
        renderTargetHistoryUI(emp, currentHistoryReports);
    }

    private void updateCurrentProgressUI(List<PerformanceReport> reports) {
        if (!reports.isEmpty()) {
            PerformanceReport current = reports.get(0);
            double pct = current.getAchievementPercentage();
            double remaining = Math.max(0, current.getTargetAmount() - current.getTotalSales());

            progressLabel.setText(String.format("%.0f%%", pct));
            currentTargetLabel.setText(String.format("Rs. %,.0f", current.getTargetAmount()));
            achievedLabel.setText(String.format("Rs. %,.0f", current.getTotalSales()));
            remainingLabel.setText(String.format("Rs. %,.0f", remaining));
            bonusLabel.setText(String.format("Rs. %,.0f", current.getBonusAmount()));
            progressInfoLabel.setText(
                    String.format("Rs. %,.0f / Rs. %,.0f", current.getTotalSales(), current.getTargetAmount()));

            String borderColor = pct >= 100 ? "#10b981" : pct >= 50 ? "#3b82f6" : "#ef4444";
            progressRing.setStyle(
                    "-fx-background-color: #141929; -fx-background-radius: 200; " +
                            "-fx-min-width: 150; -fx-min-height: 150; -fx-max-width: 150; -fx-max-height: 150; " +
                            "-fx-alignment: CENTER; -fx-border-color: " + borderColor
                            + "; -fx-border-width: 6; -fx-border-radius: 200;");

            if (pct >= 100) {
                achievementMessageLabel.setText("Target achieved. Bonus is active for this period.");
            } else if (pct > 0) {
                achievementMessageLabel.setText("Keep tracking progress against the assigned target.");
            } else {
                achievementMessageLabel.setText("No sales recorded for this employee in the selected period.");
            }
        } else {
            setEmptyProgress();
        }
    }

    private void setEmptyProgress() {
        progressLabel.setText("0%");
        currentTargetLabel.setText("Rs. 0");
        achievedLabel.setText("Rs. 0");
        remainingLabel.setText("Rs. 0");
        bonusLabel.setText("Rs. 0");
        progressInfoLabel.setText("No target set");
        achievementMessageLabel.setText("Assign a target to start tracking progress.");
        progressRing.setStyle("");
    }

    private void renderTargetHistoryUI(Employee emp, List<PerformanceReport> reports) {
        targetHistoryFlow.getChildren().clear();

        if (emp == null) {
            historyCountLabel.setText("Select an employee");
            targetHistoryFlow.getChildren().add(emptyHistory("Select an employee to view assigned targets."));
            return;
        }

        historyCountLabel.setText(reports.size() + (reports.size() == 1 ? " target" : " targets"));

        if (reports.isEmpty()) {
            targetHistoryFlow.getChildren().add(emptyHistory("No targets assigned to this employee yet."));
            return;
        }

        for (PerformanceReport report : reports) {
            targetHistoryFlow.getChildren().add(createTargetHistoryCard(report));
        }
    }

    private VBox createTargetHistoryCard(PerformanceReport report) {
        VBox card = new VBox(0);
        card.getStyleClass().add("target-history-card");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox title = new VBox(3);
        HBox.setHgrow(title, Priority.ALWAYS);

        Label period = new Label(MONTHS[report.getMonth() - 1] + " " + report.getYear());
        period.getStyleClass().add("target-history-period");

        Label target = new Label("Target: Rs. " + String.format("%,.0f", report.getTargetAmount()));
        target.getStyleClass().add("text-muted");

        Label pct = new Label(String.format("%.0f%%", Math.min(100.0, report.getAchievementPercentage())));
        pct.getStyleClass().add(report.getAchievementPercentage() >= 100 ? "badge-active" : "badge-info");

        title.getChildren().addAll(period, target);
        top.getChildren().addAll(title, pct);

        HBox metrics = new HBox(8,
                historyMetric("Achieved", String.format("Rs. %,.0f", report.getTotalSales()), false),
                historyMetric("Remaining",
                        String.format("Rs. %,.0f", Math.max(0, report.getTargetAmount() - report.getTotalSales())),
                        true),
                historyMetric("Bonus", String.format("Rs. %,.0f", report.getBonusAmount()), false));
        VBox.setMargin(metrics, new javafx.geometry.Insets(12, 0, 0, 0));

        card.getChildren().addAll(top, divider(), metrics);
        return card;
    }

    private VBox historyMetric(String labelText, String valueText, boolean warning) {
        VBox box = new VBox(3);
        box.getStyleClass().add("target-history-metric");
        Label label = new Label(labelText);
        label.getStyleClass().add("inv-card-field-label");
        Label value = new Label(valueText);
        value.getStyleClass().add(warning ? "target-history-warning" : "target-history-value");
        box.getChildren().addAll(label, value);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private Region divider() {
        Region region = new Region();
        region.getStyleClass().add("inv-card-divider");
        VBox.setMargin(region, new javafx.geometry.Insets(12, 0, 0, 0));
        return region;
    }

    private Label emptyHistory(String message) {
        Label empty = new Label(message);
        empty.getStyleClass().add("return-empty-state");
        empty.setPrefWidth(460);
        empty.setAlignment(Pos.CENTER);
        return empty;
    }

    @FXML
    private void handleSetTarget() {
        Employee emp = empSelector.getValue();
        if (emp == null) {
            showAlert("Select Employee", "Please select an employee.");
            return;
        }

        if (!emp.isActive()) {
            showAlert("Inactive Employee", "First activate the employee.");
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

            // Optimistic UI Update
            SalesTarget newTarget = new SalesTarget(0, emp, month, year, amount);

            // Remove old target for same period if exists in cache
            if (allTargetsCache != null) {
                allTargetsCache.removeIf(t -> t.getEmployee().getUserId() == emp.getUserId() && t.getMonth() == month
                        && t.getYear() == year);
                allTargetsCache.add(0, newTarget);
            }

            // Instantly show progress from local cache
            calculateAndShowProgressLocally(emp, month, year);

            // Background update
            Task<Boolean> assignTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return systemFacade.assignTarget(emp, month, year, amount);
                }
            };
            assignTask.setOnSucceeded(e -> {
                if (assignTask.getValue()) {
                    targetField.clear();
                    // Silently refresh the real target IDs and database state in background
                    Task<Void> refreshTask = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            allTargetsCache = systemFacade.getAllTargets();
                            return null;
                        }
                    };
                    new Thread(refreshTask).start();
                } else {
                    showAlert("Failed", "Failed to assign target. Only admins can assign targets.");
                    loadEmployeeProgress(); // Revert on failure
                }
            });
            new Thread(assignTask).start();

        } catch (NumberFormatException e) {
            showAlert("Invalid Amount", "Please enter a valid number.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(msg);
        DialogStyler.style(alert);
        alert.showAndWait();
    }
}
