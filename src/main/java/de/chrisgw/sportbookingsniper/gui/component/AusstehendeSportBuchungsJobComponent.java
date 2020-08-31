package de.chrisgw.sportbookingsniper.gui.component;

import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportbookingsniper.gui.state.SportBuchungJobListener;


public class AusstehendeSportBuchungsJobComponent extends BasicPanelComponent implements SportBuchungJobListener {

    private SportBuchungsJobTable sportBuchungsJobTable = new SportBuchungsJobTable();


    public AusstehendeSportBuchungsJobComponent(ApplicationStateDao applicationStateDao, Window window) {
        super(applicationStateDao, window, "Ausstehende SportBuchungen", KeyType.F2);
        setLayoutManager(new LinearLayout(Direction.VERTICAL));

        addComponent(sportBuchungsJobTable);
        applicationStateDao.getPendingBuchungsJobs().forEach(this::addSportBuchungsJob);
    }


    public void setVisibleRows(int visibleRows) {
        //        sportBuchungsJobTable.setVisibleRows(visibleRows);
    }


    public void addSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        sportBuchungsJobTable.addSportBuchungsJob(sportBuchungsJob);
    }


    public void removeSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        sportBuchungsJobTable.removeSportBuchungsJob(sportBuchungsJob);
    }


    @Override
    public void onNewPendingSportBuchungsJob(SportBuchungsJob newBuchungsJob) {
        addSportBuchungsJob(newBuchungsJob);
    }

    @Override
    public void onUpdatedSportBuchungsJob(SportBuchungsJob sportBuchungsJob) {
        invalidate();
    }

    @Override
    public void onFinishSportBuchungJob(SportBuchungsJob sportBuchungsJob) {
        removeSportBuchungsJob(sportBuchungsJob);
    }


    @Override
    public synchronized void onAdded(Container container) {
        super.onAdded(container);
        applicationStateDao.addSportBuchungJobListener(this);
    }

    @Override
    public synchronized void onRemoved(Container container) {
        super.onRemoved(container);
        applicationStateDao.removeSportBuchungJobListener(this);
    }


    @Override
    protected AusstehendeSportBuchungsJobComponent self() {
        return this;
    }

}
