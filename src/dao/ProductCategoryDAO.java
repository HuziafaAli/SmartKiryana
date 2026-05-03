package dao;

import model.ProductCategory;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductCategoryDAO {

    // Inserts a new category and sets its generated ID
    public boolean save(ProductCategory category) {
        String query = "INSERT INTO product_categories (category_name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getCategoryName());
            boolean success = stmt.executeUpdate() > 0;

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                category.setCategoryId(rs.getInt(1));
            }
            return success;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieves a single category by its primary key
    public ProductCategory findById(int categoryId) {
        String query = "SELECT category_id, category_name FROM product_categories WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Returns every product category in the system
    public List<ProductCategory> findAll() {
        List<ProductCategory> categories = new ArrayList<>();
        String query = "SELECT category_id, category_name FROM product_categories";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                categories.add(mapRow(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // Renames an existing category
    public boolean update(ProductCategory category) {
        String query = "UPDATE product_categories SET category_name = ? WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, category.getCategoryName());
            stmt.setInt(2, category.getCategoryId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Deletes a category if no products reference it
    public boolean delete(int categoryId) {
        String query = "DELETE FROM product_categories WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ProductCategory mapRow(ResultSet rs) throws SQLException {
        return new ProductCategory(
                rs.getInt("category_id"),
                rs.getString("category_name"));
    }
}
