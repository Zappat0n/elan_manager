package ui.formClassroom;

import bd.BDManager;
import bd.model.TableEvents;
import main.ApplicationLoader;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.beans.PropertyEditorSupport;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

public class MyTableModelPlanning extends AbstractTableModel {
    private static final String TAG = MyTableModelPlanning.class.getSimpleName();
    final ClassroomFormData formData;
    private final UtilDateModel dateModel;

    private Connection co;
    private ArrayList[][] data;
    public String[][] events;
    private final JPanel frame;

    public MyTableModelPlanning(Connection co, UtilDateModel dateModel, JPanel frame, ClassroomFormData formData) {
        this.formData = formData;
        this.co = co;
        this.dateModel = dateModel;
        this.frame = frame;
    }

    public void clear() {
        data = null;
        events = null;
    }

    public void resetTable() {
        data = new ArrayList[formData.students.size()][];
        events = new String[formData.students.size()][];
        for (int i = 0; i < data.length; i++) {
            data[i] = new ArrayList[5];
            events[i] = new String[5];
        }
    }

    public void loadData(){
        Calendar c = Calendar.getInstance();
        c.setTime(dateModel.getValue());
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        c.add(Calendar.DATE, -dayOfWeek);
        java.sql.Date ini = new java.sql.Date(c.getTimeInMillis());
        c.add(Calendar.DATE, 7);
        java.sql.Date end = new java.sql.Date(c.getTimeInMillis());
        String query = "SELECT * FROM Events " +
                "INNER JOIN Students ON Events.student = Students.id " +
                "WHERE Students.classroom="+formData.classroom+
                " AND event_type=13 AND Events.date BETWEEN '"+ini+"' AND '"+end+"' "+
                "ORDER BY Events.event_id, Events.event_sub ASC;";
        Statement st = null;
        ResultSet rs = null;

        try {
            if (co == null || co.isClosed()) {
                co = ApplicationLoader.bdManager.connect();
            }
            st = co.createStatement();
            rs = st.executeQuery(query);
            while (rs.next()) {
                Integer student = rs.getInt(TableEvents.student);
                Integer event_id = rs.getInt(TableEvents.event_id);
                Integer event_sub = rs.getInt(TableEvents.event_sub);
                String notes = rs.getString(TableEvents.notes);
                int row = formData.students.indexOf(student);
                int col = MyTableModelPresentations.DayOfWeekToInt(notes)+1;
                setValueAt(getValue(event_id, event_sub), row, col);
                events[row][col-1] = event_id + "." + event_sub;
            }
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
            JOptionPane.showMessageDialog(frame, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        } finally {
            BDManager.closeQuietly(co, st, rs);
        }
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    @Override
    public String getColumnName(int column) {
        String result;
        switch (column) {
            case 0 : result = ""; break;
            case 1 : result = "Monday"; break;
            case 2 : result = "Tuesday"; break;
            case 3 : result = "Wednesday"; break;
            case 4 : result = "Thursday"; break;
            case 5 : result = "Friday"; break;
            default : result = null;
        }
        return result;
    }

    @Override
    public int getRowCount() {
        return formData.students.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) return;

        ArrayList list = data[rowIndex][columnIndex-1];
        if (list == null) {
            list = new ArrayList();
            data[rowIndex][columnIndex-1] = list;
        }
        //noinspection unchecked
        list.add(aValue);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public ArrayList getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            ArrayList<String> list = new ArrayList<>();
            list.add((String)ApplicationLoader.cacheManager.students.get(formData.students.get(rowIndex))[0]);
            return list;
        }
        return data[rowIndex][columnIndex-1];
    }

    public void setValue(Integer event_id, Integer event_sub, int rowIndex, int columnIndex) {
        events[rowIndex][columnIndex-1] = event_id + ((event_sub != null) ? "." + event_sub : ".0");
        setValueAt(getValue(event_id, event_sub), rowIndex, columnIndex);
    }

    public String getValue(Integer event_id, Integer event_sub) {
        return ApplicationLoader.cacheManager.presentations.get(event_id)[ApplicationLoader.settingsManager.language] +
                ((event_sub != null && event_sub != 0) ? " (" +
                        ApplicationLoader.cacheManager.presentationsSub.get(event_sub)[
                                ApplicationLoader.settingsManager.language] + ")" : "");
    }

}
