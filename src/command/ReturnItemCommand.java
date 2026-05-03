package command;

import controller.BillController;
import model.Bill;
import model.ReturnItem;
import model.ReturnTransaction;

import java.util.List;

// Encapsulates a product return operation so it can be executed as a command
public class ReturnItemCommand implements Command {

    private BillController billController;
    private Bill originalBill;
    private List<ReturnItem> items;
    private String reason;
    private ReturnTransaction resultTransaction;

    public ReturnItemCommand(BillController billController, Bill originalBill, List<ReturnItem> items, String reason) {
        this.billController = billController;
        this.originalBill = originalBill;
        this.items = items;
        this.reason = reason;
    }

    // Delegates the return processing to the bill controller
    @Override
    public void execute() {
        this.resultTransaction = billController.processReturn(originalBill, items, reason);
    }

    public ReturnTransaction getResult() {
        return resultTransaction;
    }
}
