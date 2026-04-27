package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import facade.SystemFacade;
import model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmployeesController implements FacadeAware {

    @FXML private FlowPane employeeGrid;
    @FXML private TextField searchField;

    private SystemFacade systemFacade;
    private final ObservableList<Employee> empList = FXCollections.observableArrayList();

    public void initialize() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterEmployees(newValue));
    }

    @Override
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
        refreshData();
    }

    private void refreshData() {
        empList.setAll(systemFacade.getAllEmployees());
        renderEmployees(empList);
    }

    private void filterEmployees(String query) {
        if (query == null || query.trim().isEmpty()) {
            renderEmployees(empList);
            return;
        }

        String q = query.toLowerCase();
        List<Employee> filtered = empList.stream()
                .filter(e -> e.getFullName().toLowerCase().contains(q)
                        || e.getUsername().toLowerCase().contains(q)
                        || e.getPhone().contains(q)
                        || String.valueOf(e.getUserId()).contains(q))
                .collect(Collectors.toList());
        renderEmployees(filtered);
    }

    private void renderEmployees(List<Employee> employees) {
        employeeGrid.getChildren().clear();

        if (employees.isEmpty()) {
            Label empty = new Label("No employees found.");
            empty.getStyleClass().add("return-empty-state");
            empty.setPrefWidth(420);
            empty.setAlignment(Pos.CENTER);
            employeeGrid.getChildren().add(empty);
            return;
        }

        for (Employee employee : employees) {
            employeeGrid.getChildren().add(createEmployeeCard(employee));
        }
    }

    private VBox createEmployeeCard(Employee emp) {
        VBox card = new VBox(0);
        card.getStyleClass().add("employee-card");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(getInitials(emp.getFullName()));
        avatar.getStyleClass().add("employee-avatar");

        VBox identity = new VBox(3);
        HBox.setHgrow(identity, Priority.ALWAYS);

        Label name = new Label(emp.getFullName());
        name.getStyleClass().add("employee-name");
        name.setWrapText(true);
        name.setMaxWidth(190);

        Label id = new Label("EMP" + String.format("%03d", emp.getUserId()) + " - " + emp.getUsername());
        id.getStyleClass().add("inv-card-barcode");

        identity.getChildren().addAll(name, id);

        Label status = new Label(emp.isActive() ? "Active" : "Inactive");
        status.getStyleClass().add(emp.isActive() ? "badge-active" : "badge-inactive");

        top.getChildren().addAll(avatar, identity, status);

        HBox details = new HBox(8,
                metric("Role", emp.getRole(), 96),
                metric("Phone", emp.getPhone(), 132),
                metric("CNIC", emp.getCnic() == null ? "N/A" : emp.getCnic(), 150));
        VBox.setMargin(details, new javafx.geometry.Insets(14, 0, 14, 0));

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-table-action");
        editBtn.setOnAction(e -> handleEdit(emp));

        Button deactivateBtn = new Button("Deactivate");
        deactivateBtn.getStyleClass().add("btn-danger");
        deactivateBtn.setDisable(!emp.isActive());
        deactivateBtn.setOnAction(e -> handleDeactivate(emp));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(8, editBtn, spacer, deactivateBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(top, divider(), details, divider(), actions);
        return card;
    }

    private VBox metric(String labelText, String valueText, double width) {
        VBox box = new VBox(3);
        box.getStyleClass().add("employee-mini-metric");
        box.setPrefWidth(width);
        box.setMinWidth(width);
        box.setMaxWidth(width);
        Label label = new Label(labelText);
        label.getStyleClass().add("inv-card-field-label");
        Label value = new Label(valueText);
        value.getStyleClass().add("employee-metric-value");
        value.setWrapText(false);
        value.setMaxWidth(width - 16);
        box.getChildren().addAll(label, value);
        return box;
    }

    private Region divider() {
        Region region = new Region();
        region.getStyleClass().add("inv-card-divider");
        return region;
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String second = parts.length > 1 ? parts[1].substring(0, 1) : "";
        return (first + second).toUpperCase();
    }

    @FXML
    private void handleAddEmployee() {
        TextField usernameF = new TextField(); usernameF.setPromptText("Username");
        TextField passwordF = new TextField(); passwordF.setPromptText("Password");
        TextField nameF = new TextField(); nameF.setPromptText("Full Name");
        TextField phoneF = new TextField(); phoneF.setPromptText("Phone (03XX-XXXXXXX)");
        TextField cnicF = new TextField(); cnicF.setPromptText("CNIC (XXXXX-XXXXXXX-X)");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Username:"), 0, 0); grid.add(usernameF, 1, 0);
        grid.add(new Label("Password:"), 0, 1); grid.add(passwordF, 1, 1);
        grid.add(new Label("Full Name:"), 0, 2); grid.add(nameF, 1, 2);
        grid.add(new Label("Phone:"), 0, 3); grid.add(phoneF, 1, 3);
        grid.add(new Label("CNIC:"), 0, 4); grid.add(cnicF, 1, 4);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Employee");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogStyler.style(dialog);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = systemFacade.addEmployee(usernameF.getText(), passwordF.getText(),
                    nameF.getText(), phoneF.getText(), cnicF.getText());
            if (success) { refreshData(); }
            else { showAlert("Error", "Failed to add employee. Check inputs and permissions."); }
        }
    }

    private void handleEdit(Employee emp) {
        TextField nameF = new TextField(emp.getFullName());
        TextField phoneF = new TextField(emp.getPhone());
        TextField usernameF = new TextField(emp.getUsername());
        TextField passwordF = new TextField(emp.getPassword());

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Full Name:"), 0, 0); grid.add(nameF, 1, 0);
        grid.add(new Label("Phone:"), 0, 1); grid.add(phoneF, 1, 1);
        grid.add(new Label("Username:"), 0, 2); grid.add(usernameF, 1, 2);
        grid.add(new Label("Password:"), 0, 3); grid.add(passwordF, 1, 3);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Employee - " + emp.getFullName());
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogStyler.style(dialog);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = systemFacade.updateEmployee(emp.getUserId(), nameF.getText(),
                    phoneF.getText(), usernameF.getText(), passwordF.getText());
            if (success) { refreshData(); }
            else { showAlert("Error", "Failed to update employee."); }
        }
    }

    private void handleDeactivate(Employee emp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Deactivate Employee");
        confirm.setContentText("Deactivate " + emp.getFullName() + "?");
        DialogStyler.style(confirm);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = systemFacade.deactivateUser(emp.getUserId());
            if (success) { refreshData(); }
            else { showAlert("Error", "Failed to deactivate."); }
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
