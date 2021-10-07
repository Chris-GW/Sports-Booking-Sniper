package de.chrisgw.sportsbookingsniper.gui.buchung;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.angebot.SportTermin;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportsbookingsniper.gui.state.SportBuchungsJobListener;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Center;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill;
import static com.googlecode.lanterna.gui2.LinearLayout.GrowPolicy.CanGrow;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;


public class AusstehendeSportBuchungsJobPanel extends Panel implements SportBuchungsJobListener {

    private final ApplicationStateDao applicationStateDao;
    private final Map<SportAngebot, Panel> sportAngebotPanelMap = new ConcurrentHashMap<>();
    private final Map<Integer, AusstehendeSportBuchungsJobItem> buchungsJobComponentMap = new ConcurrentHashMap<>();
    private final Panel noContentPanel = createNoContentPanel();


    public AusstehendeSportBuchungsJobPanel(ApplicationStateDao applicationStateDao) {
        this.applicationStateDao = applicationStateDao;
        addComponent(noContentPanel);
        this.applicationStateDao.getPendingBuchungsJobs().forEach(this::onNewPendingSportBuchungsJob);
    }

    private Panel createNoContentPanel() {
        Label infoLabel = new Label("Im Moment sind keine Sport Buchungen vorhanden");

        Button newSportBuchungBtn = new Button("new SportBuchung", this::newSportBuchungsJobDialog);
        newSportBuchungBtn.setRenderer(new Button.BorderedButtonRenderer());
        newSportBuchungBtn.setLayoutData(createLayoutData(Center));

        Panel noContentPanel = new Panel();
        noContentPanel.setLayoutData(createLayoutData(Fill, CanGrow));
        noContentPanel.addComponent(new EmptySpace());
        noContentPanel.addComponent(infoLabel, createLayoutData(Center));
        noContentPanel.addComponent(newSportBuchungBtn);
        return noContentPanel;
    }


    public Panel addComponent(AusstehendeSportBuchungsJobItem component) {
        SportBuchungsJob sportBuchungsJob = component.getSportBuchungsJob();
        buchungsJobComponentMap.put(sportBuchungsJob.getJobId(), component);
        component.addListener(button -> showBuchungsJobActionListDialog(sportBuchungsJob));

        SportAngebot sportAngebot = sportBuchungsJob.getSportAngebot();
        Panel angebotPanel = sportAngebotPanelMap.computeIfAbsent(sportAngebot, sportAngebot1 -> new Panel());
        angebotPanel.addComponent(component, createLayoutData(Fill, CanGrow));
        if (!containsComponent(angebotPanel)) {
            Border border = Borders.singleLine(sportAngebot.getName());
            addComponent(angebotPanel.withBorder(border), createLayoutData(Fill, CanGrow));
            setVisibleNoContentPanel();
        }
        return self();
    }

    public Panel removeComponent(AusstehendeSportBuchungsJobItem component) {
        super.removeComponent(component);
        SportBuchungsJob sportBuchungsJob = component.getSportBuchungsJob();
        Panel angebotPanel = sportAngebotPanelMap.get(sportBuchungsJob.getSportAngebot());
        var ausstehendeSportBuchungsJobItem = buchungsJobComponentMap.get(sportBuchungsJob.getJobId());
        if (ausstehendeSportBuchungsJobItem == null || angebotPanel == null) {
            return self();
        }
        buchungsJobComponentMap.remove(sportBuchungsJob.getJobId());
        angebotPanel.removeComponent(ausstehendeSportBuchungsJobItem);
        getBasePane().setFocusedInteractable(nextFocus(null));

        if (angebotPanel.getChildCount() == 0) {
            sportAngebotPanelMap.remove(sportBuchungsJob.getSportAngebot());
            removeComponent(angebotPanel.getParent());
            setVisibleNoContentPanel();
        }
        return this;
    }

