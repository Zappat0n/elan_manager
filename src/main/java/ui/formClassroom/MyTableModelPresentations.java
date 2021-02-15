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
import java.sql.*;
import java.util.Calendar;

public class MyTableModelPresentations extends AbstractTableModel {
    private static final String TAG = MyTableModelPresentations.class.getSimpleName();
    private final UtilDateModel dateModel;
    final ClassroomFormData formData;
    Connection co;
    public int[][] data;
    private final JPanel frame;


    public MyTableModelPresentations(Connection co, JPanel frame, UtilDateModel dateModel, ClassroomFormData formData) {
        this.formData = formData;
        this.frame = frame;
        this.co = co;
        this.dateModel = dateModel;
    }

    public void clear(){
        data = null;
    }

    public void loadData() {
        data = new int[formData.presentations.size()][];
        for (int i = 0; i < data.length; i++) {
            data[i] = new int[formData.students.size()];
            for (int j = 0; j < formData.students.size(); j++) data[i][j]=0;
        }

        String query = "SELECT * FROM Events " +
                "INNER JOIN Students ON Events.student = Students.id " +
                "WHERE Students.classroom="+formData.classroom+
                " AND (event_type=1 OR event_type=6 OR event_type=7 OR event_type=13) "+
                "ORDER BY Events.event_id, Events.event_sub ASC;";
        Statement st = null;
        ResultSet rs = null;
        try {
            if (co == null || co.isClosed()) {
                this.co = ApplicationLoader.bdManager.connect();
            }
            st = co.createStatement();
            rs = st.executeQuery(query);
            while (rs.next()) {
                Integer student = rs.getInt(TableEvents.student);
                Integer event_id = rs.getInt(TableEvents.event_id);
                int event_sub = rs.getInt(TableEvents.event_sub);
                String id = event_id + "." + event_sub;
                int row = formData.presentations.indexOf(id);
                int col = formData.students.indexOf(student);
                if (row != -1 && col !=-1) {
                    int newvalue = getEvent_typeValue(rs.getInt(TableEvents.event_type), rs.getString(TableEvents.notes),
                            rs.getDate(TableEvents.date), dateModel.getValue());
                    if (newvalue > data[row][col]) {
                        data[row][col] = newvalue;
                    }
                }
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

    public static int getEvent_typeValue(Integer event_type, String notes, Date date, java.util.Date basedate) {
        switch (event_type) {
            case 1: return 1;
            case 6: return 2;
            case 7: return 3;
            case 13: {
                if (notes == null || !isThisWeek(date, basedate)) return 0;
                return DayOfWeekToInt(notes)+4;
            }
            default:return 0;
        }
    }

    public static int DayOfWeekToInt(String day) {
        if (day == null || day.contains("F")) return 4;
        else if (day.contains("M")) return 0;
        else if (day.contains("W")) return 2;
        else if (day.contains("Th")) return 3;
        else if (day.contains("T")) return 1;
        else return 4;
    }

    public static Boolean isThisWeek(Date date, java.util.Date secondate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        int year = cal.get(Calendar.YEAR);
        cal.setTime(secondate);
        int thisweek = cal.get(Calendar.WEEK_OF_YEAR);
        int thisyear = cal.get(Calendar.YEAR);
        return week == thisweek && year == thisyear;
    }

    public int getRowCount() {
        return formData.presentations.size();
    }

    public int getColumnCount() {
        if (formData == null) return 0;
        if (formData.classroom == null) return 0;
        else return ApplicationLoader.cacheManager.studentsPerClassroom.get(formData.classroom).size()+1;
    }

    public String getColumnName(int col) {
        if (col == 0) return "";
        else return (String)ApplicationLoader.cacheManager.students.get(formData.students.get(col-1))[0];
    }

    public boolean isCellEditable(int row, int col) { return false; }

    public Class getColumnClass(int c) {
        return String.class;
    }

    public String getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            String[] presentation = formData.presentations.get(rowIndex).split("[.]");
            int sub = Integer.parseInt(presentation[1]);
            if (sub == 0) return (String)ApplicationLoader.cacheManager.presentations.get(
                    Integer.valueOf(presentation[0]))[ApplicationLoader.settingsManager.language];
            else return "->" + ApplicationLoader.cacheManager.presentationsSub.get(sub)[ApplicationLoader.settingsManager.language];
        }

        String result;
        switch (data[rowIndex][columnIndex - 1]) {
            case 0 : result = ""; break;
            case 1 : result = "/"; break;
            case 2 : result = "Λ"; break;
            case 3 : result = "Δ"; break;
            case 4 : result = "M"; break;
            case 5 : result = "T"; break;
            case 6 : result = "W"; break;
            case 7 : result = "Th"; break;
            case 8 : result = "F"; break;
            default : result = null;
        }
        return result;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        data[rowIndex][columnIndex-1] = (int)aValue;
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}
