package de.chrisgw.sportsbookingsniper.gui.buchung;

import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import de.chrisgw.sportsbookingsniper.angebot.SportAngebot;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportsbookingsniper.gui.state.SportBuchungsJobListener;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill;
import static com.googlecode.lanterna.gui2.LinearLayout.GrowPolicy.CanGrow;


public class AusstehendeSportBuchungsJobPanel extends Panel implements SportBuchungsJobListener {

    private final ApplicationStateDao applicationStateDao;
    private final Map<SportAngebot, Panel> sportAngebotPanelMap = new ConcurrentHashMap<>();
    private final Map<Integer, AusstehendeSportBuchungsJobItem> buchungsJobComponentMap = new ConcurrentHashMap<>();


    public AusstehendeSportBuchungsJobPanel(ApplicationStateDao applicationStateDao) {
        this.applicationStateDao = applicationStateDao;

        Map<SportAngebot, List<SportBuchungsJob>> sportAngebotListMap = applicationStateDao.getPendingBuchungsJobs()
                .stream()
                .collect(Collectors.groupingBy(SportBuchungsJob::getSportAngebot));
        sportAngebotListMap.forEach((sportAngebot, sportBuchungsJobs) -> {
            sportBuchungsJobs.stream()
                    .sorted(Comparator.comparing(SportBuchungsJob::getSportTermin))
                    .map(AusstehendeSportBuchungsJobItem::new)
                    .forEachOrdered(this::addComponent);
        });
    }


    public Panel addComponent(AusstehendeSportBuchungsJobItem component) {
        SportBuchungsJob sportBuchungsJob = component.getSportBuchungsJob();
        buchungsJobComponentMap.put(sportBuchungsJob.getJobId(), component);

        SportAngebot sportAngebot = sportBuchungsJob.getSportAngebot();
        Panel angebotPanel = sportAngebotPanelMap.computeIfAbsent(sportAngebot, sportAngebot1 -> new Panel());
        angebotPanel.addComponent(component, LinearLayout.createLayoutData(Fill, CanGrow));
        if (!containsComponent(angebotPanel)) {
            addComponent(angebotPanel.withBorder(Borders.singleLine(sportAngebot.getName())),
                    LinearLayout.createLayoutData(Fill, CanGrow));
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
