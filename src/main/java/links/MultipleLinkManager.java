package links;

import bd.BDManager;
import bd.EventCondition;
import bd.MySet;
import bd.model.TableEvents;
import main.ApplicationLoader;
import ui.formClassroom.ClassroomForm;
import utils.CacheManager;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

public class MultipleLinkManager {
    private final ClassroomForm form;
    private final LinkManager linkManager;
    Date date;
    int newValue;


    public MultipleLinkManager(ClassroomForm form, Date date, int newValue) {
        this.form = form;
        this.date = date;
        this.newValue = newValue;
        linkManager = new LinkManager();
    }

    public Boolean checkForLinksInInsertedIds(Statement st, ResultSet rs) throws SQLException {
        boolean result = false;
        StringBuilder sql = new StringBuilder("INSERT INTO tempIds VALUES ");
        do {
            sql.append("(").append(rs.getInt(1)).append(",").append(ApplicationLoader.settingsManager.teacher).append("),");
        } while (rs.next());
        sql = new StringBuilder(sql.substring(0, sql.length() - 1) + ";");
        st.execute(sql.toString());
        MySet set = new MySet(st.executeQuery(
                "SELECT * FROM Events INNER JOIN tempIds ON id=tempIds.ids WHERE tempIds.teacher = "
                        + ApplicationLoader.settingsManager.teacher + ";"), BDManager.tableEvents, null);
        set.first();
        while (set.next()) {
            Integer id = set.getInt(TableEvents.id);
            Integer event_id = set.getInt(TableEvents.event_id);
            Integer event_sub = set.getInt(TableEvents.event_sub);
            Integer student = set.getInt(TableEvents.student);
            Integer event_type = set.getInt(TableEvents.event_type);
            if (event_type != 2 && event_type != 4 && event_type != 5
                    && event_type != 9 && event_type != 10 && event_type != 11) {
                form.paintValue(event_id, event_sub, student, newValue);
                if ((newValue == 1 || newValue == 2 || newValue == 3) && linkManager.recordLinksForPresentation(
                        st, event_id, event_sub, event_type, date, student, id))
                    result = true;
            }
        }
        st.execute("DELETE FROM tempIds WHERE teacher = " + ApplicationLoader.settingsManager.teacher + ";");
        return result;
    }

    public void checkIfRemovedLinks(Statement st, ArrayList<EventCondition> events) throws SQLException {
        for (EventCondition condition : events) {
            for (Map.Entry<int[], CacheManager.PresentationLinks> entry : ApplicationLoader.cacheManager.links.entrySet()) {
                int sub = (condition.event_sub != null) ? condition.event_sub : 0;
                int[] data = entry.getKey();
                if (data[0] == condition.event_id && data[1] == sub) {
                    for (Integer outcome : entry.getValue().outcomes) {
                        linkManager.deleteBrokenLink(condition, st, 10, outcome);
                    }

                    for (Integer target : entry.getValue().targets) {
                        linkManager.deleteBrokenLink(condition, st, 2, target);
                    }
                }
            }
        }
    }

}
