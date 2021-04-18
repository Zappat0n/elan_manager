package links;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEvents;
import main.ApplicationLoader;
import utils.MyLogger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;

class LinksRecorder extends Thread {
    private static final String TAG = LinksRecorder.class.getSimpleName();
    private final BDManager bdManager;
    private final java.sql.Date startDate;
    private final java.sql.Date endDate;
    private final JProgressBar pBar;
    private Statement st;
    private final LinkManager linkManager;

    public LinksRecorder(BDManager bdManager, Connection co, java.sql.Date startDate, java.sql.Date endDate,
                         JProgressBar pBar) {
        this.bdManager = bdManager;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pBar = pBar;
        linkManager = new LinkManager();
    }

    @Override
    public void run() {
        try {
            String condition = TableEvents.date + " >= '" + startDate.toString() + "' AND " + TableEvents.date + " <= '"
                    + endDate.toString() + "' AND (event_type = 6 OR event_type = 7)";
            MySet set = bdManager.getValues(BDManager.tableEvents, condition);
            st = ApplicationLoader.bdManager.prepareBatch();
            pBar.setMinimum(1);
            pBar.setMaximum(set.size());
            int i = 0;
            while (set.next()) {
                //JComponent component, BDManager bdManager, Connection co, int presentation, Integer presentation_sub, Date date, Integer student, Integer eventId
                Integer presentation = set.getInt(TableEvents.event_id);
                Integer presentationSub = set.getInt(TableEvents.event_sub);
                Date date = set.getDate(TableEvents.date);
                int student = set.getInt(TableEvents.student);
                int eventId = set.getInt(TableEvents.id);
                int eventType = set.getInt(TableEvents.event_type);
                linkManager.recordLinksForPresentation(presentation, presentationSub, eventType, date, student, eventId);
                pBar.setValue(i++);
            }
            ApplicationLoader.bdManager.executeBatch(st);
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(st);
        }
    }
}
