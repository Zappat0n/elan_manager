package links;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEvents;
import main.ApplicationLoader;
import utils.MyLogger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

class LinksRecorder extends Thread {
    private static final String TAG = LinksRecorder.class.getSimpleName();
    private final BDManager bdManager;
    private Connection co;
    private final java.sql.Date startDate;
    private final java.sql.Date endDate;
    private final JProgressBar pBar;

    public LinksRecorder(BDManager bdManager, Connection co, java.sql.Date startDate, java.sql.Date endDate,
                         JProgressBar pBar) {
        this.bdManager = bdManager;
        this.co = co;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pBar = pBar;
    }

    @Override
    public void run() {
        try {
            String condition = TableEvents.date + " >= '" + startDate.toString() + "' AND " + TableEvents.date + " <= '"
                    + endDate.toString() + "' AND (event_type = 6 OR event_type = 7)";
            if (co == null || co.isClosed()) co = bdManager.connect();
            MySet set = bdManager.getValues(co, BDManager.tableEvents, condition);
            pBar.setMinimum(1);
            pBar.setMaximum(set.size());
            int i = 0;
            while (set.next()) {
                //JComponent component, BDManager bdManager, Connection co, int presentation, Integer presentation_sub, Date date, Integer student, Integer eventId
                Integer presentation = set.getInt(TableEvents.event_id);
                Integer presentationSub = set.getInt(TableEvents.event_sub);
                Date date = set.getDate(TableEvents.date);
                Integer student = set.getInt(TableEvents.student);
                Integer eventId = set.getInt(TableEvents.id);
                Integer eventType = set.getInt(TableEvents.event_type);
                ApplicationLoader.linkManager.recordLinksForPresentation(co, presentation, presentationSub, eventType, date, student, eventId);
                pBar.setValue(i++);
            }
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co, null);
        }
    }
}
