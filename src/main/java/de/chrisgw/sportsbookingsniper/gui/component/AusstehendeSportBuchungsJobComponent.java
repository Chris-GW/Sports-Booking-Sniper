package de.chrisgw.sportsbookingsniper.gui.component;

import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.input.KeyType;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsJob;
import de.chrisgw.sportsbookingsniper.buchung.SportBuchungsSniperService;
import de.chrisgw.sportsbookingsniper.gui.state.ApplicationStateDao;
import de.chrisgw.sportsbookingsniper.gui.state.SportBuchungsJobListener;


public class AusstehendeSportBuchungsJobComponent extends BasicPanelComponent implements SportBuchungsJobListener {

    private SportBuchungsJobTable sportBuchungsJobTable = new SportBuchungsJobTable();
    private SportBuchungsSniperService sniperService;


    public AusstehendeSportBuchungsJobComponent(ApplicationStateDao applicationStateDao,
            SportBuchungsSniperService sniperService, Window window) {
        super(applicationStateDao, window, "Ausstehende SportBuchungen", KeyType.F2);
        this.sniperService = sniperService;
        setLayoutManager(new LinearLayout(Direction.VERTICAL));

        sportBuchungsJobTable.setSelectAction(showSportBuchungsJobContextMenu());
        addComponent(sportBuchungsJobTable);
        applicationStateDao.getPendingBuchungsJobs().forEach(this::addSportBuchungsJob);
    }


    private Runnable showSportBuchungsJobContextMenu() {
        return () -> {
            SportBuchungsJob buchungsJob = sportBuchungsJobTable.getSelectedSportBuchungsJob();
            ActionListDialogBuilder buchungsJobactionListDialogBuilder = new ActionListDialogBuilder() //
                    .setTitle("Ausgewählter Sport Buchungs Job ...")
                    .setDescription(buchungsJob.getName())
                    .setCanCancel(true)
                    .setCloseAutomaticallyOnAction(true)
                    .addAction("Bearbeiten <???>", () -> {
                        System.out.println("Bearbeiten: " + buchungsJob);
                    })
                    .addAction("Pausieren <Pause>", () -> {
                        System.out.println("Pausieren: " + buchungsJob);
                        sniperService.cancelSportBuchungsJob(buchungsJob);
                    })
                    .addAction("Erneut Versuchen <???>", () -> {
                        System.out.println("Erneut Versuchen: " + buchungsJob);
                    })
                    .addAction("Buchung kopieren <S-c>", () -> {
                        System.out.println("Buchung kopieren <S-c>" + buchungsJob);
                    })
                    .addAction("Löschen <Entf>", () -> {
                        System.out.println("Buchung löschen <Entf>" + buchungsJob);
                    });
            if (buchungsJob.isPausiert()) {
                buchungsJobactionListDialogBuilder.addAction("Fortsetzen <Pause>", () -> {
                    System.out.println("Fortsetzen: " + buchungsJob);
                    buchungsJob.setPausiert(false);
                    sniperService.submitSportBuchungsJob(buchungsJob);
                });
            } else {
                buchungsJobactionListDialogBuilder.addAction("Pausieren <Pause>", () -> {
                    System.out.println("Pausieren: " + buchungsJob);
                    buchungsJob.setPausiert(true);
                    sniperService.cancelSportBuchungsJob(buchungsJob);
                });
            }
            buchungsJobactionListDialogBuilder.build().showDialog(getTextGUI());
        };
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
        applicationStateDao.addSportBuchungsJobListener(this);
    }

    @Override
    public synchronized void onRemoved(Container container) {
        super.onRemoved(container);
        applicationStateDao.removeSportBuchungsJobListener(this);
    }


    @Override
    protected AusstehendeSportBuchungsJobComponent self() {
        return this;
    }

}
