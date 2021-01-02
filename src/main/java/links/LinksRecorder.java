package links;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEvents;
import utils.MyLogger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class LinksRecorder extends Thread {
    private static final String TAG = LinksRecorder.class.getSimpleName();
    final BDManager bdManager;
    Connection co;
    final java.sql.Date startdate;
    final java.sql.Date enddate;
    final JProgressBar pBar;

    public LinksRecorder(BDManager bdManager, Connection co, java.sql.Date startdate, java.sql.Date enddate,
                         JProgressBar pBar) {
        this.bdManager = bdManager;
        this.co = co;
        this.startdate = startdate;
        this.enddate = enddate;
        this.pBar = pBar;
    }

    @Override
    public void run() {
        PreparedStatement ps = null;
        try {
            String condition = TableEvents.date + " >= '" + startdate.toString() + "' AND " + TableEvents.date + " <= '"
                    + enddate.toString() + "' AND (event_type = 6 OR event_type = 7)";
            if (co == null || co.isClosed()) co = bdManager.connect();
            MySet set = bdManager.getValues(co, BDManager.tableEvents, condition);
            pBar.setMinimum(1);
            pBar.setMaximum(set.size());
            int i = 0;
            while (set.next()) {
                //JComponent component, BDManager bdManager, Connection co, int presentation, Integer presentation_sub, Date date, Integer student, Integer eventId
                Integer presentation = set.getInt(TableEvents.event_id);
                Integer presentation_sub = set.getInt(TableEvents.event_sub);
                Date date = set.getDate(TableEvents.date);
                Integer student = set.getInt(TableEvents.student);
                Integer eventId = set.getInt(TableEvents.id);
                LinkManager.recordLinksForPresentation(null, bdManager, co, presentation, presentation_sub, date, student, eventId);
                pBar.setValue(i++);
            }
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co, null);
        }
    }
}
