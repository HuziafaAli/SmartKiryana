package ui;

import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public final class DialogStyler {

    private DialogStyler() {
    }

    public static void style(Dialog<?> dialog) {
        if (dialog == null) {
            return;
        }

        DialogPane pane = dialog.getDialogPane();
        String stylesheet = DialogStyler.class.getResource("style.css").toExternalForm();
        if (!pane.getStylesheets().contains(stylesheet)) {
            pane.getStylesheets().add(stylesheet);
        }
        if (!pane.getStyleClass().contains("smart-dialog")) {
            pane.getStyleClass().add("smart-dialog");
        }
    }
}
