package util;

public class Validator {

    // Month & Year
    public static boolean isValidMonth(int month) {
        return month >= 1 && month <= 12;
    }

    public static boolean isValidYear(int year) {
        return year >= 2020 && year <= 2030;
    }

    // Amounts
    public static boolean isPositiveAmount(double amount) {
        return amount > 0;
    }

    public static boolean isPositiveQuantity(int quantity) {
        return quantity > 0;
    }

    // Strings
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // CNIC (Pakistani format: XXXXX-XXXXXXX-X)
    public static boolean isValidCNIC(String cnic) {
        if (cnic == null) return false;
        return cnic.matches("\\d{5}-\\d{7}-\\d{1}");
    }

    // Phone (Pakistani format: 03XX-XXXXXXX)
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return phone.matches("0\\d{3}-\\d{7}");
    }

    // Barcode (at least 1 character, non-empty)
    public static boolean isValidBarcode(String barcode) {
        return isNotEmpty(barcode);
    }

    // Object null check
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }
}
