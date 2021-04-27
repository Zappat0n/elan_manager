package links;

import bd.BDManager;
import bd.EventCondition;
import bd.MySet;
import bd.model.TableEvents;
import main.ApplicationLoader;
import utils.CacheManager;
import utils.MyLogger;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;

public class LinkManager {
    private static final String TAG = LinkManager.class.getSimpleName();
    private Statement statement;
    private int student;
    private int eventType;

    public void recordLinksForPresentation(Statement st, int presentation, int presentation_sub, int eventType,
                                           Date date, int student, int eventId) {
        initialize(student, eventType);
        this.statement = st;
        recordLinks(presentation, presentation_sub, date, eventId);
    }

    private void initialize (int student, int eventType ) {
        this.student = student;
        this.eventType = eventType;
    }

    private void recordLinks(int presentation, int presentation_sub, Date date, int eventId) {
        try {
            String code = presentation + "." + presentation_sub;
            CacheManager.PresentationLinks links = ApplicationLoader.cacheManager.links.get(code);
            if (links == null) return;
            for (int outcome : links.outcomes) {
                int newType = getType(true);
                if (newType != 0 && !checkExistence(outcome, newType)) {
                    addBatch(date, outcome, newType, eventId);
                }
            }
            for (int target : links.targets) {
                int newType = getType(false);
                if (newType != 0 && !checkExistence(target, newType)) {
                    addBatch(date, target, newType, eventId);
                }
            }
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        }
    }

    private void addBatch(Date date, int eventId, int newType, int id) throws SQLException {
        String sql = "INSERT INTO `Events` (`date`,`student`,`event_type`,`event_id`, `notes`, `teacher`) VALUES('"
                + date + "'," + student + "," + newType + "," + eventId + ",'" + id + "'," +
                ApplicationLoader.settingsManager.teacher + ")";
        statement.addBatch(sql);
    }

    private Boolean checkExistence(Integer eventId, int newType) {
        String condition = TableEvents.student + " = " + student + " AND " + TableEvents.event_id + " = " + eventId +
                " AND (event_type = " + newType + ")";
        MySet set = ApplicationLoader.bdManager.getValues(BDManager.tableEvents, condition);
        return set.size() > 0;
    }

    private int getType(Boolean isOutcome) {
        int result;
        switch (eventType) {
            case 1 : result = isOutcome ? 9 : 4; break;
            case 6 : result = isOutcome ? 10 : 2; break;
            case 7 : result = isOutcome ? 11 : 5; break;
            default : result = 0;
        }
        return result;
    }
}
