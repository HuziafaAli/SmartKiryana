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

    public void login(String username, String password) {
        boolean success = authService.login(username, password);
        
        if (success) {
            System.out.println("Login successful! Welcome " + authService.getCurrentUser().getFullName());
        } else {
            System.out.println("Invalid username, password, or account disabled.");
        }
    }

    public void logout() {
        authService.logout();
        System.out.println("Logged out successfully.");
    }

    public User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public void addEmployee(String username, String password, String fullName, String phone, String cnic) {
        boolean success = authService.addEmployee(username, password, fullName, phone, cnic);
        
        if (success) {
            System.out.println("Employee " + fullName + " added successfully!");
        } else {
            System.out.println("Access Denied: Only Admins can add employees.");
        }
    }

    public void updateEmployee(int userId, String newFullName, String newPhone, String newUsername, String newPassword) {
        boolean success = authService.updateEmployee(userId, newFullName, newPhone, newUsername, newPassword);
        
        if (success) {
            System.out.println("Employee " + newFullName + " updated successfully!");
        } else {
            System.out.println("Failed to update. Access Denied or Employee does not exist.");
        }
    }

    public void deactivateUser(int userId) {
        boolean success = authService.deactivateUser(userId);
        
        if (success) {
            System.out.println("Employee deactivated successfully.");
        } else {
            System.out.println("Failed to deactivate. Access Denied or Employee does not exist.");
        }
    }

    public List<Employee> getAllEmployees() {
        return authService.getAllEmployees();
    }
}
