package ui.formReports.managers;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEvents;
import bd.model.TableEventsYet;
import utils.MyLogger;
import utils.SettingsManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import static bd.BDManager.encodeString;

public class YetManager implements IManager{
    private static final String TAG = YetManager.class.getSimpleName();
    private final BDManager bdManager;
    private final SettingsManager settingsManager;
    private final ReportManager reportManager;
    private final Date date;
    final Integer classroom;
    final Integer student;
    final String legend;
    final String doneWell;
    final String evenBetter;
    final String task;
    final int month;
    final int year;

    public YetManager(BDManager bdManager, SettingsManager settingsManager, ReportManager reportManager, String legend,
                      String doneWell, String evenBetter, String task, Date date, Integer classroom, Integer student) {
        this.bdManager = bdManager;
        this.settingsManager = settingsManager;
        this.reportManager = reportManager;
        this.legend = legend;
        this.doneWell = doneWell;
        this.evenBetter = evenBetter;
        this.task = task;
        this.date = date;
        this.classroom = classroom;
        this.student = student;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);
    }

    public String[] load(){
        Connection co = null;
        String[] result = new String[4];
        try {
            co = bdManager.connect();
            MySet set = bdManager.getValues(co, BDManager.tableEventsYet, getCondition());
            while (set.next()) {
                Integer event_id = set.getInt(TableEvents.event_id);
                Integer student = set.getInt(TableEvents.student);
                String notes = set.getString(TableEvents.notes);
                fillData(result, student, event_id, notes);
            }
        } finally {
            BDManager.closeQuietly(co);
        }
        return result;
    }

    public void save(){
        Connection co = null;
        Statement st = null;
        try {
            co = bdManager.connect();
            st = co.createStatement();
            st.executeUpdate("DELETE FROM " + BDManager.tableEventsYet.getName() + " WHERE " + getCondition());
            st.addBatch(getInsertString(-1, classroom, encodeString(legend)));
            st.addBatch(getInsertString(student, 1, encodeString(doneWell)));
            st.addBatch(getInsertString(student, 2, encodeString(evenBetter)));
            st.addBatch(getInsertString(student, 3, encodeString(task)));
            st.executeBatch();
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co, st);
        }
    }

    @Override
    public void setDate(Date date) {

    }

    private void fillData(String[] array, Integer student, Integer id, String text){
        if (student == -1) {
            if (id == classroom) {
                array[0] = text;
            }
            return;
        }
        array[id] = text;
    }

    private String getCondition() {
        return "(" + TableEventsYet.student + "=" + student + " OR (" + TableEventsYet.student + "= -1 AND "+
                TableEventsYet.event_id +"=" + classroom+")) AND " + TableEventsYet.date + "> '" + reportManager.getInitialDate() +
                "' AND " + TableEventsYet.date + "< '" + reportManager.getFinalDate() + "'";
    }

    private String getInsertString(Integer student, Integer event_id, String text) {
        String tableName = TableEventsYet.table_name;
        return "INSERT INTO `" + tableName + "` (`date`, `student`, `event_id`, `notes`, `teacher`) VALUES('" +
                date + "'," + student + "," + event_id + "," + (text != null ? "'" +
                text + "'" : "NULL")+ "," + settingsManager.teacher + ");";
    }
}
