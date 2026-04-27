package facade;

import controller.BillController;
import controller.InventoryController;
import controller.ReportController;
import controller.UserController;
import model.*;
import command.Command;
import java.util.List;
import java.time.LocalDateTime;

public class SystemFacade {
    private BillController billController;
    private InventoryController inventoryController;
    private ReportController reportController;
    private UserController userController;

    public SystemFacade(BillController billController, InventoryController inventoryController,
            ReportController reportController, UserController userController) {

        this.billController = billController;
        this.inventoryController = inventoryController;
        this.reportController = reportController;
        this.userController = userController;
    }

    // Command Execution
    public void executeCommand(Command command) {
        command.execute();
    }

    // User & Authentication
    public boolean login(String username, String password) {
        return userController.login(username, password);
    }

    public boolean logout() {
        return userController.logout();
    }

    public User getCurrentUser() {
        return userController.getCurrentUser();
    }

    public boolean addEmployee(String username, String password, String fullName, String phone, String cnic) {
        return userController.addEmployee(username, password, fullName, phone, cnic);
    }

    public boolean updateEmployee(int userId, String newFullName, String newPhone, String newUsername,
            String newPassword) {
        return userController.updateEmployee(userId, newFullName, newPhone, newUsername, newPassword);
    }

    public boolean deactivateUser(int userId) {
        return userController.deactivateUser(userId);
    }

    public List<Employee> getAllEmployees() {
        return userController.getAllEmployees();
    }

    // Inventory: Categories
    public boolean addCategory(String name) {
        return inventoryController.addCategory(name);
    }

    public boolean updateCategory(int id, String newName) {
        return inventoryController.updateCategory(id, newName);
    }

    public boolean deleteCategory(int id) {
        return inventoryController.deleteCategory(id);
    }

    public List<ProductCategory> getAllCategories() {
        return inventoryController.getAllCategories();
    }

    // Inventory: Products
    public boolean addProduct(String barcode, String name, int categoryId, double price,
            double costPrice, int minStock, int maxStock) {
        return inventoryController.addProduct(barcode, name, categoryId, price, costPrice, minStock,
                maxStock);
    }

    public boolean updateProduct(String barcode, String newName, double newPrice, double newCostPrice) {
        return inventoryController.updateProduct(barcode, newName, newPrice, newCostPrice);
    }

    public boolean deleteProduct(String barcode) {
        return inventoryController.deleteProduct(barcode);
    }

    // Inventory: Stock
    public boolean addStock(String barcode, int quantityToAdd) {
        return inventoryController.addStock(barcode, quantityToAdd);
    }

    public boolean reduceStock(String barcode, int quantityToReduce) {
        return inventoryController.reduceStock(barcode, quantityToReduce);
    }

    public InventoryItem isProductExists(String barcode) {
        return inventoryController.isProductExists(barcode);
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryController.getLowStockItems();
    }

    public List<InventoryItem> getAllInventoryItems() {
        return inventoryController.getAllInventoryItems();
    }

    public void checkAllStockLevels() {
        inventoryController.checkAllStockLevels();
    }

    // POS & Billing
    public Bill startNewBill(User cashier) {
        return billController.startNewBill(cashier);
    }

    public String scanItem(String barcode, int quantity) {
        return billController.scanItem(barcode, quantity);
    }

    public boolean applyDiscount(double amount) {
        return billController.applyDiscount(amount);
    }

    public boolean removeItem(String barcode) {
        return billController.removeItem(barcode);
    }

    public Bill getCurrentBill() {
        return billController.getCurrentBill();
    }

    public Bill checkOut(double cashProvided) {
        return billController.checkOut(cashProvided);
    }

    public List<Bill> getAllBills() {
        return billController.getAllBills();
    }

    public List<ReturnTransaction> getAllReturns() {
        return billController.getAllReturns();
    }

    public List<Bill> filterByDateRange(LocalDateTime from, LocalDateTime to) {
        return billController.filterByDateRange(from, to);
    }

    public List<Bill> filterByCategory(int categoryId) {
        return billController.filterByCategory(categoryId);
    }

    public List<Bill> filterByEmployee(int employeeId) {
        return billController.filterByEmployee(employeeId);
    }

    public List<Bill> filterByAmountRange(double minAmount, double maxAmount) {
        return billController.filterByAmountRange(minAmount, maxAmount);
    }

    // Reports & Performance
    public boolean assignTarget(Employee employee, int month, int year, double targetAmount) {
        return reportController.assignTarget(employee, month, year, targetAmount);
    }

    public List<PerformanceReport> getPerformanceComparison(List<Employee> employees, int month, int year,
            List<Bill> allBills) {
        return reportController.getPerformanceComparison(employees, month, year, allBills);
    }

    public List<SalesRecord> getSalesHistory(List<Bill> allBills) {
        return reportController.getSalesHistory(allBills);
    }

    // Controller access for Command pattern usage
    public BillController getBillController() {
        return billController;
    }

    public ReportController getReportController() {
        return reportController;
    }

    // Utility: Find bill by ID
    public Bill findBillById(int billId) {
        List<Bill> allBills = getAllBills();
        for (Bill b : allBills) {
            if (b.getBillId() == billId) {
                return b;
            }
        }
        return null;
    }
}
