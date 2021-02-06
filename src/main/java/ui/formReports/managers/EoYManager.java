package ui.formReports.managers;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEventsEoY;
import main.ApplicationLoader;
import utils.MyLogger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import static bd.BDManager.encodeString;

public class EoYManager {
    private static final String TAG = EoYManager.class.getSimpleName();
    final ArrayList<JTextArea> textAreas;
    private final Date date;
    private final Integer classroom;
    private final Integer student;
    private final ReportManager reportManager;
    public Integer teacher;

    public EoYManager(ReportManager reportManager, ArrayList<JTextArea> textAreas, Date date, Integer classroom, Integer student) {
        this.reportManager = reportManager;
        this.textAreas = textAreas;
        this.date = date;
        this.classroom = classroom;
        this.student = student;
    }

    public HashMap<Integer, String> load() {
        HashMap<Integer, String> result = new HashMap<>();

        Connection co = null;
        try {
            co = ApplicationLoader.bdManager.connect();
            MySet set = ApplicationLoader.bdManager.getValues(co, BDManager.tableEventsEoY, getCondition());
            while (set.next()) {
                Integer event_id = set.getInt(TableEventsEoY.event_id);
                String notes = set.getString(TableEventsEoY.notes);
                teacher = set.getInt(TableEventsEoY.teacher);
                if (event_id!= null) {
                    if (textAreas != null) textAreas.get(event_id).setText(notes);
                    else result.put(event_id, notes);
                }
            }
        } finally {
            BDManager.closeQuietly(co);
        }
        return result;
    }

    public void save() {
        Connection co = null;
        Statement st = null;
        try {
            co = ApplicationLoader.bdManager.connect();
            st = co.createStatement();
            st.executeUpdate("DELETE FROM " + BDManager.tableEventsEoY.getName() + " WHERE " + getCondition());
            for (int i = 0; i < textAreas.size(); i++) {
                JTextArea tA = textAreas.get(i);
                if (tA != null && !tA.getText().equals("")) st.addBatch(getInsertString(student, i, encodeString(tA.getText())));
            }
            st.executeBatch();
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co, st);
        }
    }


    private String getCondition() {
        return "(" + TableEventsEoY.student + "=" + student + " OR (" + TableEventsEoY.student + "= -1 AND "+
                TableEventsEoY.event_id +"=" + classroom+")) AND " + TableEventsEoY.date + ">= '" +
                reportManager.getInitialEOYDate() + "' AND " + TableEventsEoY.date + "<= '" +
                reportManager.getFinalEOYDate() + "'";
    }

    private String getInsertString(Integer student, Integer event_id, String text) {
        return "INSERT INTO `" + TableEventsEoY.table_name + "` (`date`, `student`, `event_id`, `notes`, " +
                "`teacher`) VALUES('" + date + "'," + student + "," + event_id + "," + (text != null ? "'" +
                text + "'" : "NULL")+ "," + ApplicationLoader.settingsManager.teacher + ");";
    }

}
