import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import dao.*;
import service.*;
import controller.*;
import facade.SystemFacade;
import observer.StockAlert;
import util.DatabaseConnection;
import ui.LoginController;

public class Main extends Application {

    private SystemFacade systemFacade;

    // Wires up the entire backend stack before the UI loads
    @Override
    public void init() {
        if (!DatabaseConnection.testConnection()) {
            System.err.println("CRITICAL: Database Connection Failed!");
            return;
        }

        UserDAO userDAO = new UserDAO();
        ProductCategoryDAO categoryDAO = new ProductCategoryDAO();
        InventoryItemDAO inventoryDAO = new InventoryItemDAO();
        BillDAO billDAO = new BillDAO(userDAO);
        ReturnTransactionDAO returnDAO = new ReturnTransactionDAO(billDAO);
        SalesTargetDAO salesTargetDAO = new SalesTargetDAO(userDAO);

        AuthService authService = new AuthService(userDAO);
        InventoryService inventoryService = new InventoryService(categoryDAO, inventoryDAO);

        StockAlert stockAlert = new StockAlert();
        inventoryService.addObserver(stockAlert);
        inventoryService.checkAllStockLevels();

        BillingService billingService = new BillingService(inventoryService, billDAO, returnDAO);
        ReportService reportService = new ReportService(authService, salesTargetDAO);

        UserController userController = new UserController(authService);
        InventoryController inventoryController = new InventoryController(inventoryService);
        BillController billController = new BillController(billingService);
        ReportController reportController = new ReportController(reportService);

        this.systemFacade = new SystemFacade(billController, inventoryController, reportController, userController, stockAlert);
    }

    // Loads the login screen and shows the main window
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Login.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setSystemFacade(systemFacade);

        Scene scene = new Scene(root);
        stage.setTitle("SmartKiryana POS - Login");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
