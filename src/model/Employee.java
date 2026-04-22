package model;

public class Employee extends User {

    private String cnic;

    public Employee() {
        setRole("EMPLOYEE");
    }

    public Employee(int userId, String username, String password, String fullName, String phone, String cnic) {
        super(userId, username, password, fullName, phone, "EMPLOYEE");
        this.cnic = cnic;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "userId=" + getUserId() +
                ", fullName='" + getFullName() + '\'' +
                ", cnic='" + cnic + '\'' +
                '}';
    }
}
