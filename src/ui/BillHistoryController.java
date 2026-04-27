package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
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

    @FXML private TableView<Bill> billTable;
    @FXML private TableColumn<Bill, String> colId;
    @FXML private TableColumn<Bill, String> colDate;
    @FXML private TableColumn<Bill, String> colCustomer;
    @FXML private TableColumn<Bill, String> colAmount;
    @FXML private TableColumn<Bill, String> colPayment;
    @FXML private TableColumn<Bill, String> colCashier;

    private SystemFacade systemFacade;
    private ObservableList<Bill> allBills = FXCollections.observableArrayList();
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");

    public void initialize() {
        colId.setCellValueFactory(c -> new SimpleStringProperty("BLL-" + c.getValue().getBillId()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBillDate().format(dtf)));
        colCustomer.setCellValueFactory(c -> new SimpleStringProperty("Walk-in Customer"));
        colAmount.setCellValueFactory(c -> new SimpleStringProperty(String.format("Rs. %,.2f", c.getValue().getTotalAmount())));
        colPayment.setCellValueFactory(c -> new SimpleStringProperty("Cash"));
        colCashier.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getUser() != null ? c.getValue().getUser().getFullName() : "N/A"));
    }

    @Override
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
        loadAllBills();
    }

    private void loadAllBills() {
        allBills.setAll(systemFacade.getAllBills());
        billTable.setItems(allBills);
        updateStats(allBills);
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

        if (from != null && to != null) {
            LocalDateTime fromDT = from.atStartOfDay();
            LocalDateTime toDT = to.atTime(LocalTime.MAX);
            List<Bill> filtered = systemFacade.filterByDateRange(fromDT, toDT);

            String query = searchField.getText();
            if (query != null && !query.isEmpty()) {
                filtered.removeIf(b -> !String.valueOf(b.getBillId()).contains(query));
            }

            billTable.setItems(FXCollections.observableArrayList(filtered));
            updateStats(filtered);
        } else {
            // Just search filter
            String query = searchField.getText();
            if (query != null && !query.isEmpty()) {
                ObservableList<Bill> filtered = allBills.filtered(b ->
                        String.valueOf(b.getBillId()).contains(query));
                billTable.setItems(filtered);
                updateStats(filtered);
            }
        }
    }

    @FXML
    private void handleReset() {
        dateFrom.setValue(null);
        dateTo.setValue(null);
        searchField.clear();
        loadAllBills();
    }
}
