package de.chrisgw.sportbooking.gui.dialog;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;

import java.util.Arrays;


public class WelcomeDialog extends DialogWindow {

    public WelcomeDialog() {
        super("Willkommen zum \"Buchungsbot - RWTH Hochschulsport\"");
        setHints(Arrays.asList(Hint.MODAL, Hint.CENTERED, Hint.FIT_TERMINAL_WINDOW));

        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new GridLayout(1).setHorizontalSpacing(1));
        buttonPanel.addComponent(new Button("Weiter", this::close));

        Panel mainPanel = new Panel(new GridLayout(1).setLeftMarginSize(1).setRightMarginSize(1));
        String text = "Mithilfe dies Programmes können Sie RWTH Hochschulsport "
                + "automatisch Buchen lassen. Hierfür müssen Sie das Program "
                + "auch weiterhin  im Hintergrund laufen lassen. Zu aller erst "
                + "werden Ihre  Anmeldeinformationen benötigt, mit denen Sie "
                + "sich für die  Sportangebote anmelden.";
        mainPanel.addComponent(new Label(text).setPreferredSize(new TerminalSize(60, 5)).setLabelWidth(60));
        mainPanel.addComponent(new EmptySpace(TerminalSize.ONE));
        buttonPanel.setLayoutData(
                GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, false, false))
                .addTo(mainPanel);
        setComponent(mainPanel);
    }

}
