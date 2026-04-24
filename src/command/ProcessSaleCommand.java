package command;

import controller.BillController;
import model.User;

public class ProcessSaleCommand implements Command {

    private BillController billController;
    private User cashier;
    private String barcode;
    private int quantity;
    private double cashProvided;

    public ProcessSaleCommand(BillController billController, User cashier, String barcode, int quantity, double cashProvided) {
        this.billController = billController;
        this.cashier = cashier;
        this.barcode = barcode;
        this.quantity = quantity;
        this.cashProvided = cashProvided;
    }

    @Override
    public void execute() {
        billController.startNewBill(cashier);
        billController.scanItem(barcode, quantity);
        billController.checkOut(cashProvided);
    }
}
