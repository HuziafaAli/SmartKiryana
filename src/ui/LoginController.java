package ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import facade.SystemFacade;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private SystemFacade systemFacade;

    public void setSystemFacade(SystemFacade systemFacade) {
        this.systemFacade = systemFacade;
    }

    // Validates credentials and navigates to the dashboard on success
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Username and Password are required.", true);
            return;
        }

        boolean success = systemFacade.login(username, password);

        if (success) {
            showStatus("Login Successful! Transitioning...", false);
            System.out.println("Login Success for: " + systemFacade.getCurrentUser().getFullName());
            navigateToDashboard();
        } else {
            showStatus("Invalid username or password.", true);
        }
    }

    // Loads the main dashboard and injects the facade
    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Dashboard.fxml"));
            Parent root = loader.load();

            DashboardController dashboardController = loader.getController();
            dashboardController.setSystemFacade(systemFacade);

            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Error loading Dashboard.", true);
        }
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-error", "status-success");
        if (isError) {
            statusLabel.getStyleClass().add("status-error");
        } else {
            statusLabel.getStyleClass().add("status-success");
        }
    }
}
