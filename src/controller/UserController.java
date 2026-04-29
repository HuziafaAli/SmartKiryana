package controller;

import service.AuthService;
import model.User;
import model.Employee;
import java.util.List;

public class UserController {

    private AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    public boolean login(String username, String password) {
        boolean success = authService.login(username, password);
        if (success) {
            System.out.println("Login successful! Welcome " + authService.getCurrentUser().getFullName());
        } else {
            System.out.println("Invalid username, password, or account disabled.");
        }
        return success;
    }

    public boolean logout() {
        boolean success = authService.logout();
        if (success) {
            System.out.println("Logged out successfully.");
        }
        return success;
    }

    public User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public boolean addEmployee(String username, String password, String fullName, String phone, String cnic) {
        boolean success = authService.addEmployee(username, password, fullName, phone, cnic);
        if (success) {
            System.out.println("Employee " + fullName + " added successfully!");
        } else {
            System.out.println("Access Denied: Only Admins can add employees.");
        }
        return success;
    }

    public boolean updateEmployee(int userId, String newFullName, String newPhone, String newUsername, String newPassword) {
        boolean success = authService.updateEmployee(userId, newFullName, newPhone, newUsername, newPassword);
        if (success) {
            System.out.println("Employee " + newFullName + " updated successfully!");
        } else {
            System.out.println("Failed to update. Access Denied or Employee does not exist.");
        }
        return success;
    }

    public boolean deactivateUser(int userId) {
        boolean success = authService.deactivateUser(userId);
        if (success) {
            System.out.println("Employee deactivated successfully.");
        } else {
            System.out.println("Failed to deactivate. Access Denied or Employee does not exist.");
        }
        return success;
    }

    public boolean activateUser(int userId) {
        boolean success = authService.activateUser(userId);
        if (success) {
            System.out.println("Employee activated successfully.");
        } else {
            System.out.println("Failed to activate. Access Denied or Employee does not exist.");
        }
        return success;
    }

    public List<Employee> getAllEmployees() {
        return authService.getAllEmployees();
    }
}
