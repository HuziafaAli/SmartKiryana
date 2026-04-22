package model;

public class Admin extends User {

    public Admin() {
        setRole("ADMIN");
    }

    public Admin(int userId, String username, String password, String fullName, String phone) {
        super(userId, username, password, fullName, phone, "ADMIN");
    }

    @Override
    public String toString() {
        return "Admin{" +
                "userId=" + getUserId() +
                ", fullName='" + getFullName() + '\'' +
                '}';
    }
}
