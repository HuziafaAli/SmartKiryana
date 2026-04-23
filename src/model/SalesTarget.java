package model;

public class SalesTarget {

    private int targetId;
    private Employee employee;
    private int month;
    private int year;
    private double targetAmount;
    private double achievedAmount;

    public SalesTarget() {
    }

    public SalesTarget(int targetId, Employee employee, int month, int year, double targetAmount) {
        this.targetId = targetId;
        this.employee = employee;
        this.month = month;
        this.year = year;
        this.targetAmount = targetAmount;
        this.achievedAmount = 0.0; 
    }

    // Business Logic
    
    public double getAchievementPercentage() {
        if (targetAmount == 0) {
            return 0.0;
        }
        return (achievedAmount / targetAmount) * 100.0;
    }

    // Getters & Setters

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getAchievedAmount() {
        return achievedAmount;
    }

    public void setAchievedAmount(double achievedAmount) {
        this.achievedAmount = achievedAmount;
    }

    @Override
    public String toString() {
        return "SalesTarget{" +
                "employee=" + (employee != null ? employee.getFullName() : "None") +
                ", month=" + month + "/" + year +
                ", target=" + targetAmount +
                ", achieved=" + achievedAmount +
                '}';
    }
}
