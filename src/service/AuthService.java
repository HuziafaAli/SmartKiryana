package service;

import model.User;
import model.Employee;
import factory.UserFactory;
import dao.UserDAO;
import util.Validator;

import java.util.ArrayList;
import java.util.List;

public class AuthService {

    private UserDAO userDAO;
    private User currentUser;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.currentUser = null;
    }

    // Validates credentials and starts a session for active users
    public boolean login(String username, String password) {
        if (!Validator.isNotEmpty(username) || !Validator.isNotEmpty(password)) {
            return false;
        }

        List<User> allUsers = userDAO.findAll();
        for (User user : allUsers) {
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

    // Ends the current user session
    public boolean logout() {
        if (this.currentUser != null) {
            this.currentUser = null;
            return true;
        }
        return false;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    // Creates a new employee account after admin and input validation
    public boolean addEmployee(String username, String password, String fullName, String phone, String cnic) {
        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("ADMIN")) {
            return false;
        }

        if (!Validator.isNotEmpty(username) || !Validator.isNotEmpty(password)
                || !Validator.isNotEmpty(fullName) || !Validator.isValidPhone(phone)
                || !Validator.isValidCNIC(cnic)) {
            return false;
        }

        User newEmp = UserFactory.createUser("EMPLOYEE", 0, username, password, fullName, phone, cnic);
        return userDAO.save(newEmp);
    }

    // Updates an employee's profile fields after admin check
    public boolean updateEmployee(int userId, String newFullName, String newPhone, String newUsername, String newPassword) {
        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("ADMIN")) {
            return false;
        }

        if (!Validator.isNotEmpty(newUsername) || !Validator.isNotEmpty(newPassword)
                || !Validator.isNotEmpty(newFullName) || !Validator.isValidPhone(newPhone)) {
            return false;
        }

        List<User> allUsers = userDAO.findAll();
        for (User u : allUsers) {
            if (u.getUserId() == userId) {
                u.setUsername(newUsername);
                u.setPassword(newPassword);
                u.setFullName(newFullName);
                u.setPhone(newPhone);
                return userDAO.update(u);
            }
        }
        return false;
    }

    // Marks an employee as inactive so they can no longer log in
    public boolean deactivateUser(int userId) {
        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("ADMIN")) {
            return false;
        }

        if (!Validator.isPositiveQuantity(userId)) {
            return false;
        }

        List<User> allUsers = userDAO.findAll();
        for (User u : allUsers) {
            if (u.getUserId() == userId) {
                u.setActive(false);
                return userDAO.update(u);
            }
        }
        return false;
    }

    // Re-enables a previously deactivated account
    public boolean activateUser(int userId) {
        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("ADMIN")) {
            return false;
        }

        if (!Validator.isPositiveQuantity(userId)) {
            return false;
        }

        List<User> allUsers = userDAO.findAll();
        for (User u : allUsers) {
            if (u.getUserId() == userId) {
                u.setActive(true);
                return userDAO.update(u);
            }
        }
        return false;
    }

    // Filters and returns only employee-type users
    public List<Employee> getAllEmployees() {
        List<User> allUsers = userDAO.findAll();
        List<Employee> tempEmp = new ArrayList<>();
        for (User u : allUsers) {
            if (u.getRole().equals("EMPLOYEE")) {
                tempEmp.add((Employee) u);
            }
        }
        return tempEmp;
    }
}
