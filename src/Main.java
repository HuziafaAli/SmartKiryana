import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import dao.*;
import service.*;
import controller.*;
import facade.SystemFacade;
import util.DatabaseConnection;
import ui.LoginController;

public class Main extends Application {

    private SystemFacade systemFacade;

    @Override
    public void init() {
        // 1. Test Database Connection
        if (!DatabaseConnection.testConnection()) {
            System.err.println("CRITICAL: Database Connection Failed!");
            return;
        }

        // 2. Initialize Backend stack
        UserDAO userDAO = new UserDAO();
        ProductCategoryDAO categoryDAO = new ProductCategoryDAO();
        InventoryItemDAO inventoryDAO = new InventoryItemDAO();
        BillDAO billDAO = new BillDAO(userDAO);
        ReturnTransactionDAO returnDAO = new ReturnTransactionDAO(billDAO);
        SalesTargetDAO salesTargetDAO = new SalesTargetDAO(userDAO);

        AuthService authService = new AuthService(userDAO);
        InventoryService inventoryService = new InventoryService(categoryDAO, inventoryDAO);
        BillingService billingService = new BillingService(inventoryService, billDAO, returnDAO);
        ReportService reportService = new ReportService(authService, salesTargetDAO);

        UserController userController = new UserController(authService);
        InventoryController inventoryController = new InventoryController(inventoryService);
        BillController billController = new BillController(billingService);
        ReportController reportController = new ReportController(reportService);

        this.systemFacade = new SystemFacade(billController, inventoryController, reportController, userController);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // 3. Load the FXML Login View
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Login.fxml"));
        Parent root = loader.load();

        // 4. Inject the Facade into the Controller
        LoginController controller = loader.getController();
        controller.setSystemFacade(systemFacade);

        // 5. Setup Scene and Window
        Scene scene = new Scene(root);
        stage.setTitle("SmartKiryana POS - Login");
        stage.setScene(scene);
        
        // Open in Maximized mode
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
