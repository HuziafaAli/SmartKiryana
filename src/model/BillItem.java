package model;

public class BillItem {

    private int billItemId;
    private Product product;
    private int quantity;
    private double unitPrice; 

    public BillItem() {
    }

    public BillItem(int billItemId, Product product, int quantity, double unitPrice) {
        this.billItemId = billItemId;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Returns the line total for this item
    public double getSubtotal() {
        return quantity * unitPrice;
    }

    public int getBillItemId() {
        return billItemId;
    }

    public void setBillItemId(int billItemId) {
        this.billItemId = billItemId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Override
    public String toString() {
        return "BillItem{" +
                "product=" + (product != null ? product.getName() : "None") +
                ", quantity=" + quantity +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
