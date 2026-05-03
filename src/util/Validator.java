package util;

// Centralized input validation helpers used across the service layer
public class Validator {

    public static boolean isValidMonth(int month) {
        return month >= 1 && month <= 12;
    }

    public static boolean isValidYear(int year) {
        return year >= 2020 && year <= 2030;
    }

    public static boolean isPositiveAmount(double amount) {
        return amount > 0;
    }

    public static boolean isPositiveQuantity(int quantity) {
        return quantity > 0;
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidCNIC(String cnic) {
        if (cnic == null) return false;
        return cnic.matches("\\d{5}-\\d{7}-\\d{1}");
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return phone.matches("0\\d{3}-\\d{7}");
    }

    public static boolean isValidBarcode(String barcode) {
        return isNotEmpty(barcode);
    }

    public static boolean isNotNull(Object obj) {
        return obj != null;
    }
}
