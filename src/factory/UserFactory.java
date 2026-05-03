package factory;

import model.User;
import model.Employee;
import model.Admin;

// Creates the correct User subclass based on the role string
public class UserFactory {

    public static User createUser(String role, int userId, String username, String password, String fullName, String phone, String cnic) {
        
        if (role.equalsIgnoreCase("ADMIN")) {
            return new Admin(userId, username, password, fullName, phone);
        } 
        else if (role.equalsIgnoreCase("EMPLOYEE")) {
            return new Employee(userId, username, password, fullName, phone, cnic);
        } 
        else {
            throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
}
