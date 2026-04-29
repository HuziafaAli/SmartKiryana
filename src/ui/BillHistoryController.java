package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import facade.SystemFacade;
import model.Bill;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import model.ReturnTransaction;
import model.ReturnItem;

public class BillHistoryController implements FacadeAware {

    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private TextField searchField;
    @FXML private Label totalSalesLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label avgOrderLabel;
    @FXML private Label salesCountLabel;
    @FXML private Label returnsCountLabel;
    @FXML private FlowPane billGrid;
    @FXML private FlowPane returnsGrid;
    @FXML private TabPane historyTabPane;

    private SystemFacade systemFacade;
    private final ObservableList<Bill> allBills = FXCollections.observableArrayList();
    private final ObservableList<ReturnTransaction> allReturns = FXCollections.observableArrayList();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");

    @Override
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
        
        historyTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> handleFilter());

        loadAllBills();
        loadAllReturns();
    }

    private void loadAllBills() {
        allBills.setAll(systemFacade.getAllBills());
        if (historyTabPane.getSelectionModel().getSelectedIndex() == 0) {
            renderBills(allBills);
            updateStats(allBills);
        }
    }

    private void loadAllReturns() {
        allReturns.setAll(systemFacade.getAllReturns());
        if (historyTabPane.getSelectionModel().getSelectedIndex() == 1) {
            renderReturns(allReturns);
            updateReturnStats(allReturns);
        }
    }

    private void renderBills(List<Bill> bills) {
        billGrid.getChildren().clear();
        salesCountLabel.setText(bills.size() + (bills.size() == 1 ? " record" : " records"));

        if (bills.isEmpty()) {
            Label empty = new Label("No sales records found.");
            empty.getStyleClass().add("return-empty-state");
            empty.setPrefWidth(440);
            empty.setAlignment(Pos.CENTER);
            billGrid.getChildren().add(empty);
            return;
        }

        for (Bill bill : bills) {
            billGrid.getChildren().add(createBillCard(bill));
        }
    }

    private VBox createBillCard(Bill bill) {
        VBox card = new VBox(0);
        card.getStyleClass().add("sales-card");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox title = new VBox(3);
        HBox.setHgrow(title, Priority.ALWAYS);

        Label billId = new Label("BLL-" + bill.getBillId());
        billId.getStyleClass().add("sales-bill-id");

        Label date = new Label(bill.getBillDate().format(dtf));
        date.getStyleClass().add("inv-card-barcode");

        title.getChildren().addAll(billId, date);

        Label payment = new Label("Cash");
        payment.getStyleClass().add("badge-active");
        top.getChildren().addAll(title, payment);

        HBox metrics = new HBox(8,
                metric("Amount", String.format("Rs. %,.0f", bill.getTotalAmount()), true),
                metric("Items", String.valueOf(bill.getItems().size()), false),
                metric("Cashier", bill.getUser() != null ? bill.getUser().getFullName() : "N/A", false));
        VBox.setMargin(metrics, new javafx.geometry.Insets(14, 0, 14, 0));

        HBox bottom = new HBox(8);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        Label change = new Label("Change: Rs. " + String.format("%,.0f", bill.getreturnCash()));
        change.getStyleClass().add("sales-change");
        bottom.getChildren().add(change);

        card.getChildren().addAll(top, divider(), metrics, divider(), bottom);
        return card;
    }

    private void renderReturns(List<ReturnTransaction> returns) {
        returnsGrid.getChildren().clear();
        returnsCountLabel.setText(returns.size() + (returns.size() == 1 ? " record" : " records"));

        if (returns.isEmpty()) {
            Label empty = new Label("No return records found.");
            empty.getStyleClass().add("return-empty-state");
            empty.setPrefWidth(440);
            empty.setAlignment(Pos.CENTER);
            returnsGrid.getChildren().add(empty);
            return;
        }

        for (ReturnTransaction tx : returns) {
            returnsGrid.getChildren().add(createReturnCard(tx));
        }
    }

    private VBox createReturnCard(ReturnTransaction tx) {
        VBox card = new VBox(0);
        card.getStyleClass().add("sales-card");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox title = new VBox(3);
        HBox.setHgrow(title, Priority.ALWAYS);

        Label retId = new Label("RET-" + tx.getReturnId());
        retId.getStyleClass().add("sales-bill-id");

        Label date = new Label(tx.getReturnDate().format(dtf));
        date.getStyleClass().add("inv-card-barcode");

        title.getChildren().addAll(retId, date);

        Label type = new Label("Refund");
        type.getStyleClass().add("badge-warning");
        top.getChildren().addAll(title, type);

        int itemsCount = tx.getReturnedItems().stream().mapToInt(ReturnItem::getReturnQuantity).sum();

        HBox metrics = new HBox(8,
                metric("Refund", String.format("Rs. %,.0f", tx.getRefundAmount()), true),
                metric("Items", String.valueOf(itemsCount), false),
                metric("Original Bill", "BLL-" + tx.getOriginalBill().getBillId(), false));
        VBox.setMargin(metrics, new javafx.geometry.Insets(14, 0, 14, 0));

        HBox bottom = new HBox(8);
        bottom.setAlignment(Pos.CENTER_LEFT);
        Label reason = new Label("Reason: " + (tx.getReason() == null || tx.getReason().isEmpty() ? "None" : tx.getReason()));
        reason.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        bottom.getChildren().add(reason);

        card.getChildren().addAll(top, divider(), metrics, divider(), bottom);
        return card;
    }

    private VBox metric(String labelText, String valueText, boolean amount) {
        VBox box = new VBox(3);
        box.getStyleClass().add("sales-mini-metric");
        Label label = new Label(labelText);
        label.getStyleClass().add("inv-card-field-label");
        Label value = new Label(valueText);
        value.getStyleClass().add(amount ? "sales-amount" : "inv-card-field-value");
        value.setWrapText(true);
        value.setMaxWidth(116);
        box.getChildren().addAll(label, value);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private Region divider() {
        Region region = new Region();
        region.getStyleClass().add("inv-card-divider");
        return region;
    }

    private void updateStats(List<Bill> bills) {
        double total = bills.stream().mapToDouble(Bill::getTotalAmount).sum();
        int count = bills.size();
        double avg = count > 0 ? total / count : 0;
        totalSalesLabel.setText(String.format("Rs. %,.0f", total));
        totalOrdersLabel.setText(String.valueOf(count));
        avgOrderLabel.setText(String.format("Rs. %,.2f", avg));
    }

    private void updateReturnStats(List<ReturnTransaction> returns) {
        double total = returns.stream().mapToDouble(ReturnTransaction::getRefundAmount).sum();
        int count = returns.size();
        double avg = count > 0 ? total / count : 0;
        totalSalesLabel.setText(String.format("Rs. %,.0f", total));
        totalOrdersLabel.setText(String.valueOf(count));
        avgOrderLabel.setText(String.format("Rs. %,.2f", avg));
    }

    @FXML
    private void handleFilter() {
        LocalDate from = dateFrom.getValue();
        LocalDate to = dateTo.getValue();
        String query = searchField.getText();

        boolean isSalesMode = historyTabPane.getSelectionModel().getSelectedIndex() == 0;

        if (isSalesMode) {
            List<Bill> filtered;
            if (from != null && to != null) {
                LocalDateTime fromDT = from.atStartOfDay();
                LocalDateTime toDT = to.atTime(LocalTime.MAX);
                filtered = systemFacade.filterByDateRange(fromDT, toDT);
            } else {
                filtered = FXCollections.observableArrayList(allBills);
            }

            if (query != null && !query.trim().isEmpty()) {
                String q = query.trim().toLowerCase();
                filtered.removeIf(b -> !String.valueOf(b.getBillId()).contains(q)
                        && !(b.getUser() != null && b.getUser().getFullName().toLowerCase().contains(q)));
            }

            renderBills(filtered);
            updateStats(filtered);
        } else {
            List<ReturnTransaction> filtered = FXCollections.observableArrayList(allReturns);
            
            if (from != null && to != null) {
                LocalDateTime fromDT = from.atStartOfDay();
                LocalDateTime toDT = to.atTime(LocalTime.MAX);
                filtered.removeIf(r -> r.getReturnDate().isBefore(fromDT) || r.getReturnDate().isAfter(toDT));
            }

            if (query != null && !query.trim().isEmpty()) {
                String q = query.trim().toLowerCase();
                filtered.removeIf(r -> !String.valueOf(r.getReturnId()).contains(q)
                        && !String.valueOf(r.getOriginalBill().getBillId()).contains(q)
                        && !(r.getReason() != null && r.getReason().toLowerCase().contains(q)));
            }

            renderReturns(filtered);
            updateReturnStats(filtered);
        }
    }

    @FXML
    private void handleReset() {
        dateFrom.setValue(null);
        dateTo.setValue(null);
        searchField.clear();
        
        if (historyTabPane.getSelectionModel().getSelectedIndex() == 0) {
            loadAllBills();
        } else {
            loadAllReturns();
        }
    }
}
