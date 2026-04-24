package service;

import model.User;
import model.Employee;
import model.Admin;
import factory.UserFactory;
import util.Validator;

import java.util.ArrayList;
import java.util.List;

public class AuthService {
    //Runtime Database
    private List<User> userDatabase;
    private User currentUser;

    public AuthService() {
        this.userDatabase = new ArrayList<>();
        this.currentUser = null;

        // Default admin for testing
        Admin defaultAdmin = new Admin(1, "admin", "admin123", "Super Admin", "0300-1234567");
        userDatabase.add(defaultAdmin);
    }

    public boolean login(String username, String password) {
        if (!Validator.isNotEmpty(username) || !Validator.isNotEmpty(password)) {
            return false;
        }

        for (User user : userDatabase) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                if (!user.isActive()) {
                    return false; 
                }
                this.currentUser = user;
                return true; 
            }
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public boolean addEmployee(String username, String password, String fullName, String phone, String cnic) {
        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("ADMIN")) {
            return false; 
        }

        if (!Validator.isNotEmpty(username) || !Validator.isNotEmpty(password) 
                || !Validator.isNotEmpty(fullName) || !Validator.isValidPhone(phone) 
                || !Validator.isValidCNIC(cnic)) {
            return false;
        }

        int newId = userDatabase.size() + 1;
        User newEmp = UserFactory.createUser("EMPLOYEE", newId, username, password, fullName, phone, cnic);
        userDatabase.add(newEmp);
        return true;
    }

    public boolean updateEmployee(int userId, String newFullName, String newPhone, String newUsername, String newPassword) {
        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("ADMIN")) {
            return false;
        }

        if (!Validator.isNotEmpty(newUsername) || !Validator.isNotEmpty(newPassword) 
                || !Validator.isNotEmpty(newFullName) || !Validator.isValidPhone(newPhone)) {
            return false;
        }

        for (User u : userDatabase) {
            if (u.getUserId() == userId) {
                u.setUsername(newUsername);
                u.setPassword(newPassword);
                u.setFullName(newFullName);
                u.setPhone(newPhone);
                return true; 
            }
        }
        return false; 
    }

    public boolean deactivateUser(int userId) {
        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("ADMIN")) {
            return false;
        }

        if(!Validator.isPositiveQuantity(userId)) {
            return false;
        }

        for (User u : userDatabase) {
            if (u.getUserId() == userId) {
                u.setActive(false);
                return true;
            }
        }
        return false;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> tempEmp = new ArrayList<>();
        for (User u : userDatabase) {
            if (u.getRole().equals("EMPLOYEE")) {
                tempEmp.add((Employee) u);
            }
        }
        return tempEmp;
    }
}
