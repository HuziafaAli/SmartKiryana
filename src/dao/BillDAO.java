package dao;

import model.Bill;
import model.BillItem;
import model.Product;
import model.ProductCategory;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BillDAO {

    private static List<Bill> cachedBills = null;
    private static boolean isCacheDirty = true;

    public static void invalidateCache() {
        isCacheDirty = true;
    }

    private UserDAO userDAO;

    public BillDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // Persists the bill, its items, and updates product sales counters in one transaction
    public boolean save(Bill bill) {
        String billQuery = "INSERT INTO bills (user_id, bill_date, total_amount, tax_amount, discount_amount, cash_provided, return_cash) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String itemQuery = "INSERT INTO bill_items (bill_id, barcode, quantity, unit_price) VALUES (?, ?, ?, ?)";
        String productUpdateQuery = "UPDATE products SET sales_quantity = sales_quantity + ? WHERE barcode = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement billStmt = conn.prepareStatement(billQuery, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement itemStmt = conn.prepareStatement(itemQuery);
                    PreparedStatement productUpdateStmt = conn.prepareStatement(productUpdateQuery)) {

                billStmt.setInt(1, bill.getUser() != null ? bill.getUser().getUserId() : 0);
                billStmt.setTimestamp(2, Timestamp.valueOf(bill.getBillDate()));
                billStmt.setDouble(3, bill.getTotalAmount());
                billStmt.setDouble(4, bill.getTaxAmount());
                billStmt.setDouble(5, bill.getDiscountAmount());
                billStmt.setDouble(6, bill.getCashProvided());
                billStmt.setDouble(7, bill.getreturnCash());
                billStmt.executeUpdate();

                ResultSet generatedKeys = billStmt.getGeneratedKeys();
                int generatedBillId = 0;
                if (generatedKeys.next()) {
                    generatedBillId = generatedKeys.getInt(1);
                    bill.setBillId(generatedBillId);
                }

                for (BillItem item : bill.getItems()) {
                    itemStmt.setInt(1, generatedBillId);
                    itemStmt.setString(2, item.getProduct().getBarcode());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setDouble(4, item.getUnitPrice());
                    itemStmt.executeUpdate();

                    productUpdateStmt.setInt(1, item.getQuantity());
                    productUpdateStmt.setString(2, item.getProduct().getBarcode());
                    productUpdateStmt.executeUpdate();
                }

                conn.commit();
                invalidateCache();
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

    // Looks up a single bill by its ID including all line items
    public Bill findById(int billId) {
        String query = "SELECT * FROM bills WHERE bill_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, billId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<Integer, model.User> tempCache = new HashMap<>();
                Bill bill = mapBillRow(rs, tempCache);
                bill.setItems(findBillItems(billId));
                return bill;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Returns all bills with their items, using an in-memory cache when possible
    public List<Bill> findAll() {
        if (!isCacheDirty && cachedBills != null) {
            return new ArrayList<>(cachedBills);
        }
        Map<Integer, Bill> billMap = new LinkedHashMap<>();
        Map<Integer, model.User> userCache = new HashMap<>();
        String query = "SELECT b.*, bi.bill_item_id, bi.quantity, bi.unit_price, "
                + "p.product_id, p.barcode, p.product_name, p.price, p.cost_price, "
                + "c.category_id, c.category_name "
                + "FROM bills b "
                + "LEFT JOIN bill_items bi ON b.bill_id = bi.bill_id "
                + "LEFT JOIN products p ON bi.barcode = p.barcode "
                + "LEFT JOIN product_categories c ON p.category_id = c.category_id "
                + "ORDER BY b.bill_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int billId = rs.getInt("bill_id");
                Bill bill = billMap.get(billId);

                if (bill == null) {
                    bill = mapBillRow(rs, userCache);
                    billMap.put(billId, bill);
                }

                int billItemId = rs.getInt("bill_item_id");
                if (rs.wasNull())
                    continue;

                ProductCategory category = new ProductCategory(
                        rs.getInt("category_id"),
                        rs.getString("category_name"));

                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("barcode"),
                        rs.getString("product_name"),
                        category,
                        rs.getDouble("price"),
                        rs.getDouble("cost_price"));

                BillItem item = new BillItem(
                        billItemId,
                        product,
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"));

                bill.addItem(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        cachedBills = new ArrayList<>(billMap.values());
        isCacheDirty = false;
        return new ArrayList<>(cachedBills);
    }

    // Fetches the line items for a specific bill
    private List<BillItem> findBillItems(int billId) {
        List<BillItem> items = new ArrayList<>();
        String query = "SELECT bi.bill_item_id, bi.quantity, bi.unit_price, "
                + "p.product_id, p.barcode, p.product_name, p.price, p.cost_price, "
                + "c.category_id, c.category_name "
                + "FROM bill_items bi "
                + "JOIN products p ON bi.barcode = p.barcode "
                + "JOIN product_categories c ON p.category_id = c.category_id "
                + "WHERE bi.bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, billId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ProductCategory category = new ProductCategory(
                        rs.getInt("category_id"),
                        rs.getString("category_name"));

                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("barcode"),
                        rs.getString("product_name"),
                        category,
                        rs.getDouble("price"),
                        rs.getDouble("cost_price"));

                BillItem item = new BillItem(
                        rs.getInt("bill_item_id"),
                        product,
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"));

                items.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Converts a result set row into a Bill object with its cashier resolved
    private Bill mapBillRow(ResultSet rs, Map<Integer, model.User> userCache) throws SQLException {
        Bill bill = new Bill();
        bill.setBillId(rs.getInt("bill_id"));
        bill.setBillDate(rs.getTimestamp("bill_date").toLocalDateTime());
        bill.setTotalAmount(rs.getDouble("total_amount"));
        bill.setTaxAmount(rs.getDouble("tax_amount"));
        bill.setDiscountAmount(rs.getDouble("discount_amount"));
        bill.setCashProvided(rs.getDouble("cash_provided"));
        bill.setReturnCash(rs.getDouble("return_cash"));

        int userId = rs.getInt("user_id");
        if (userId > 0) {
            if (!userCache.containsKey(userId)) {
                userCache.put(userId, userDAO.findById(userId));
            }
            bill.setUser(userCache.get(userId));
        }

        return bill;
    }
}
