package ui.formReports;

import ui.formReports.managers.EoYManager;
import ui.formReports.managers.ReportManager;

import javax.swing.*;
import java.sql.Date;
import java.util.ArrayList;

public class SwingDBEOYUpdater extends SwingWorker<Object, Object> {
    private static final String TAG = SwingDBEOYUpdater.class.getSimpleName();
    private final ArrayList<JTextArea> textAreas;

    final Integer classroom;
    final Integer student;

    Integer action = null; // 0 load, 1 save;
    final EoYManager eoyManager;

    public SwingDBEOYUpdater(ArrayList<JTextArea> textAreas, Date date, Integer classroom,
                             Integer student) {
        this.textAreas = textAreas;
        this.classroom = classroom;
        this.student = student;
        ReportManager reportManager = new ReportManager(date, student);
        eoyManager = new EoYManager(reportManager, textAreas, date, classroom, student);
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
            case 0 : {
                clear();
                eoyManager.load();
                ReportsForm.yetChanged = false;
                break;
            }
            case 1 : eoyManager.save();
        }
        return null;
    }

    private void clear() {
        textAreas.forEach(e -> e.setText(null));
    }

}
