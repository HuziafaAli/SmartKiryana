package model;

public class Product {

    private int productId;
    private String barcode;
    private String name;
    private ProductCategory category;
    private double price;       // Selling price
    private double costPrice;   // Purchase price

    public Product() {
    }

    public Product(int productId, String barcode, String name, ProductCategory category, double price, double costPrice) {
        this.productId = productId;
        this.barcode = barcode;
        this.name = name;
        this.category = category;
        this.price = price;
        this.costPrice = costPrice;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", barcode='" + barcode + '\'' +
                ", price=" + price +
                ", category=" + (category != null ? category.getCategoryName() : "None") +
                '}';
    }
}
