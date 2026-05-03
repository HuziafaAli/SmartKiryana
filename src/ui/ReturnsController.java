package ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import facade.SystemFacade;
import model.*;
import command.ReturnItemCommand;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReturnsController implements FacadeAware {

    @FXML private TextField billSearchField;
    @FXML private VBox billInfoBox;
    @FXML private Label billIdLabel;
    @FXML private Label billDateLabel;
    @FXML private Label billTotalLabel;
    @FXML private Label billCashierLabel;
    @FXML private Label itemCountLabel;
    @FXML private FlowPane billItemsFlow;
    @FXML private VBox selectedReturnsBox;
    @FXML private TextField reasonField;
    @FXML private Label refundLabel;

    private SystemFacade systemFacade;
    private Bill foundBill;
    private final Map<Integer, Spinner<Integer>> returnSpinners = new HashMap<>();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");

    public void initialize() {
        renderEmptyState("Search for a bill to begin a return.");
        renderSelectedReturns();
    }

    @Override
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
    }

    // Searches for a previous bill by ID to begin a return process
    @FXML
    private void handleSearchBill() {
        String idText = billSearchField.getText().trim();
        try {
            int billId = Integer.parseInt(idText);
            foundBill = systemFacade.findBillById(billId);

            if (foundBill == null) {
                showAlert("Not Found", "No bill found with ID: " + idText);
                handleReset();
                billSearchField.setText(idText);
                return;
            }

            billInfoBox.setVisible(true);
            billInfoBox.setManaged(true);
            billIdLabel.setText("BLL-" + foundBill.getBillId());
            billDateLabel.setText(foundBill.getBillDate().format(dtf));
            billTotalLabel.setText(String.format("Rs. %.2f", foundBill.getTotalAmount()));
            billCashierLabel.setText(foundBill.getUser() != null ? foundBill.getUser().getFullName() : "N/A");

            returnSpinners.clear();
            renderBillItems();
            calculateRefund();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid Bill ID number.");
        }
    }

    // Clears the active return session and resets the UI
    @FXML
    private void handleReset() {
        foundBill = null;
        returnSpinners.clear();
        billSearchField.clear();
        reasonField.clear();
        billInfoBox.setVisible(false);
        billInfoBox.setManaged(false);
        refundLabel.setText("Rs. 0.00");
        itemCountLabel.setText("Search for a bill");
        renderEmptyState("Search for a bill to begin a return.");
        renderSelectedReturns();
    }

    // Populates the grid with products from the loaded bill
    private void renderBillItems() {
        billItemsFlow.getChildren().clear();

        if (foundBill == null || foundBill.getItems().isEmpty()) {
            itemCountLabel.setText("No products");
            renderEmptyState("This bill has no products.");
            return;
        }

        itemCountLabel.setText(foundBill.getItems().size() + " products available");
        for (BillItem item : foundBill.getItems()) {
            billItemsFlow.getChildren().add(createReturnItemCard(item));
        }
    }

    private VBox createReturnItemCard(BillItem item) {
        Product product = item.getProduct();
        VBox card = new VBox(0);
        card.getStyleClass().add("return-product-card");

        Label category = new Label(product.getCategory() != null ? product.getCategory().getCategoryName() : "Uncategorized");
        category.getStyleClass().add("inv-card-cat-pill");

        Label name = new Label(product.getName());
        name.getStyleClass().add("return-product-name");
        name.setMaxWidth(238);
        name.setWrapText(true);
        VBox.setMargin(name, new javafx.geometry.Insets(10, 0, 2, 0));

        Label barcode = new Label("# " + product.getBarcode());
        barcode.getStyleClass().add("inv-card-barcode");
        VBox.setMargin(barcode, new javafx.geometry.Insets(0, 0, 12, 0));

        HBox metrics = new HBox(8,
                metric("Sold", String.valueOf(item.getQuantity())),
                metric("Unit", String.format("Rs. %.0f", item.getUnitPrice())),
                metric("Total", String.format("Rs. %.0f", item.getSubtotal())));
        VBox.setMargin(metrics, new javafx.geometry.Insets(12, 0, 12, 0));

        Spinner<Integer> spinner = new Spinner<>(0, item.getQuantity(), 0);
        spinner.setEditable(true);
        spinner.setPrefWidth(86);
        spinner.getStyleClass().add("return-spinner");
        returnSpinners.put(item.getBillItemId(), spinner);

        Label refund = new Label("Rs. 0");
        refund.getStyleClass().add("return-card-refund");

        spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            refund.setText(String.format("Rs. %.0f", newValue * item.getUnitPrice()));
            updateCardSelection(card, newValue > 0);
            calculateRefund();
        });

        HBox action = new HBox(10);
        action.setAlignment(Pos.CENTER_LEFT);
        Label returnQty = new Label("Return Qty");
        returnQty.getStyleClass().add("bill-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        action.getChildren().addAll(returnQty, spinner, refund);

        card.getChildren().addAll(category, name, barcode, divider(), metrics, divider(), action);
        return card;
    }

    private VBox metric(String labelText, String valueText) {
        VBox box = new VBox(3);
        box.getStyleClass().add("return-mini-metric");
        Label label = new Label(labelText);
        label.getStyleClass().add("inv-card-field-label");
        Label value = new Label(valueText);
        value.getStyleClass().add("inv-card-field-value");
        box.getChildren().addAll(label, value);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private Region divider() {
        Region region = new Region();
        region.getStyleClass().add("inv-card-divider");
        return region;
    }

    private void updateCardSelection(VBox card, boolean selected) {
        if (selected) {
            if (!card.getStyleClass().contains("return-product-card-selected")) {
                card.getStyleClass().add("return-product-card-selected");
            }
        } else {
            card.getStyleClass().remove("return-product-card-selected");
        }
    }

    private void calculateRefund() {
        double refund = 0;
        if (foundBill != null) {
            for (BillItem item : foundBill.getItems()) {
                Spinner<Integer> spinner = returnSpinners.get(item.getBillItemId());
                if (spinner != null && spinner.getValue() > 0) {
                    refund += spinner.getValue() * item.getUnitPrice();
                }
            }
        }
        refundLabel.setText(String.format("Rs. %.2f", refund));
        renderSelectedReturns();
    }

    // Updates the summary sidebar with the selected return items
    private void renderSelectedReturns() {
        selectedReturnsBox.getChildren().clear();

        if (foundBill == null) {
            selectedReturnsBox.getChildren().add(emptySummary("No bill selected."));
            return;
        }

        boolean hasItems = false;
        for (BillItem item : foundBill.getItems()) {
            Spinner<Integer> spinner = returnSpinners.get(item.getBillItemId());
            if (spinner == null || spinner.getValue() <= 0) {
                continue;
            }

            hasItems = true;
            selectedReturnsBox.getChildren().add(createSelectedReturnRow(item, spinner.getValue()));
        }

        if (!hasItems) {
            selectedReturnsBox.getChildren().add(emptySummary("Set a return quantity on any product card."));
        }
    }

    private HBox createSelectedReturnRow(BillItem item, int quantity) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("selected-return-row");

        VBox text = new VBox(3);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label name = new Label(item.getProduct().getName());
        name.getStyleClass().add("selected-return-name");
        name.setWrapText(true);
        name.setMaxWidth(190);

        Label qty = new Label(quantity + " x Rs. " + String.format("%.0f", item.getUnitPrice()));
        qty.getStyleClass().add("text-muted");

        Label amount = new Label(String.format("Rs. %.0f", quantity * item.getUnitPrice()));
        amount.getStyleClass().add("selected-return-amount");

        text.getChildren().addAll(name, qty);
        row.getChildren().addAll(text, amount);
        return row;
    }

    private Label emptySummary(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("return-empty-state");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private void renderEmptyState(String message) {
        billItemsFlow.getChildren().clear();
        Label empty = new Label(message);
        empty.getStyleClass().add("return-empty-state");
        empty.setPrefWidth(460);
        empty.setAlignment(Pos.CENTER);
        billItemsFlow.getChildren().add(empty);
    }

    // Finalizes the return transaction through the command pattern
    @FXML
    private void handleProcessReturn() {
        if (foundBill == null) {
            showAlert("No Bill", "Please search for a bill first.");
            return;
        }

        String reason = reasonField.getText().trim();
        if (reason.isEmpty()) {
            showAlert("Missing Reason", "Please enter a return reason.");
            return;
        }

        List<ReturnItem> returnItems = new ArrayList<>();
        for (BillItem item : foundBill.getItems()) {
            Spinner<Integer> spinner = returnSpinners.get(item.getBillItemId());
            if (spinner != null && spinner.getValue() > 0) {
                returnItems.add(new ReturnItem(0, item, spinner.getValue()));
            }
        }

        if (returnItems.isEmpty()) {
            showAlert("No Items", "Please set a return quantity for at least one product.");
            return;
        }

        ReturnItemCommand cmd = new ReturnItemCommand(systemFacade.getBillController(), foundBill, returnItems, reason);
        systemFacade.executeCommand(cmd);

        ReturnTransaction result = cmd.getResult();
        if (result != null) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Return Processed");
            info.setContentText("Refund Amount: Rs. " + String.format("%.2f", result.getRefundAmount()));
            DialogStyler.style(info);
            info.showAndWait();
            handleReset();
        } else {
            showAlert("Failed", "Return processing failed. Items may be ineligible.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(msg);
        DialogStyler.style(alert);
        alert.showAndWait();
    }
}
