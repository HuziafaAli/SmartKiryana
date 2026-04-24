package command;

import controller.BillController;
import model.Bill;
import model.ReturnItem;

import java.util.List;

public class ReturnItemCommand implements Command {

    private BillController billController;
    private Bill originalBill;
    private List<ReturnItem> items;
    private String reason;

    public ReturnItemCommand(BillController billController, Bill originalBill, List<ReturnItem> items, String reason) {
        this.billController = billController;
        this.originalBill = originalBill;
        this.items = items;
        this.reason = reason;
    }

    @Override
    public void execute() {
        billController.processReturn(originalBill, items, reason);
    }
}
