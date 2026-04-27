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

public class BillHistoryController implements FacadeAware {

    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private TextField searchField;
    @FXML private Label totalSalesLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label avgOrderLabel;
    @FXML private Label resultCountLabel;
    @FXML private FlowPane billGrid;

    private SystemFacade systemFacade;
    private final ObservableList<Bill> allBills = FXCollections.observableArrayList();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");

    @Override
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
        loadAllBills();
    }

    private void loadAllBills() {
        allBills.setAll(systemFacade.getAllBills());
        renderBills(allBills);
        updateStats(allBills);
    }

    private void renderBills(List<Bill> bills) {
        billGrid.getChildren().clear();
        resultCountLabel.setText(bills.size() + (bills.size() == 1 ? " record" : " records"));

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

    @FXML
    private void handleFilter() {
        LocalDate from = dateFrom.getValue();
        LocalDate to = dateTo.getValue();
        String query = searchField.getText();

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
    }

    @FXML
    private void handleReset() {
        dateFrom.setValue(null);
        dateTo.setValue(null);
        searchField.clear();
        loadAllBills();
    }
}
