package dao;

import model.InventoryItem;
import model.Product;
import model.ProductCategory;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InventoryItemDAO {

    // Save a new product along with its inventory entry
    public boolean save(InventoryItem item) {
        String productQuery = "INSERT INTO products (barcode, product_name, category_id, price, cost_price, sales_quantity) VALUES (?, ?, ?, ?, ?, ?)";
        String inventoryQuery = "INSERT INTO inventory_items (barcode, stock_quantity, min_stock, max_stock) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement productStmt = conn.prepareStatement(productQuery, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement inventoryStmt = conn.prepareStatement(inventoryQuery, Statement.RETURN_GENERATED_KEYS)) {

                // Insert into products table
                Product p = item.getProduct();
                productStmt.setString(1, p.getBarcode());
                productStmt.setString(2, p.getName());
                productStmt.setInt(3, p.getCategory().getCategoryId());
                productStmt.setDouble(4, p.getPrice());
                productStmt.setDouble(5, p.getCostPrice());
                productStmt.setInt(6, p.getSalesQuantity());
                productStmt.executeUpdate();

                ResultSet productKeys = productStmt.getGeneratedKeys();
                if (productKeys.next()) {
                    p.setProductId(productKeys.getInt(1));
                }

                // Insert into inventory_items table
                inventoryStmt.setString(1, p.getBarcode());
                inventoryStmt.setInt(2, item.getStockQuantity());
                inventoryStmt.setInt(3, item.getMinStockThreshold());
                inventoryStmt.setInt(4, item.getMaxStockThreshold());
                inventoryStmt.executeUpdate();

                ResultSet inventoryKeys = inventoryStmt.getGeneratedKeys();
                if (inventoryKeys.next()) {
                    item.setInventoryId(inventoryKeys.getInt(1));
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Find an inventory item by its product barcode
    public InventoryItem findByBarcode(String barcode) {
        String query = "SELECT i.inventory_id, i.stock_quantity, i.min_stock, i.max_stock, "
                + "p.product_id, p.barcode, p.product_name, p.price, p.cost_price, p.sales_quantity, "
                + "c.category_id, c.category_name "
                + "FROM inventory_items i "
                + "JOIN products p ON i.barcode = p.barcode "
                + "JOIN product_categories c ON p.category_id = c.category_id "
                + "WHERE i.barcode = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Find a product by its barcode even if it is not in the inventory
    public Product findProductByBarcode(String barcode) {
        String query = "SELECT p.product_id, p.barcode, p.product_name, p.price, p.cost_price, p.sales_quantity, "
                + "c.category_id, c.category_name "
                + "FROM products p "
                + "JOIN product_categories c ON p.category_id = c.category_id "
                + "WHERE p.barcode = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ProductCategory category = new ProductCategory(
                        rs.getInt("category_id"),
                        rs.getString("category_name"));

                return new Product(
                        rs.getInt("product_id"),
                        rs.getString("barcode"),
                        rs.getString("product_name"),
                        category,
                        rs.getDouble("price"),
                        rs.getDouble("cost_price"),
                        rs.getInt("sales_quantity"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Fetch all inventory items
    public List<InventoryItem> findAll() {
        List<InventoryItem> items = new ArrayList<>();
        String query = "SELECT i.inventory_id, i.stock_quantity, i.min_stock, i.max_stock, "
                + "p.product_id, p.barcode, p.product_name, p.price, p.cost_price, p.sales_quantity, "
                + "c.category_id, c.category_name "
                + "FROM inventory_items i "
                + "JOIN products p ON i.barcode = p.barcode "
                + "JOIN product_categories c ON p.category_id = c.category_id";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapRow(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Update product details (name, price, cost price)
    public boolean updateProduct(String barcode, String newName, double newPrice, double newCostPrice) {
        String query = "UPDATE products SET product_name = ?, price = ?, cost_price = ? WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newName);
            stmt.setDouble(2, newPrice);
            stmt.setDouble(3, newCostPrice);
            stmt.setString(4, barcode);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update stock quantity in the database
    public boolean updateStock(String barcode, int newQuantity) {
        String query = "UPDATE inventory_items SET stock_quantity = ? WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, newQuantity);
            stmt.setString(2, barcode);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Insert only the inventory row (useful for restoring deleted stock)
    public boolean insertInventoryOnly(InventoryItem item) {
        String inventoryQuery = "INSERT INTO inventory_items (barcode, stock_quantity, min_stock, max_stock) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement inventoryStmt = conn.prepareStatement(inventoryQuery, Statement.RETURN_GENERATED_KEYS)) {

            inventoryStmt.setString(1, item.getProduct().getBarcode());
            inventoryStmt.setInt(2, item.getStockQuantity());
            inventoryStmt.setInt(3, item.getMinStockThreshold());
            inventoryStmt.setInt(4, item.getMaxStockThreshold());
            boolean success = inventoryStmt.executeUpdate() > 0;

            ResultSet rs = inventoryStmt.getGeneratedKeys();
            if (rs.next()) {
                item.setInventoryId(rs.getInt(1));
            }
            return success;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete an inventory entry (keeps the product in the master table)
    public boolean delete(String barcode) {
        String inventoryQuery = "DELETE FROM inventory_items WHERE barcode = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement invStmt = conn.prepareStatement(inventoryQuery)) {

            invStmt.setString(1, barcode);
            return invStmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    // Helper: Convert a database row into a Java object
    private InventoryItem mapRow(ResultSet rs) throws SQLException {
        ProductCategory category = new ProductCategory(
                rs.getInt("category_id"),
                rs.getString("category_name"));

        Product product = new Product(
                rs.getInt("product_id"),
                rs.getString("barcode"),
                rs.getString("product_name"),
                category,
                rs.getDouble("price"),
                rs.getDouble("cost_price"),
                rs.getInt("sales_quantity"));

        return new InventoryItem(
                rs.getInt("inventory_id"),
                product,
                rs.getInt("stock_quantity"),
                rs.getInt("min_stock"),
                rs.getInt("max_stock"));
    }
}
