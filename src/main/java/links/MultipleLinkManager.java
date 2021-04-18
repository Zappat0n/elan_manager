package links;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEvents;
import main.ApplicationLoader;
import ui.formClassroom.ClassroomForm;
import utils.MyLogger;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MultipleLinkManager {
    private static final String TAG = MultipleLinkManager.class.getSimpleName();
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

    public void checkForLinksInInsertedIds(Statement st, ResultSet rs) throws SQLException {
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
                if ((newValue == 1 || newValue == 2 || newValue == 3))
                    linkManager.recordLinksForPresentation(st, event_id, event_sub, event_type, date, student, id);
            }
        }
        st.addBatch("DELETE FROM tempIds WHERE teacher = " + ApplicationLoader.settingsManager.teacher + ";");
        ApplicationLoader.bdManager.executeBatch(st);
    }

    public void checkIfRemovedLinks(Statement st) throws SQLException {
        String sql = "SELECT b.id FROM Events a RIGHT JOIN Events b ON a.id = concat('',b.notes * 1) WHERE (concat('',b.notes * 1) = b.notes AND a.id IS NULL)";
        ResultSet rs = st.executeQuery(sql);
        ArrayList<Integer> ids = new ArrayList<>();
        while (rs.next()) {
            ids.add(rs.getInt("id"));
        }
        rs.close();
        ids.forEach((id) -> {
            try {
                st.addBatch("DELETE FROM Events WHERE id = " + id.toString());
            } catch (SQLException e) {
                MyLogger.e(TAG, e);
            }
        });
        ApplicationLoader.bdManager.executeBatch(st);
    }
}
