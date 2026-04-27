package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleStringProperty;
import facade.SystemFacade;
import model.Employee;

import java.util.Optional;

public class EmployeesController implements FacadeAware {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colEmpId;
    @FXML private TableColumn<Employee, String> colEmpName;
    @FXML private TableColumn<Employee, String> colEmpRole;
    @FXML private TableColumn<Employee, String> colEmpPhone;
    @FXML private TableColumn<Employee, String> colEmpStatus;
    @FXML private TableColumn<Employee, Void> colEmpActions;
    @FXML private TextField searchField;

    private SystemFacade systemFacade;
    private ObservableList<Employee> empList = FXCollections.observableArrayList();

    public void initialize() {
        colEmpId.setCellValueFactory(c -> new SimpleStringProperty("EMP" + String.format("%03d", c.getValue().getUserId())));
        colEmpName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colEmpRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));
        colEmpPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));

        colEmpStatus.setCellFactory(col -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Employee emp = getTableRow().getItem();
                    Label badge = new Label(emp.isActive() ? "Active" : "Inactive");
                    badge.getStyleClass().add(emp.isActive() ? "badge-active" : "badge-inactive");
                    setGraphic(badge);
                }
            }
        });

        colEmpActions.setCellFactory(col -> new TableCell<Employee, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deactBtn = new Button("Deactivate");
            {
                editBtn.getStyleClass().add("btn-table-action");
                deactBtn.getStyleClass().add("btn-danger");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deactBtn.setOnAction(e -> handleDeactivate(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); }
                else { setGraphic(new HBox(5, editBtn, deactBtn)); }
            }
        });

        searchField.textProperty().addListener((obs, o, n) -> filterEmployees(n));
    }

    @Override
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
        refreshData();
    }

    private void refreshData() {
        empList.setAll(systemFacade.getAllEmployees());
        employeeTable.setItems(empList);
    }

    private void filterEmployees(String query) {
        if (query == null || query.isEmpty()) {
            employeeTable.setItems(empList);
        } else {
            employeeTable.setItems(empList.filtered(e ->
                    e.getFullName().toLowerCase().contains(query.toLowerCase()) ||
                    String.valueOf(e.getUserId()).contains(query)));
        }
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

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = systemFacade.deactivateUser(emp.getUserId());
            if (success) { refreshData(); }
            else { showAlert("Error", "Failed to deactivate."); }
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setContentText(msg); a.showAndWait();
    }
}
