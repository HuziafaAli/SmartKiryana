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

public class SalesTargetDAO {

    private UserDAO userDAO;

    public SalesTargetDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean save(SalesTarget target) {
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

    public List<SalesTarget> findAll() {
        List<SalesTarget> targets = new ArrayList<>();
        String query = "SELECT * FROM sales_targets";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                targets.add(mapRow(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return targets;
    }



    private SalesTarget mapRow(ResultSet rs) throws SQLException {
        int employeeId = rs.getInt("employee_id");
        Employee emp = (Employee) userDAO.findById(employeeId);

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