    private void setVisibleNoContentPanel() {
        boolean visible = getChildCount() <= 1;
        noContentPanel.setVisible(visible);
        if (visible) {
            addComponent(0, noContentPanel);
        } else {
            removeComponent(noContentPanel);
        }
    }


    private void showBuchungsJobActionListDialog(SportBuchungsJob sportBuchungsJob) {
        SportAngebot sportAngebot = sportBuchungsJob.getSportAngebot();
        SportTermin sportTermin = sportBuchungsJob.getSportTermin();
        new ActionListDialogBuilder() //
                .setTitle("Ausgewählter Sport Buchungs Job ...")
                .setDescription(sportAngebot.getName() + "\n" + sportTermin)
                .setCanCancel(true)
                .setCloseAutomaticallyOnAction(true)
                .addAction("Bearbeiten <c-b>", () -> editSportBuchungsJobDialog(sportBuchungsJob))
                .addAction("Jetzt Versuchen zu buchen <F5>", () -> retrySportBuchungsJob(sportBuchungsJob))
                .addAction("Buchung kopieren <S-c>", () -> copySportBuchungsJob(sportBuchungsJob))
                .addAction("Löschen <Entf>", () -> removeSportBuchungsJob(sportBuchungsJob))
                .build()
                .showDialog((WindowBasedTextGUI) getTextGUI());
    }

    private void newSportBuchungsJobDialog() {
        Optional<SportBuchungsJob> savedSportBuchungsJob = new SportBuchungDialog(applicationStateDao) //
                .showDialog((WindowBasedTextGUI) getTextGUI());
        savedSportBuchungsJob.ifPresent(applicationStateDao::addSportBuchungsJob);
    }

    private void editSportBuchungsJobDialog(SportBuchungsJob sportBuchungsJob) {
        Optional<SportBuchungsJob> savedSportBuchungsJob = new SportBuchungDialog(applicationStateDao) //
                .setSportBuchungsJob(sportBuchungsJob) //
                .showDialog((WindowBasedTextGUI) getTextGUI());
        savedSportBuchungsJob.ifPresent(applicationStateDao::refreshSportBuchungsJob);
    }

    private void retrySportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        applicationStateDao.retrySportBuchungsJob(sportBuchungsJob);
    }

    private void copySportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        Optional<SportBuchungsJob> savedSportBuchungsJob = new SportBuchungDialog(applicationStateDao) //
                .setSportBuchungsJob(sportBuchungsJob) //
                .showDialog((WindowBasedTextGUI) getTextGUI());
        savedSportBuchungsJob.ifPresent(applicationStateDao::addSportBuchungsJob);
    }

    private void removeSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        applicationStateDao.removeSportBuchungsJob(sportBuchungsJob);
    }


    @Override
    public void onNewPendingSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        var ausstehendeSportBuchungsJobItem = new AusstehendeSportBuchungsJobItem(sportBuchungsJob);
        addComponent(ausstehendeSportBuchungsJobItem);
    }

    @Override
    public void onUpdatedSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        var ausstehendeSportBuchungsJobItem = buchungsJobComponentMap.get(sportBuchungsJob.getJobId());
        if (ausstehendeSportBuchungsJobItem != null) {
            ausstehendeSportBuchungsJobItem.invalidate();
        }
    }

    @Override
    public void onFinishSportBuchungJob(SportBuchungsJob sportBuchungsJob) {
        var ausstehendeSportBuchungsJobItem = buchungsJobComponentMap.get(sportBuchungsJob.getJobId());
        if (ausstehendeSportBuchungsJobItem != null) {
            removeComponent(ausstehendeSportBuchungsJobItem);
        }
    }


    @Override
    public synchronized void onAdded(Container container) {
        super.onAdded(container);
        this.applicationStateDao.addSportBuchungsJobListener(this);
    }

    @Override
    public synchronized void onRemoved(Container container) {
        super.onRemoved(container);
        this.applicationStateDao.removeSportBuchungsJobListener(this);
    }

}
