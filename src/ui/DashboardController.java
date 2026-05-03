package ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import facade.SystemFacade;
import model.User;

import java.io.IOException;

public class DashboardController {

    @FXML
    private StackPane contentArea;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;

    @FXML
    private Button navDashboard;
    @FXML
    private Button navPOS;
    @FXML
    private Button navInventory;
    @FXML
    private Button navSales;
    @FXML
    private Button navEmployees;
    @FXML
    private Button navReports;
    @FXML
    private Button navSalesTarget;

    private Button activeNavButton;
    private SystemFacade systemFacade;

    // Configures sidebar visibility based on user role and loads the home view
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
        User user = facade.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getFullName());
            userRoleLabel.setText(user.getRole());

            if (user.getRole().equalsIgnoreCase("EMPLOYEE")) {
                navEmployees.setVisible(false);
                navEmployees.setManaged(false);
                navSalesTarget.setVisible(false);
                navSalesTarget.setManaged(false);
            }
        }

        showDashboardHome();
    }

    @FXML
    private void showDashboardHome() {
        setActiveNav(navDashboard);
        loadView("DashboardHome.fxml");
    }

    @FXML
    private void showPOS() {
        setActiveNav(navPOS);
        loadView("POS.fxml");
    }

    @FXML
    private void showInventory() {
        setActiveNav(navInventory);
        loadView("Inventory.fxml");
    }

    @FXML
    private void showSales() {
        setActiveNav(navSales);
        loadView("BillHistory.fxml");
    }

    @FXML
    private void showEmployees() {
        setActiveNav(navEmployees);
        loadView("Employees.fxml");
    }

    @FXML
    private void showReports() {
        setActiveNav(navReports);
        loadView("Reports.fxml");
    }

    @FXML
    private void showSalesTarget() {
        setActiveNav(navSalesTarget);
        loadView("SalesTarget.fxml");
    }

    private void setActiveNav(Button button) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-button-active");
        }
        activeNavButton = button;
        if (!button.getStyleClass().contains("nav-button-active")) {
            button.getStyleClass().add("nav-button-active");
        }
    }

    // Logs out and returns to the login screen
    @FXML
    private void handleLogout() {
        try {
            systemFacade.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Login.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setSystemFacade(systemFacade);

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loads an FXML view into the content area and injects the facade
    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/" + fxmlFile));
            Node view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof FacadeAware) {
                ((FacadeAware) controller).setSystemFacade(systemFacade);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Could not load view: " + fxmlFile);
            e.printStackTrace();
        }
    }
}
