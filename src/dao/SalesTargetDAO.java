package dao;

import model.SalesTarget;
import model.Employee;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class SalesTargetDAO {

    private UserDAO userDAO;

    public SalesTargetDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // Saves a target or updates an existing one for the same employee/month/year
    public boolean save(SalesTarget target) {
        int existingId = findExistingTargetId(target.getEmployee().getUserId(), target.getMonth(), target.getYear());
        if (existingId > 0) {
            target.setTargetId(existingId);
            return update(target);
        }

        String query = "INSERT INTO sales_targets (employee_id, month, year, target_amount, achieved_amount) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, target.getEmployee().getUserId());
            stmt.setInt(2, target.getMonth());
            stmt.setInt(3, target.getYear());
            stmt.setDouble(4, target.getTargetAmount());
            stmt.setDouble(5, target.getAchievedAmount());
            boolean success = stmt.executeUpdate() > 0;

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                target.setTargetId(rs.getInt(1));
            }
            return success;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Updates the target and achieved amounts for an existing record
    public boolean update(SalesTarget target) {
        String query = "UPDATE sales_targets SET target_amount = ?, achieved_amount = ? WHERE target_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, target.getTargetAmount());
            stmt.setDouble(2, target.getAchievedAmount());
            stmt.setInt(3, target.getTargetId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Fetches all sales targets sorted by most recent period first
    public List<SalesTarget> findAll() {
        List<SalesTarget> targets = new ArrayList<>();
        Map<Integer, model.User> userCache = new HashMap<>();
        String query = "SELECT * FROM sales_targets ORDER BY year DESC, month DESC, target_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                targets.add(mapRow(rs, userCache));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return targets;
    }

    // Gets all targets for a specific employee, one per period
    public List<SalesTarget> findByEmployee(int employeeId) {
        List<SalesTarget> targets = new ArrayList<>();
        Map<Integer, model.User> userCache = new HashMap<>();
        String query = "SELECT * FROM sales_targets WHERE employee_id = ? ORDER BY year DESC, month DESC, target_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            Set<String> seenPeriods = new HashSet<>();
            while (rs.next()) {
                String periodKey = rs.getInt("year") + "-" + rs.getInt("month");
                if (seenPeriods.add(periodKey)) {
                    targets.add(mapRow(rs, userCache));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return targets;
    }

    // Checks if a target already exists for this employee in the given period
    private int findExistingTargetId(int employeeId, int month, int year) {
        String query = "SELECT target_id FROM sales_targets WHERE employee_id = ? AND month = ? AND year = ? ORDER BY target_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("target_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private SalesTarget mapRow(ResultSet rs, Map<Integer, model.User> userCache) throws SQLException {
        int employeeId = rs.getInt("employee_id");
        Employee emp;

        if (userCache.containsKey(employeeId)) {
            emp = (Employee) userCache.get(employeeId);
        } else {
            emp = (Employee) userDAO.findById(employeeId);
            userCache.put(employeeId, emp);
        }

        SalesTarget target = new SalesTarget(
                rs.getInt("target_id"),
                emp,
                rs.getInt("month"),
                rs.getInt("year"),
                rs.getDouble("target_amount"));
        target.setAchievedAmount(rs.getDouble("achieved_amount"));

        return target;
    }
}
