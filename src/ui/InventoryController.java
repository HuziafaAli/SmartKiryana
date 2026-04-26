package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Product;
import model.ProductCategory;
import model.InventoryItem;
import facade.SystemFacade;
import java.util.stream.Collectors;

public class InventoryController {

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colBarcode;
    @FXML private TableColumn<Product, String> colProductName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Double> colCostPrice;

    @FXML private TableView<ProductCategory> categoryTable;
    @FXML private TableColumn<ProductCategory, Integer> colCatId;
    @FXML private TableColumn<ProductCategory, String> colCatName;

    @FXML private TextField productSearchField;

    private SystemFacade systemFacade;
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<ProductCategory> categoryList = FXCollections.observableArrayList();

    public void initialize() {
        // Setup Product Columns
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("name")); // Fixed: was getProductName
        colCategory.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory().getCategoryName()));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colCostPrice.setCellValueFactory(new PropertyValueFactory<>("costPrice"));

        // Setup Category Columns
        colCatId.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        colCatName.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        // Add search listener
        productSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterProducts(newVal);
        });
    }

    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
        refreshData();
    }

    private void refreshData() {
        if (systemFacade != null) {
            // Get products from InventoryItems
            productList.setAll(
                systemFacade.getAllInventoryItems().stream()
                    .map(InventoryItem::getProduct)
                    .collect(Collectors.toList())
            );
            categoryList.setAll(systemFacade.getAllCategories());
            productTable.setItems(productList);
            categoryTable.setItems(categoryList);
        }
    }

    private void filterProducts(String query) {
        if (query == null || query.isEmpty()) {
            productTable.setItems(productList);
        } else {
            ObservableList<Product> filtered = productList.filtered(p -> 
                p.getName().toLowerCase().contains(query.toLowerCase()) || // Fixed: was getProductName
                p.getBarcode().contains(query)
            );
            productTable.setItems(filtered);
        }
    }

    @FXML
    private void handleAddProduct() {
        System.out.println("Add Product Dialog opening...");
    }

    @FXML
    private void handleAddCategory() {
        System.out.println("Add Category Dialog opening...");
    }
}
