package ui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.beans.property.SimpleStringProperty;
import facade.SystemFacade;
import model.*;

import java.util.List;
import java.util.Optional;

public class POSController implements FacadeAware {

    @FXML
    private TextField searchField;
    @FXML
    private FlowPane productGrid;
    @FXML
    private VBox billItemsBox;
    @FXML
    private Label subTotalLabel;
    @FXML
    private Label taxLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private TextField discountField;

    private SystemFacade systemFacade;

    public void initialize() {
        discountField.textProperty().addListener((obs, o, n) -> {
            try {
                double discount = n.isEmpty() ? 0 : Double.parseDouble(n);
                if (systemFacade != null) {
                    systemFacade.applyDiscount(discount);
                    refreshBillSummary();
                }
            } catch (NumberFormatException ignored) {
            }
        });
    }

    @Override
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
        systemFacade.startNewBill(systemFacade.getCurrentUser());
        loadProductGrid();
    }

    private void loadProductGrid() {
        productGrid.getChildren().clear();
        List<InventoryItem> items = systemFacade.getAllInventoryItems();

        for (InventoryItem inv : items) {
            Product p = inv.getProduct();
            VBox card = new VBox(8);
            card.getStyleClass().add("product-card");

            Label name = new Label(p.getName());
            name.getStyleClass().add("product-card-name");
            name.setWrapText(true);
            name.setMaxWidth(120);

            Label category = new Label(p.getCategory() != null ? p.getCategory().getCategoryName() : "");
            category.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 10px;");

            Label price = new Label(String.format("Rs. %.0f", p.getPrice()));
            price.getStyleClass().add("product-card-price");

            Label stock = new Label("Stock: " + inv.getStockQuantity());
            stock.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 10px;");

            card.getChildren().addAll(name, category, price, stock);

            card.setOnMouseClicked(e -> {
                String result = systemFacade.scanItem(p.getBarcode(), 1);
                if (result.contains("successfully") || result.contains("Updated")) {
                    refreshBill();
                } else {
                    showAlert("Cannot Add Item", result);
                }
            });

            productGrid.getChildren().add(card);
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadProductGrid();
            return;
        }

        productGrid.getChildren().clear();
        List<InventoryItem> items = systemFacade.getAllInventoryItems();
        for (InventoryItem inv : items) {
            Product p = inv.getProduct();
            if (p.getName().toLowerCase().contains(query.toLowerCase()) || p.getBarcode().contains(query)) {
                VBox card = createProductCard(inv);
                productGrid.getChildren().add(card);
            }
        }
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        loadProductGrid();
    }

    private VBox createProductCard(InventoryItem inv) {
        Product p = inv.getProduct();
        VBox card = new VBox(8);
        card.getStyleClass().add("product-card");

        Label name = new Label(p.getName());
        name.getStyleClass().add("product-card-name");
        name.setWrapText(true);
        name.setMaxWidth(120);

        Label price = new Label(String.format("Rs. %.0f", p.getPrice()));
        price.getStyleClass().add("product-card-price");

        card.getChildren().addAll(name, price);
        card.setOnMouseClicked(e -> {
            String result = systemFacade.scanItem(p.getBarcode(), 1);
            if (result.contains("successfully") || result.contains("Updated")) {
                refreshBill();
            } else {
                showAlert("Cannot Add Item", result);
            }
        });

        return card;
    }

    private void refreshBill() {
        billItemsBox.getChildren().clear();
        Bill bill = systemFacade.getCurrentBill();

        if (bill != null && !bill.getItems().isEmpty()) {
            for (BillItem item : bill.getItems()) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.04);" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 10 12;");

                VBox nameBox = new VBox(3);
                HBox.setHgrow(nameBox, Priority.ALWAYS);

                Label nameLabel = new Label(item.getProduct().getName());
                nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
                nameLabel.setMaxWidth(160);
                nameLabel.setWrapText(true);

                Label qtyLabel = new Label("x" + item.getQuantity());
                qtyLabel.setStyle(
                        "-fx-text-fill: #60a5fa; -fx-font-size: 11px; -fx-font-weight: bold;" +
                                "-fx-background-color: rgba(59,130,246,0.15);" +
                                "-fx-background-radius: 20; -fx-padding: 2 8;");

                nameBox.getChildren().addAll(nameLabel, qtyLabel);

                Label priceLabel = new Label(String.format("Rs. %.0f", item.getUnitPrice()));
                priceLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.45); -fx-font-size: 12px;");
                priceLabel.setMinWidth(65);

                Label subtotalLabel = new Label(String.format("Rs. %.0f", item.getSubtotal()));
                subtotalLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 13px; -fx-font-weight: bold;");
                subtotalLabel.setMinWidth(70);

                Button removeBtn = new Button("X");
                removeBtn.getStyleClass().add("btn-danger");
                removeBtn.setStyle("-fx-padding: 4 8; -fx-font-size: 11px;");
                removeBtn.setOnAction(e -> {
                    systemFacade.removeItem(item.getProduct().getBarcode());
                    refreshBill();
                });

                row.getChildren().addAll(nameBox, priceLabel, subtotalLabel, removeBtn);
                billItemsBox.getChildren().add(row);
            }
        } else {
            Label empty = new Label("No items added yet");
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.25); -fx-font-size: 13px;");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40, 0, 40, 0));
            billItemsBox.getChildren().add(empty);
        }

        refreshBillSummary();
    }

    private void refreshBillSummary() {
        Bill bill = systemFacade.getCurrentBill();
        if (bill != null) {
            double subtotal = bill.getItems().stream().mapToDouble(BillItem::getSubtotal).sum();
            subTotalLabel.setText(String.format("Rs. %.2f", subtotal));
            taxLabel.setText(String.format("Rs. %.2f", bill.getTaxAmount()));
            totalLabel.setText(String.format("Rs. %.2f", bill.getTotalAmount()));
        } else {
            subTotalLabel.setText("Rs. 0.00");
            taxLabel.setText("Rs. 0.00");
            totalLabel.setText("Rs. 0.00");
        }
    }

    @FXML
    private void handlePay() {
        Bill bill = systemFacade.getCurrentBill();
        if (bill == null || bill.getItems().isEmpty()) {
            showAlert("No Items", "Please add items to the bill first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.format("%.0f", bill.getTotalAmount()));
        dialog.setTitle("Payment");
        dialog.setHeaderText("Total: " + String.format("Rs. %.2f", bill.getTotalAmount()));
        dialog.setContentText("Cash Provided:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(cash -> {
            try {
                double cashAmount = Double.parseDouble(cash);
                Bill finalized = systemFacade.checkOut(cashAmount);
                if (finalized != null) {
                    double change = finalized.getreturnCash();
                    showInfo("Payment Successful", String.format("Change: Rs. %.2f\nThank you!", change));
                    systemFacade.startNewBill(systemFacade.getCurrentUser());
                    refreshBill();
                    loadProductGrid();
                } else {
                    showAlert("Payment Failed", "Insufficient cash or no active bill.");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Amount", "Please enter a valid number.");
            }
        });
    }

    @FXML
    private void handleClear() {
        systemFacade.startNewBill(systemFacade.getCurrentUser());
        discountField.clear();
        refreshBill();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}