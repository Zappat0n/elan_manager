package links;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEvents;
import main.ApplicationLoader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MultipleLinkManager {
/*
    private Boolean checkTempIds(Statement st, ResultSet rs) throws SQLException {
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
                paintValue(event_id, event_sub, student);
                if ((newValue == 1 || newValue == 2 || newValue == 3) && ApplicationLoader.linkManager.recordLinksForPresentation(
                        st, event_id, event_sub, event_type, date, student, id))
                    result = true;
            }
        }
        st.execute("DELETE FROM tempIds WHERE teacher = " + ApplicationLoader.settingsManager.teacher + ";");
        return result;
    }
*/
}
