package model;

public class ReturnItem {

    private int returnItemId;
    private BillItem originalItem;
    private int returnQuantity;

    public ReturnItem() {
    }

    public ReturnItem(int returnItemId, BillItem originalItem, int returnQuantity) {
        this.returnItemId = returnItemId;
        this.originalItem = originalItem;
        this.returnQuantity = returnQuantity;
    }

    // Calculates the refund amount for this returned quantity
    public double getRefundSubtotal() {
        if (originalItem == null) return 0.0;
        return returnQuantity * originalItem.getUnitPrice();
    }

    public int getReturnItemId() {
        return returnItemId;
    }

    public void setReturnItemId(int returnItemId) {
        this.returnItemId = returnItemId;
    }

    public BillItem getOriginalItem() {
        return originalItem;
    }

    public void setOriginalItem(BillItem originalItem) {
        this.originalItem = originalItem;
    }

    public int getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(int returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    @Override
    public String toString() {
        return "ReturnItem{" +
                "product=" + (originalItem != null && originalItem.getProduct() != null ? originalItem.getProduct().getName() : "None") +
                ", returnQty=" + returnQuantity +
                ", refundSubtotal=" + getRefundSubtotal() +
                '}';
    }
}
