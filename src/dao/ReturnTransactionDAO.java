package dao;

import model.ReturnTransaction;
import model.ReturnItem;
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

public class ReturnTransactionDAO {

    private static List<ReturnTransaction> cachedReturns = null;
    private static boolean isCacheDirty = true;

    public static void invalidateCache() {
        isCacheDirty = true;
    }

    private BillDAO billDAO;

    public ReturnTransactionDAO(BillDAO billDAO) {
        this.billDAO = billDAO;
    }

    // Saves a return transaction and all its line items in a single transaction
    public boolean save(ReturnTransaction returnTx) {
        String returnQuery = "INSERT INTO return_transactions (original_bill_id, refund_amount, return_date, reason) VALUES (?, ?, ?, ?)";
        String itemQuery = "INSERT INTO return_items (return_id, bill_item_id, return_quantity) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement returnStmt = conn.prepareStatement(returnQuery, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement itemStmt = conn.prepareStatement(itemQuery)) {

                returnStmt.setInt(1, returnTx.getOriginalBill().getBillId());
                returnStmt.setDouble(2, returnTx.getRefundAmount());
                returnStmt.setTimestamp(3, Timestamp.valueOf(returnTx.getReturnDate()));
                returnStmt.setString(4, returnTx.getReason());
                returnStmt.executeUpdate();

                ResultSet generatedKeys = returnStmt.getGeneratedKeys();
                int generatedReturnId = 0;
                if (generatedKeys.next()) {
                    generatedReturnId = generatedKeys.getInt(1);
                    returnTx.setReturnId(generatedReturnId);
                }

                for (ReturnItem item : returnTx.getReturnedItems()) {
                    itemStmt.setInt(1, generatedReturnId);
                    itemStmt.setInt(2, item.getOriginalItem().getBillItemId());
                    itemStmt.setInt(3, item.getReturnQuantity());
                    itemStmt.executeUpdate();
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

    // Loads all return transactions with their items, using cache when available
    public List<ReturnTransaction> findAll() {
        if (!isCacheDirty && cachedReturns != null) {
            return new ArrayList<>(cachedReturns);
        }
        java.util.Map<Integer, ReturnTransaction> returnMap = new java.util.LinkedHashMap<>();
        String query = "SELECT rt.*, ri.return_item_id, ri.return_quantity, "
                + "bi.bill_item_id, bi.quantity, bi.unit_price, "
                + "p.product_id, p.barcode, p.product_name, p.price, p.cost_price, "
                + "c.category_id, c.category_name "
                + "FROM return_transactions rt "
                + "LEFT JOIN return_items ri ON rt.return_id = ri.return_id "
                + "LEFT JOIN bill_items bi ON ri.bill_item_id = bi.bill_item_id "
                + "LEFT JOIN products p ON bi.barcode = p.barcode "
                + "LEFT JOIN product_categories c ON p.category_id = c.category_id "
                + "ORDER BY rt.return_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int returnId = rs.getInt("return_id");
                ReturnTransaction tx = returnMap.get(returnId);

                if (tx == null) {
                    tx = mapRow(rs);
                    returnMap.put(returnId, tx);
                }

                int returnItemId = rs.getInt("return_item_id");
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

                BillItem originalItem = new BillItem(
                        rs.getInt("bill_item_id"),
                        product,
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"));

                ReturnItem returnItem = new ReturnItem(
                        returnItemId,
                        originalItem,
                        rs.getInt("return_quantity"));

                tx.addReturnedItem(returnItem);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        cachedReturns = new ArrayList<>(returnMap.values());
        isCacheDirty = false;
        return new ArrayList<>(cachedReturns);
    }

    // Converts a result set row into a ReturnTransaction linked to its original bill
    private ReturnTransaction mapRow(ResultSet rs) throws SQLException {
        ReturnTransaction tx = new ReturnTransaction();
        tx.setReturnId(rs.getInt("return_id"));
        tx.setRefundAmount(rs.getDouble("refund_amount"));
        tx.setReturnDate(rs.getTimestamp("return_date").toLocalDateTime());
        tx.setReason(rs.getString("reason"));

        int billId = rs.getInt("original_bill_id");
        tx.setOriginalBill(billDAO.findById(billId));

        return tx;
    }
}
