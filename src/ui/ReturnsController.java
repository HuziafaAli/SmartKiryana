package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;
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

    @FXML private TableView<BillItem> billItemsTable;
    @FXML private TableColumn<BillItem, String> colOrigItem;
    @FXML private TableColumn<BillItem, String> colOrigQty;
    @FXML private TableColumn<BillItem, String> colOrigPrice;
    @FXML private TableColumn<BillItem, String> colOrigTotal;

    @FXML private TableView<BillItem> returnItemsTable;
    @FXML private TableColumn<BillItem, String> colRetItem;
    @FXML private TableColumn<BillItem, String> colRetQty;
    @FXML private TableColumn<BillItem, String> colRetReturn;
    @FXML private TableColumn<BillItem, Void> colRetSelect;

    @FXML private TextField reasonField;
    @FXML private Label refundLabel;

    private SystemFacade systemFacade;
    private Bill foundBill;
    private Map<Integer, Spinner<Integer>> returnSpinners = new HashMap<>();
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");

    public void initialize() {
        colOrigItem.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProduct().getName()));
        colOrigQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        colOrigPrice.setCellValueFactory(c -> new SimpleStringProperty(String.format("Rs. %.0f", c.getValue().getUnitPrice())));
        colOrigTotal.setCellValueFactory(c -> new SimpleStringProperty(String.format("Rs. %.0f", c.getValue().getSubtotal())));

        colRetItem.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProduct().getName()));
        colRetQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));

        colRetReturn.setCellFactory(col -> new TableCell<BillItem, String>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    BillItem item = getTableRow().getItem();
                    Spinner<Integer> spinner = new Spinner<>(0, item.getQuantity(), 0);
                    spinner.setPrefWidth(70);
                    spinner.setStyle("-fx-background-color: #1a1f35; -fx-font-size: 12px;");
                    returnSpinners.put(item.getBillItemId(), spinner);
                    spinner.valueProperty().addListener((obs, o, n) -> calculateRefund());
                    setGraphic(spinner);
                }
            }
        });

        colRetSelect.setCellFactory(col -> new TableCell<BillItem, Void>() {
            private final CheckBox cb = new CheckBox();
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : cb);
            }
        });
    }

    @Override
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
    }

    @FXML
    private void handleSearchBill() {
        String idText = billSearchField.getText().trim();
        try {
            int billId = Integer.parseInt(idText);
            foundBill = systemFacade.findBillById(billId);

            if (foundBill != null) {
                billInfoBox.setVisible(true);
                billInfoBox.setManaged(true);
                billIdLabel.setText("BLL-" + foundBill.getBillId());
                billDateLabel.setText(foundBill.getBillDate().format(dtf));
                billTotalLabel.setText(String.format("Rs. %.2f", foundBill.getTotalAmount()));
                billCashierLabel.setText(foundBill.getUser() != null ? foundBill.getUser().getFullName() : "N/A");

                ObservableList<BillItem> items = FXCollections.observableArrayList(foundBill.getItems());
                billItemsTable.setItems(items);
                returnItemsTable.setItems(items);
                returnSpinners.clear();
                refundLabel.setText("Rs. 0.00");
            } else {
                showAlert("Not Found", "No bill found with ID: " + idText);
                billInfoBox.setVisible(false);
                billInfoBox.setManaged(false);
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid Bill ID number.");
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
    }

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
                ReturnItem ri = new ReturnItem(0, item, spinner.getValue());
                returnItems.add(ri);
            }
        }

        if (returnItems.isEmpty()) {
            showAlert("No Items", "Please select items to return.");
            return;
        }

        // Use Command Pattern
        ReturnItemCommand cmd = new ReturnItemCommand(systemFacade.getBillController(), foundBill, returnItems, reason);
        systemFacade.executeCommand(cmd);

        ReturnTransaction result = cmd.getResult();
        if (result != null) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Return Processed");
            info.setContentText("Refund Amount: Rs. " + String.format("%.2f", result.getRefundAmount()));
            info.showAndWait();

            // Reset
            billSearchField.clear();
            billInfoBox.setVisible(false);
            billInfoBox.setManaged(false);
            billItemsTable.getItems().clear();
            returnItemsTable.getItems().clear();
            reasonField.clear();
            refundLabel.setText("Rs. 0.00");
        } else {
            showAlert("Failed", "Return processing failed. Items may be ineligible.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setContentText(msg); a.showAndWait();
    }
}
