package planning;

import bd.model.TableEvents;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

public class PlanningManager {

    public void addBatchToRemovePlanning(Statement st, Date date, Integer student, Integer event_id, Integer event_sub,
                                         String planning_value) throws SQLException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        java.sql.Date monday = new java.sql.Date(cal.getTime().getTime());
        String sql = "DELETE FROM Events WHERE " + TableEvents.student + "=" + student + " AND " +
                TableEvents.event_id + "=" + event_id + " AND " + (event_sub!=null?TableEvents.event_sub + "=" +
                event_sub:TableEvents.event_sub + "IS NULL") + " AND " + TableEvents.date + " >= '" + monday + "' AND "
                + TableEvents.event_type + " = 13 AND INSTR(notes, '" + planning_value + "')";
        st.addBatch(sql);
    }
}
