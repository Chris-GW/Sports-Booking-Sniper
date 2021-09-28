package de.chrisgw.sportsbookingsniper.gui.buchung;

import com.googlecode.lanterna.gui2.*;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportsbookingsniper.gui.state.SportBuchungsJobListener;

import java.util.Map;
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

        Button newSportBuchungBtn = new Button("new SportBuchung");
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

        SportAngebot sportAngebot = sportBuchungsJob.getSportAngebot();
        Panel angebotPanel = sportAngebotPanelMap.computeIfAbsent(sportAngebot, sportAngebot1 -> new Panel());
        angebotPanel.addComponent(component, createLayoutData(Fill, CanGrow));
        if (!containsComponent(angebotPanel)) {
            addComponent(angebotPanel.withBorder(Borders.singleLine(sportAngebot.getName())),
                    createLayoutData(Fill, CanGrow));
        }
        return self();
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


    @Override
    protected void onBeforeDrawing() {
        super.onBeforeDrawing();
        noContentPanel.setVisible(getChildCount() <= 1);
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
            ausstehendeSportBuchungsJobItem.onUpdatedSportBuchungsJob(sportBuchungsJob);
        }
    }

    @Override
    public void onFinishSportBuchungJob(SportBuchungsJob sportBuchungsJob) {
        var ausstehendeSportBuchungsJobItem = buchungsJobComponentMap.get(sportBuchungsJob.getJobId());
        if (ausstehendeSportBuchungsJobItem != null) {
            ausstehendeSportBuchungsJobItem.onFinishSportBuchungJob(sportBuchungsJob);
        }
        invalidate();
    }

}
