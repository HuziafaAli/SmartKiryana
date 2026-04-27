package ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import facade.SystemFacade;
import model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardHomeController implements FacadeAware {

    @FXML private Label welcomeLabel;
    @FXML private Label todaySalesLabel;
    @FXML private Label todaySalesChange;
    @FXML private Label monthlyRevenueLabel;
    @FXML private Label monthlyRevenueChange;
    @FXML private Label lowStockLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label alertCountLabel;

    @FXML private TableView<Bill> recentBillsTable;
    @FXML private TableColumn<Bill, String> colBillId;
    @FXML private TableColumn<Bill, String> colBillDate;
    @FXML private TableColumn<Bill, String> colBillCustomer;
    @FXML private TableColumn<Bill, String> colBillAmount;
    @FXML private TableColumn<Bill, String> colBillCashier;

    @FXML private VBox topProductsList;
    @FXML private VBox alertsList;
    @FXML private VBox salesChartArea;

    private SystemFacade systemFacade;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");

    public void initialize() {
        colBillId.setCellValueFactory(c -> new SimpleStringProperty("BLL-" + c.getValue().getBillId()));
        colBillDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBillDate().format(dtf)));
        colBillCustomer.setCellValueFactory(c -> new SimpleStringProperty("Walk-in Customer"));
        colBillAmount.setCellValueFactory(c -> new SimpleStringProperty(String.format("Rs. %.2f", c.getValue().getTotalAmount())));
        colBillCashier.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getUser() != null ? c.getValue().getUser().getFullName() : "N/A"));
    }

    @Override
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
        loadDashboardData();
    }

    private void loadDashboardData() {
        User user = systemFacade.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName());
        }

        List<Bill> allBills = systemFacade.getAllBills();
        List<InventoryItem> allItems = systemFacade.getAllInventoryItems();
        List<InventoryItem> lowStockItems = systemFacade.getLowStockItems();

        // Today's Sales
        LocalDate today = LocalDate.now();
        double todaySales = allBills.stream()
                .filter(b -> b.getBillDate().toLocalDate().equals(today))
                .mapToDouble(Bill::getTotalAmount).sum();
        todaySalesLabel.setText(String.format("Rs. %,.0f", todaySales));

        // Monthly Revenue
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        double monthlyRevenue = allBills.stream()
                .filter(b -> b.getBillDate().getMonthValue() == currentMonth && b.getBillDate().getYear() == currentYear)
                .mapToDouble(Bill::getTotalAmount).sum();
        monthlyRevenueLabel.setText(String.format("Rs. %,.0f", monthlyRevenue));

        // Low Stock & Total Products
        lowStockLabel.setText(String.valueOf(lowStockItems.size()));
        totalProductsLabel.setText(String.valueOf(allItems.size()));

        // Recent Transactions (last 5)
        List<Bill> recent = allBills.stream()
                .sorted(Comparator.comparing(Bill::getBillDate).reversed())
                .limit(5)
                .collect(Collectors.toList());
        recentBillsTable.setItems(FXCollections.observableArrayList(recent));

        // Top Selling Products
        loadTopProducts(allBills);

        // Stock Alerts
        loadStockAlerts(lowStockItems);
    }

    private void loadTopProducts(List<Bill> allBills) {
        topProductsList.getChildren().clear();
        Map<String, Integer> productSales = new HashMap<>();

        for (Bill b : allBills) {
            for (BillItem item : b.getItems()) {
                String name = item.getProduct().getName();
                productSales.put(name, productSales.getOrDefault(name, 0) + item.getQuantity());
            }
        }

        productSales.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .forEach(entry -> {
                    HBox row = new HBox(10);
                    row.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 8 0;");

                    Label rank = new Label(String.valueOf(topProductsList.getChildren().size() + 1) + ".");
                    rank.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 13px; -fx-min-width: 25;");

                    Label name = new Label(entry.getKey());
                    name.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

                    Label qty = new Label(entry.getValue() + " sold");
                    qty.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 12px;");

                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    row.getChildren().addAll(rank, name, spacer, qty);
                    topProductsList.getChildren().add(row);
                });

        if (topProductsList.getChildren().isEmpty()) {
            Label empty = new Label("No sales data yet");
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 13px;");
            topProductsList.getChildren().add(empty);
        }
    }

    private void loadStockAlerts(List<InventoryItem> lowStockItems) {
        alertsList.getChildren().clear();
        alertCountLabel.setText(lowStockItems.size() + " alerts");

        for (InventoryItem item : lowStockItems) {
            VBox alertItem = new VBox(3);
            alertItem.getStyleClass().add("alert-item");

            Label name = new Label(item.getProduct().getName());
            name.getStyleClass().add("alert-item-name");

            Label stock = new Label("Stock: " + item.getStockQuantity() + " (Min: " + item.getMinStockThreshold() + ")");
            stock.getStyleClass().add("alert-item-stock");

            alertItem.getChildren().addAll(name, stock);
            alertsList.getChildren().add(alertItem);
        }

        if (lowStockItems.isEmpty()) {
            Label empty = new Label("No stock alerts");
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 13px;");
            alertsList.getChildren().add(empty);
        }
    }
}
