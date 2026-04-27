package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import model.Product;
import model.ProductCategory;
import model.InventoryItem;
import facade.SystemFacade;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryController implements FacadeAware {

    @FXML
    private TextField productSearchField;
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private FlowPane productGrid;

    @FXML
    private TextField categorySearchField;
    @FXML
    private FlowPane categoryGrid;

    private SystemFacade systemFacade;
    private List<InventoryItem> allItems;
    private List<ProductCategory> allCategories;

    public void initialize() {
        productSearchField.textProperty().addListener((obs, o, n) -> filterProducts());
        categorySearchField.textProperty().addListener((obs, o, n) -> filterCategories());
    }

    @Override
    public void setSystemFacade(SystemFacade facade) {
        this.systemFacade = facade;
        refreshData();
    }

    private void refreshData() {
        allItems = systemFacade.getAllInventoryItems();
        allCategories = systemFacade.getAllCategories();

        ObservableList<String> catNames = FXCollections.observableArrayList("All Categories");
        catNames.addAll(allCategories.stream()
                .map(ProductCategory::getCategoryName)
                .collect(Collectors.toList()));
        categoryFilter.setItems(catNames);
        categoryFilter.setValue("All Categories");
        categoryFilter.setOnAction(e -> filterProducts());

        loadProductCards();
        loadCategoryCards();
    }

    private void filterProducts() {
        loadProductCards();
    }

    private void loadProductCards() {
        productGrid.getChildren().clear();
        String query = productSearchField.getText();
        String selectedCat = categoryFilter.getValue();

        List<InventoryItem> filtered = allItems.stream().filter(item -> {
            boolean matchSearch = query == null || query.isEmpty()
                    || item.getProduct().getName().toLowerCase().contains(query.toLowerCase())
                    || item.getProduct().getBarcode().contains(query);
            boolean matchCat = selectedCat == null || selectedCat.equals("All Categories")
                    || (item.getProduct().getCategory() != null
                            && item.getProduct().getCategory().getCategoryName().equals(selectedCat));
            return matchSearch && matchCat;
        }).collect(Collectors.toList());

        for (InventoryItem item : filtered) {
            productGrid.getChildren().add(createProductCard(item));
        }
    }

    private VBox createProductCard(InventoryItem item) {
        Product p = item.getProduct();

        VBox card = new VBox(0);
        card.getStyleClass().add("inv-product-card");

        Label catPill = new Label(
                p.getCategory() != null ? p.getCategory().getCategoryName() : "Uncategorized");
        catPill.getStyleClass().add("inv-card-cat-pill");

        Label statusBadge = buildStatusBadge(item);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topRow = new HBox(8, catPill, topSpacer, statusBadge);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(p.getName());
        nameLabel.getStyleClass().add("inv-card-name");
        nameLabel.setMaxWidth(223);
        VBox.setMargin(nameLabel, new javafx.geometry.Insets(10, 0, 2, 0));

        Label barcodeLabel = new Label("# " + p.getBarcode());
        barcodeLabel.getStyleClass().add("inv-card-barcode");
        VBox.setMargin(barcodeLabel, new javafx.geometry.Insets(0, 0, 12, 0));

        Region div1 = new Region();
        div1.getStyleClass().add("inv-card-divider");

        VBox priceCol = buildInfoColumn("PRICE", String.format("Rs. %.0f", p.getPrice()), true);
        VBox costCol = buildInfoColumn("COST PRICE", String.format("Rs. %.0f", p.getCostPrice()), false);
        VBox stockCol = buildInfoColumn("STOCK", item.getStockQuantity() + " pcs", false);

        Region vDiv1 = verticalDivider();
        Region vDiv2 = verticalDivider();

        HBox statsRow = new HBox(0, priceCol, vDiv1, costCol, vDiv2, stockCol);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(priceCol, Priority.ALWAYS);
        HBox.setHgrow(costCol, Priority.ALWAYS);
        HBox.setHgrow(stockCol, Priority.ALWAYS);
        VBox.setMargin(statsRow, new javafx.geometry.Insets(12, 0, 12, 0));

        Region div2 = new Region();
        div2.getStyleClass().add("inv-card-divider");

        Button editBtn = new Button("Edit");
        Button stockBtn = new Button("+ Stock");
        Button delBtn = new Button("Delete");
        editBtn.getStyleClass().add("btn-table-action");
        stockBtn.getStyleClass().add("btn-table-action");
        delBtn.getStyleClass().add("btn-danger");

        editBtn.setOnAction(e -> handleEditProduct(item));
        stockBtn.setOnAction(e -> handleAddStock(item));
        delBtn.setOnAction(e -> handleDeleteProduct(item));

        Region btnSpacer = new Region();
        HBox.setHgrow(btnSpacer, Priority.ALWAYS);

        HBox actionsRow = new HBox(8, editBtn, stockBtn, btnSpacer, delBtn);
        actionsRow.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(actionsRow, new javafx.geometry.Insets(12, 0, 0, 0));

        card.getChildren().addAll(topRow, nameLabel, barcodeLabel,
                div1, statsRow, div2, actionsRow);

        DropShadow restShadow = makeShadow(0.50, 18, 6);
        DropShadow hoverShadow = makeShadow(0.70, 28, 12);
        card.setEffect(restShadow);
        card.setOnMouseEntered(e -> {
            card.setEffect(hoverShadow);
            card.setTranslateY(-3);
        });
        card.setOnMouseExited(e -> {
            card.setEffect(restShadow);
            card.setTranslateY(0);
        });

        return card;
    }

    @FXML
    private void handleAddProduct() {
        List<ProductCategory> cats = systemFacade.getAllCategories();

        TextField barcodeF = field("Barcode");
        TextField nameF = field("Product Name");
        ComboBox<ProductCategory> catBox = new ComboBox<>(FXCollections.observableArrayList(cats));
        catBox.setPromptText("Select Category");
        TextField priceF = field("Selling Price");
        TextField costF = field("Cost Price");
        TextField minF = field("Min Stock");
        minF.setText("10");
        TextField maxF = field("Max Stock");
        maxF.setText("100");

        GridPane grid = dialogGrid();
        grid.add(new Label("Barcode:"), 0, 0);
        grid.add(barcodeF, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameF, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(catBox, 1, 2);
        grid.add(new Label("Price:"), 0, 3);
        grid.add(priceF, 1, 3);
        grid.add(new Label("Cost Price:"), 0, 4);
        grid.add(costF, 1, 4);
        grid.add(new Label("Min Stock:"), 0, 5);
        grid.add(minF, 1, 5);
        grid.add(new Label("Max Stock:"), 0, 6);
        grid.add(maxF, 1, 6);

        Dialog<ButtonType> dialog = buildDialog("Add New Product", grid);
        applyDialogStyle(dialog);
        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK)
                return;
            try {
                ProductCategory selCat = catBox.getValue();
                if (selCat == null) {
                    showAlert("Error", "Please select a category.");
                    return;
                }

                boolean ok = systemFacade.addProduct(
                        barcodeF.getText(), nameF.getText(), selCat.getCategoryId(),
                        Double.parseDouble(priceF.getText()),
                        Double.parseDouble(costF.getText()),
                        Integer.parseInt(minF.getText()),
                        Integer.parseInt(maxF.getText()));

                if (ok)
                    refreshData();
                else
                    showAlert("Error", "Failed to add product.");
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid number format.");
            }
        });
    }

    private void handleEditProduct(InventoryItem item) {
        Product p = item.getProduct();

        TextField nameF = new TextField(p.getName());
        TextField priceF = new TextField(String.valueOf(p.getPrice()));
        TextField costF = new TextField(String.valueOf(p.getCostPrice()));

        GridPane grid = dialogGrid();
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameF, 1, 0);
        grid.add(new Label("Price:"), 0, 1);
        grid.add(priceF, 1, 1);
        grid.add(new Label("Cost:"), 0, 2);
        grid.add(costF, 1, 2);

        Dialog<ButtonType> dialog = buildDialog("Edit Product — " + p.getBarcode(), grid);
        applyDialogStyle(dialog);
        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK)
                return;
            try {
                boolean ok = systemFacade.updateProduct(p.getBarcode(), nameF.getText(),
                        Double.parseDouble(priceF.getText()),
                        Double.parseDouble(costF.getText()));
                if (ok)
                    refreshData();
                else
                    showAlert("Error", "Failed to update.");
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid number.");
            }
        });
    }

    private void handleAddStock(InventoryItem item) {
        TextInputDialog d = new TextInputDialog("10");
        d.setTitle("Add Stock");
        d.setHeaderText("Add stock for: " + item.getProduct().getName());
        d.setContentText("Quantity to add:");
        applyDialogStyle(d);
        d.showAndWait().ifPresent(qty -> {
            try {
                boolean ok = systemFacade.addStock(
                        item.getProduct().getBarcode(), Integer.parseInt(qty));
                if (ok)
                    refreshData();
                else
                    showAlert("Error", "Failed to add stock.");
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid number.");
            }
        });
    }

    private void handleDeleteProduct(InventoryItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Product");
        confirm.setContentText("Delete \"" + item.getProduct().getName() + "\"?");
        applyDialogStyle(confirm);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK)
                return;
            boolean ok = systemFacade.deleteProduct(item.getProduct().getBarcode());
            if (ok)
                refreshData();
            else
                showAlert("Error", "Failed to delete.");
        });
    }

    private void filterCategories() {
        loadCategoryCards();
    }

    private void loadCategoryCards() {
        categoryGrid.getChildren().clear();
        String query = categorySearchField.getText().toLowerCase();
        for (ProductCategory cat : allCategories) {
            if (query.isEmpty() || cat.getCategoryName().toLowerCase().contains(query)) {
                categoryGrid.getChildren().add(createCategoryCard(cat));
            }
        }
    }

    private VBox createCategoryCard(ProductCategory cat) {
        VBox card = new VBox(0);
        card.getStyleClass().add("inv-category-card");

        String initial = cat.getCategoryName().isEmpty() ? "?" : cat.getCategoryName().substring(0, 1).toUpperCase();
        Label iconBox = new Label(initial);
        iconBox.setStyle(
                "-fx-background-color: rgba(139,92,246,0.18);" +
                        "-fx-background-radius: 10;" +
                        "-fx-text-fill: #a78bfa;" +
                        "-fx-font-size: 18px;" +
                        "-fx-padding: 8 10;" +
                        "-fx-alignment: CENTER;");
        VBox.setMargin(iconBox, new javafx.geometry.Insets(0, 0, 12, 0));

        Label nameLabel = new Label(cat.getCategoryName());
        nameLabel.getStyleClass().add("inv-cat-name");
        nameLabel.setMaxWidth(191);

        Label idLabel = new Label("ID: " + cat.getCategoryId());
        idLabel.getStyleClass().add("inv-cat-id");
        VBox.setMargin(idLabel, new javafx.geometry.Insets(3, 0, 0, 0));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox.setMargin(spacer, new javafx.geometry.Insets(12, 0, 0, 0));

        Region divider = new Region();
        divider.getStyleClass().add("inv-card-divider");
        VBox.setMargin(divider, new javafx.geometry.Insets(0, 0, 12, 0));

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-table-action");
        editBtn.setOnAction(e -> handleEditCategory(cat));
        editBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(editBtn, Priority.ALWAYS);

        Button delBtn = new Button("Delete");
        delBtn.getStyleClass().add("btn-danger");
        delBtn.setOnAction(e -> handleDeleteCategory(cat));
        delBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(delBtn, Priority.ALWAYS);

        HBox actions = new HBox(8, editBtn, delBtn);
        actions.setAlignment(Pos.CENTER);

        card.getChildren().addAll(iconBox, nameLabel, idLabel, spacer, divider, actions);

        DropShadow restShadow = makeShadow(0.50, 18, 6);
        DropShadow hoverShadow = makeShadow(0.70, 28, 12);
        card.setEffect(restShadow);
        card.setOnMouseEntered(e -> {
            card.setEffect(hoverShadow);
            card.setTranslateY(-3);
        });
        card.setOnMouseExited(e -> {
            card.setEffect(restShadow);
            card.setTranslateY(0);
        });

        return card;
    }

    @FXML
    private void handleAddCategory() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Add Category");
        d.setHeaderText("Create a new product category");
        d.setContentText("Category Name:");
        applyDialogStyle(d);
        d.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                boolean ok = systemFacade.addCategory(name.trim());
                if (ok)
                    refreshData();
                else
                    showAlert("Error", "Failed to add category.");
            }
        });
    }

    private void handleEditCategory(ProductCategory cat) {
        TextInputDialog d = new TextInputDialog(cat.getCategoryName());
        d.setTitle("Edit Category");
        d.setHeaderText("Rename: " + cat.getCategoryName());
        d.setContentText("New Name:");
        applyDialogStyle(d);
        d.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                boolean ok = systemFacade.updateCategory(cat.getCategoryId(), name.trim());
                if (ok)
                    refreshData();
                else
                    showAlert("Error", "Failed to update category.");
            }
        });
    }

    private void handleDeleteCategory(ProductCategory cat) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Category");
        confirm.setContentText(
                "Delete \"" + cat.getCategoryName() + "\"?\n" +
                        "This will fail if any products use this category.");
        applyDialogStyle(confirm);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK)
                return;
            boolean ok = systemFacade.deleteCategory(cat.getCategoryId());
            if (ok)
                refreshData();
            else
                showAlert("Error", "Cannot delete — category may be in use.");
        });
    }

    private VBox buildInfoColumn(String labelText, String valueText, boolean isPrice) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("inv-card-field-label");
        Label val = new Label(valueText);
        val.getStyleClass().add(isPrice ? "inv-card-price-value" : "inv-card-field-value");
        VBox col = new VBox(3, lbl, val);
        col.setAlignment(Pos.CENTER_LEFT);
        col.setPadding(new javafx.geometry.Insets(0, 10, 0, 0));
        return col;
    }

    private Region verticalDivider() {
        Region r = new Region();
        r.setStyle("-fx-background-color: rgba(255,255,255,0.07);");
        r.setPrefWidth(1);
        r.setMinWidth(1);
        r.setMaxWidth(1);
        r.setPrefHeight(36);
        r.setMaxHeight(36);
        HBox.setMargin(r, new javafx.geometry.Insets(0, 10, 0, 0));
        return r;
    }

    private Label buildStatusBadge(InventoryItem item) {
        Label badge = new Label();
        if (item.getStockQuantity() == 0) {
            badge.setText("Out of Stock");
            badge.getStyleClass().add("badge-inactive");
        } else if (item.isLowStock()) {
            badge.setText("Low Stock");
            badge.getStyleClass().add("badge-warning");
        } else {
            badge.setText("In Stock");
            badge.getStyleClass().add("badge-active");
        }
        return badge;
    }

    private DropShadow makeShadow(double opacity, double radius, double offsetY) {
        DropShadow s = new DropShadow();
        s.setColor(Color.color(0, 0, 0, opacity));
        s.setRadius(radius);
        s.setOffsetX(0);
        s.setOffsetY(offsetY);
        return s;
    }

    private TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        return f;
    }

    private GridPane dialogGrid() {
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(10);
        return g;
    }

    private Dialog<ButtonType> buildDialog(String title, GridPane content) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(title);
        d.getDialogPane().setContent(content);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.setResultConverter(clickedButton -> clickedButton);
        return d;
    }

    private void applyDialogStyle(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("style.css").toExternalForm());
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setContentText(msg);
        applyDialogStyle(a);
        a.showAndWait();
    }
}