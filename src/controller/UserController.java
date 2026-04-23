package controller;

import model.User;
import model.Employee;
import model.Admin;

import java.util.ArrayList;
import java.util.List;

public class UserController {

    //Runtime Database
    private List<User> userDatabase;

    private User currentUser;

    public UserController() {
        this.userDatabase = new ArrayList<>();
        this.currentUser = null;

        
        Admin defaultAdmin = new Admin(1, "admin", 
                                    "admin123", "Super Admin",
                                    "0300-1234567");
        userDatabase.add(defaultAdmin);
    }

    public boolean login(String username, String password) {
        for(User user: userDatabase) {
            if(user.getUsername().equals(username) && user.getPassword().equals(password)) {
                if (!user.isActive()) {
                    System.out.println("Account is disabled.");
                    return false;
                }
                System.out.println("Login successful! Welcome " + user.getFullName());
                this.currentUser = user;
                return true;
            }
        }
        System.out.println("Invalid username or password.");
        return false;

    }

    public void logout() {
        this.currentUser = null;
        System.out.println("Logged out successfully.");
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public void addEmployee(String username, String password, String fullName, String phone, String cnic) {
        if (currentUser == null) {
            System.out.println("Error: No user is logged in.");
            return;
        }

        String role = currentUser.getRole();
        if (!role.equals("ADMIN")) {
            System.out.println("Access Denied: Only Admins can update employees.");
            return;
        }

        int newId = userDatabase.size() + 1;
        
        Employee newEmp = new Employee(newId, username, password, fullName, phone, cnic);
        userDatabase.add(newEmp);
        System.out.println("Employee " + fullName + " added successfully!");
    }

    public void updateEmployee(int userId, String newFullName, String newPhone, String newUsername, String newPassword) {
        
        if (currentUser == null) {
            System.out.println("Error: No user is logged in.");
            return;
        }

        String role = currentUser.getRole();
        if (!role.equals("ADMIN")) {
            System.out.println("Access Denied: Only Admins can update employees.");
            return;
        }
        
        for (User u : userDatabase) {
            if (u.getUserId() == userId) {
                u.setUsername(newUsername);
                u.setPassword(newPassword);
                u.setFullName(newFullName);
                u.setPhone(newPhone);
                System.out.println("Employee " + newFullName + " updated successfully!");
                return;
            }
        }

        System.out.println("Employee " + userId + " does not exist.");    
    }

    public void deactivateUser(int userId) {
        if (currentUser == null) {
            System.out.println("Error: No user is logged in.");
            return;
        }

        String role = currentUser.getRole();
        if (!role.equals("ADMIN")) {
            System.out.println("Access Denied: Only Admins can update employees.");
            return;
        }
        
        for (User u : userDatabase) {
            if (u.getUserId() == userId) {
                u.setActive(false);
                return;
            }
        }

        System.out.println("Employee " + userId + " does not exist.");  
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
