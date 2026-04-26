package ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import facade.SystemFacade;

import java.io.IOException;

public class DashboardController {

    @FXML private StackPane contentArea;
    @FXML private Label userNameLabel;

    private SystemFacade systemFacade;

    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
        if (facade.getCurrentUser() != null) {
            userNameLabel.setText(facade.getCurrentUser().getFullName());
        }
    }

    @FXML
    private void showPOS() {
        loadView("POS.fxml");
    }

    @FXML
    private void showInventory() {
        loadView("Inventory.fxml");
    }

    @FXML
    private void showEmployees() {
        // To be implemented
        System.out.println("Switching to Employees...");
    }

    @FXML
    private void showReports() {
        // To be implemented
        System.out.println("Switching to Reports...");
    }

    @FXML
    private void handleLogout() {
        try {
            systemFacade.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Login.fxml"));
            Parent root = loader.load();
            
            // Re-inject facade into new LoginController
            LoginController controller = loader.getController();
            controller.setSystemFacade(systemFacade);

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/" + fxmlFile));
            Node view = loader.load();
            
            // Inject the facade into the loaded controller
            Object controller = loader.getController();
            if (controller instanceof InventoryController) {
                ((InventoryController) controller).setSystemFacade(systemFacade);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Could not load view: " + fxmlFile);
            e.printStackTrace();
        }
    }
}
