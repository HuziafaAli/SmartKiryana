import dao.*;
import service.*;
import controller.*;
import facade.SystemFacade;
import observer.StockAlert;
import util.DatabaseConnection;

public class Main {

    public static void main(String[] args) {
        
        System.out.println("Initializing SmartKiryana Backend...");

        // 1. Test Database Connection
        if (!DatabaseConnection.testConnection()) {
            System.err.println("CRITICAL: Could not connect to PostgreSQL Database. Exiting...");
            return;
        }
        System.out.println("Database Connected Successfully!");

        // 2. Initialize DAOs (Data Access Layer)
        UserDAO userDAO = new UserDAO();
        ProductCategoryDAO categoryDAO = new ProductCategoryDAO();
        InventoryItemDAO inventoryDAO = new InventoryItemDAO();
        BillDAO billDAO = new BillDAO(userDAO);
        ReturnTransactionDAO returnDAO = new ReturnTransactionDAO(billDAO);
        SalesTargetDAO salesTargetDAO = new SalesTargetDAO(userDAO);

        // 3. Initialize Services (Business Logic Layer)
        AuthService authService = new AuthService(userDAO);
        InventoryService inventoryService = new InventoryService(categoryDAO, inventoryDAO);
        BillingService billingService = new BillingService(inventoryService, billDAO, returnDAO);
        ReportService reportService = new ReportService(authService, salesTargetDAO);

        // 4. Setup Observers (e.g., Stock Alerts)
        StockAlert stockAlert = new StockAlert();
        inventoryService.addObserver(stockAlert);

        // 5. Initialize Controllers (API / Routing Layer)
        UserController userController = new UserController(authService);
        InventoryController inventoryController = new InventoryController(inventoryService);
        BillController billController = new BillController(billingService);
        ReportController reportController = new ReportController(reportService);

        // 6. Initialize the Facade (Single Entry Point for UI)
        SystemFacade systemFacade = new SystemFacade(billController, inventoryController, reportController, userController);

        System.out.println("Backend Initialization Complete! Ready for UI connection.");
        
        // --- Quick Test to prove it works ---
        System.out.println("\n--- Testing Login with Database ---");
        boolean loginSuccess = systemFacade.login("admin", "admin123");
        if(loginSuccess) {
            System.out.println("SUCCESS: Logged in as: " + systemFacade.getCurrentUser().getFullName());
        } else {
            System.out.println("FAILED: Could not log in.");
        }
    }
}
