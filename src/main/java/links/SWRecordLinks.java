package links;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEvents;
import main.ApplicationLoader;
import ui.MainForm;
import utils.MyLogger;

import javax.swing.*;
import java.sql.Date;
import java.sql.Statement;

public class SWRecordLinks extends SwingWorker<Boolean, Integer> {
    private static final String TAG = SWRecordLinks.class.getSimpleName();
    private final java.sql.Date startDate;
    private final java.sql.Date endDate;
    private final JProgressBar pBar;
    private Statement st;
    private final LinkManager linkManager;

    public SWRecordLinks(java.sql.Date startDate, java.sql.Date endDate, JProgressBar pBar) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.pBar = pBar;
        linkManager = new LinkManager();
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        try {
            String condition = TableEvents.date + " >= '" + startDate.toString() + "' AND " + TableEvents.date + " <= '"
                    + endDate.toString() + "' AND (event_type = 6 OR event_type = 7)";
            st = ApplicationLoader.bdManager.prepareBatch();
            MySet set = ApplicationLoader.bdManager.getValues(BDManager.tableEvents, condition);
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
                linkManager.recordLinksForPresentation(st, presentation, presentationSub, eventType, date, student, eventId);
                pBar.setValue(i++);
            }
            ApplicationLoader.bdManager.executeBatch(st);
        } catch (Exception e) {
            MyLogger.e(TAG, e);
            return false;
        } finally {
            BDManager.closeQuietly(st);
        }
        return true;
    }

    @Override
    protected void done() {
        try {
            super.done();
            if (get()) {
                JOptionPane.showMessageDialog(MainForm.frame,
                        "Links generated correctly", "Task finished", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(MainForm.frame,
                        "Error generating Links", "Task finished", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        }
    }
}
