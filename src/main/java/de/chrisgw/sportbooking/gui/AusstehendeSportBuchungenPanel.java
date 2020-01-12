package de.chrisgw.sportbooking.gui;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbooking.model.SportBuchungsJob;


public class AusstehendeSportBuchungenPanel extends Panel {

    private final ActionListBox pendingJobBox;


    public AusstehendeSportBuchungenPanel() {
        super(new LinearLayout(Direction.VERTICAL));
        pendingJobBox = new ActionListBox();
        addComponent(pendingJobBox);

        Button addNewBtn = new Button("new");
        addNewBtn.addListener(button -> {

        });
        addComponent(addNewBtn);
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        return key.isCtrlDown() && key.getKeyType() == KeyType.Insert;
    }

    public synchronized void addPendingSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        pendingJobBox.addItem(sportBuchungsJob.toString(), () -> {
            System.out.println("clicked on " + sportBuchungsJob);
        });
    }

}
