package bd;

import bd.model.TableEvents;
import bd.model.TableLinks;
import utils.MyLogger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LinkManager {
    private static final String TAG = LinkManager.class.getSimpleName();

    public static void recordLinksForPresentation(JComponent component, BDManager bdManager, Connection co,
                                                  int presentation, Integer presentation_sub, Date date,
                                                  Integer student, Integer eventId) {
        if (component != null) {
            int result = JOptionPane.showConfirmDialog(component, "Record targets linked with presentations?");
            if (result == JOptionPane.CANCEL_OPTION) return;
        }
        PreparedStatement ps = null;
        try {
            if (co == null || co.isClosed()) co = bdManager.connect();
            MySet set = bdManager.getValues(co, BDManager.tableLinks,
                    TableLinks.presentation + "=" + presentation +
                            ((presentation_sub != null)?" AND "+TableLinks.presentation_sub+"="+presentation_sub : ""));

            while (set.next()) {
               Integer outcome = set.getInt(TableLinks.outcomes);
               Integer target = set.getInt(TableLinks.targets);

               if (outcome != null && outcome != 0) {
                   if (!checkExistence(bdManager, co, student, outcome, true)) {
                       bdManager.addEvent(co,date, student, 10, outcome, null, eventId.toString());
                   }

               }
               if (target != null && target != 0) {
                   if (!checkExistence(bdManager, co, student, target, false)) {
                       bdManager.addEvent(co,date, student, 2, outcome, null, eventId.toString());
                   }
               }
            }
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co, ps);
        }
    }

    public static void recordLinksForPresentation(JProgressBar pBar, BDManager bdManager, Connection connection,
                                                     java.sql.Date startdate, java.sql.Date enddate) {
        if (pBar != null) {
            int result = JOptionPane.showConfirmDialog(pBar.getParent(), "Record targets linked with presentations?");
            if (result == JOptionPane.CANCEL_OPTION) return;
        }

        LinksRecorder recorder = new LinksRecorder(bdManager, connection, startdate, enddate, pBar);
        try {
            recorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Boolean checkExistence(BDManager bdManager, Connection co, Integer student, Integer event_id, Boolean isOutcome) {
        String[] even_type = (isOutcome) ? new String[]{"10","11"} : new String[]{"2","5"};
        String condition = TableEvents.student + " = " + student + " AND " + TableEvents.event_id + " = " + event_id +
                " AND (event_type = " + even_type[0] + " OR event_type = " + even_type[1] + ")";
        MySet set = bdManager.getValues(co, BDManager.tableEvents, condition);
        return set.size() > 0;
    }

    static class LinksRecorder extends Thread {
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
                    recordLinksForPresentation(null, bdManager, co, presentation, presentation_sub, date, student, eventId);
                    pBar.setValue(i++);
                }
            } catch (SQLException e) {
                MyLogger.e(TAG, e);
            } finally {
                BDManager.closeQuietly(co, ps);
            }
        }
    }

}
