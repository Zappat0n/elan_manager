package links;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEvents;
import main.ApplicationLoader;
import utils.CacheManager;
import utils.MyLogger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;

public class LinkManager {
    private static final String TAG = LinkManager.class.getSimpleName();
    private Connection co;
    private Statement statement;
    private int student;
    private int eventType;

    public void recordLinksForPresentation(Connection co, int presentation, int presentation_sub, int eventType,
                                           Date date, Integer student, Integer eventId) {
        this.co = co;
        this.student = student;
        this.eventType = eventType;
        try {
            if (co == null || co.isClosed()) co = ApplicationLoader.bdManager.connect();
            statement = ApplicationLoader.bdManager.prepareBatch();

            int[] values = {presentation, presentation_sub};
            CacheManager.PresentationLinks links = ApplicationLoader.cacheManager.links.get(values);

            for (int outcome : links.outcomes) {
                int newType = getType(true);
                if (newType != 0 && !checkExistence(outcome, newType)) {
                    addBatch(date, eventId, newType, eventId);
                }
            }
            for (int target : links.targets) {
                int newType = getType(false);
                if (newType != 0 && !checkExistence(target, newType)) {
                    addBatch(date, eventId, newType, eventId);
                }
            }
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co, null);
        }
    }

    private void addBatch(Date date, int eventId, int newType, int id) throws SQLException {
        String sql = "INSERT INTO `Events` (`date`,`student`,`event_type`,`event_id`, `notes`, `teacher`) VALUES('"
                + date + "'," + student + "," + newType + "," + eventId + ",'" + id + "'," +
                ApplicationLoader.settingsManager.teacher + ")";
        statement.addBatch(sql);
    }

    public static void recordLinksForPresentation(JProgressBar pBar, BDManager bdManager, Connection connection,
                                                     java.sql.Date startDate, java.sql.Date endDate) {
        if (pBar != null) {
            int result = JOptionPane.showConfirmDialog(pBar.getParent(), "Record targets linked with presentations?");
            if (result == JOptionPane.CANCEL_OPTION) return;
        }

        LinksRecorder recorder = new LinksRecorder(bdManager, connection, startDate, endDate, pBar);
        try {
            recorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Boolean checkExistence(Integer eventId, int newType) {
        String condition = TableEvents.student + " = " + student + " AND " + TableEvents.event_id + " = " + eventId +
                " AND (event_type = " + newType + ")";
        MySet set = ApplicationLoader.bdManager.getValues(co, BDManager.tableEvents, condition);
        return set.size() > 0;
    }

    private int getType(Boolean isOutcome) {
        return switch (eventType) {
            case 1 -> isOutcome ? 9 : 4;
            case 6 -> isOutcome ? 10 : 2;
            case 7 -> isOutcome ? 11 : 5;
            default -> 0;
        };
    }
}
