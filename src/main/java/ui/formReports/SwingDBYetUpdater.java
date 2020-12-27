package ui.formReports;

import bd.BDManager;
import ui.formReports.managers.ReportManager;
import ui.formReports.managers.YetManager;
import utils.CacheManager;
import utils.SettingsManager;

import javax.swing.*;
import java.sql.Date;

public class SwingDBYetUpdater extends SwingWorker {
    private static final String TAG = SwingDBYetUpdater.class.getSimpleName();
    private final JTextArea tALegend;
    private final JTextArea tADoneWell;
    private final JTextArea tAEvenBetter;
    private final JTextArea tATask;
    final Integer classroom;
    final Integer student;

    Integer action = null; // 0 load, 1 save;
    final YetManager yetManager;

    public SwingDBYetUpdater(BDManager bdManager, CacheManager cacheManager, SettingsManager settingsManager, JTextArea tALegend, JTextArea tADoneWell,
                             JTextArea tAEvenBetter, JTextArea tATask, Date date, Integer classroom, Integer student) {
        this.tALegend = tALegend;
        this.tADoneWell = tADoneWell;
        this.tAEvenBetter = tAEvenBetter;
        this.tATask = tATask;
        this.classroom = classroom;
        this.student = student;
        ReportManager reportManager = new ReportManager(cacheManager, date, student);
        yetManager = new YetManager(bdManager, settingsManager, reportManager, tALegend.getText(), tADoneWell.getText(),
                tAEvenBetter.getText(), tATask.getText(), date, classroom, student);
    }

    public void setLoad(){
        action = 0;
    }

    public void setSave(){
        action = 1;
    }

    @Override
    protected Object doInBackground() {
        if (action == null) return null;
        switch (action) {
            case 0 -> {
                clear();
                String[] result = yetManager.load();
                fillTextArea(result);
                ReportsForm.yetChanged = false;
            }
            case 1 -> yetManager.save();
        }
        return null;
    }

    private void clear() {
        tALegend.setText(null);
        tADoneWell.setText(null);
        tAEvenBetter.setText(null);
        tATask.setText(null);
    }

    private void fillTextArea(String[] array){
        tALegend.setText(array[0]);
        tALegend.setLineWrap(true);
        tADoneWell.setText(array[1]);
        tAEvenBetter.setText(array[2]);
        tATask.setText(array[3]);
    }
}
