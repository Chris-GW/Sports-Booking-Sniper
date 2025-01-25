package de.chrisgw.sportsbookingsniper.gui.dialog;

import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

public class ExitApplicationDialog {


    public static MessageDialog build() {
        return new MessageDialogBuilder() //
                .setTitle("SportBookingSniper wirklich beenden?")
                .setText("Wenn Sie den SportBookingSniper beenden, können keine\n"
                        + "Buchungsversuche im Hintergrund ausgeführt werden.\n"
                        + "Wollen Sie den SportBookingSniper wirklich beenden?")
                .addButton(MessageDialogButton.Cancel)
                .addButton(MessageDialogButton.Yes)
                .build();
    }

}
