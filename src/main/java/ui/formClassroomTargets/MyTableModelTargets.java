package ui.formClassroomTargets;

import bd.BDManager;
import bd.model.TableEvents;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import utils.data.RawData;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.sql.*;
import java.util.*;

public class MyTableModelTargets extends AbstractTableModel {
    private static final String TAG = MyTableModelTargets.class.getSimpleName();
    final SettingsManager settingsManager;
    final BDManager bdManager;
    final CacheManager cacheManager;
    public int[][] data;
    final JPanel frame;
    public final ArrayList<Integer> targets;
    public final ArrayList<Integer> outcomes;
    public ArrayList<Integer> students;
    Double year;


    public MyTableModelTargets(SettingsManager settingsManager, CacheManager cacheManager, BDManager bdManager, JPanel frame, UtilDateModel dateModel) {
        this.settingsManager = settingsManager;
        this.cacheManager = cacheManager;
        this.bdManager = bdManager;
        this.frame = frame;
        targets = new ArrayList<>();
        outcomes = new ArrayList<>();
        students = new ArrayList<>();
    }

    public void loadData(Integer classroom, Integer stage, Integer area) {
        students = cacheManager.loadStudentsSortedByAge(classroom);//cacheManager.studentsperclassroom.get(classroom);
        year = RawData.yearsperstage[stage];
        ArrayList<Integer> subareas = cacheManager.subareasTargetPerArea.get(area);
        if (subareas != null) {
            targets.clear();
            outcomes.clear();
            LinkedHashMap<Integer, ArrayList<Integer>> outcomesperyear = cacheManager.getOutcomesPerYear(year);

            for (Integer subarea: subareas) {
                if (outcomesperyear != null && outcomesperyear.containsKey(subarea))
                    outcomes.addAll(outcomesperyear.get(subarea));
                ArrayList<Integer> list = cacheManager.targetsPerYearAndSubarea.get(year).get(subarea);
                if (list != null) targets.addAll(list);
            }
        } else return;

        data = new int[outcomes.size() + targets.size()][];
        for (int i = 0; i < data.length; i++) {
            if (i < outcomes.size())
                fireTableCellUpdated(i,0);
            else {
                fireTableCellUpdated(i-outcomes.size(),0);
            }

            data[i] = new int[students.size()];
            for (int j = 0; j < students.size(); j++) data[i][j]=0;
        }

        Statement st = null;
        ResultSet rs = null;
        Connection co = null;
        try {
            co = bdManager.connect();
            st = co.createStatement();
            rs = loadOutcomes(st, classroom);
            BDManager.closeQuietly(rs);
            rs = loadTargets(st, classroom);
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
            JOptionPane.showMessageDialog(frame, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        } finally {
            BDManager.closeQuietly(co, st, rs);
        }
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    private ResultSet loadOutcomes(Statement st, Integer classroom) throws SQLException {
        String query = "SELECT * FROM Events " +
                "INNER JOIN Students ON Events.student = Students.id " +
                "WHERE Students.classroom=" + classroom +
                " AND (event_type = 9 OR event_type = 10 OR event_type = 11) "+
                "ORDER BY Events.event_id ASC;";
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            Integer student = rs.getInt(TableEvents.student);
            Integer event_id = rs.getInt(TableEvents.event_id);
            int row = outcomes.indexOf(event_id);
            int col = students.indexOf(student);
            if (row != -1 && col !=-1) {
                int newvalue = getOutcome_typeValue(rs.getInt(TableEvents.event_type));
                if (newvalue > data[row][col]) data[row][col] = newvalue;
            }
        }
        return rs;
    }


    private ResultSet loadTargets(Statement st, Integer classroom) throws SQLException {
        String query = "SELECT * FROM Events INNER JOIN Students ON Events.student = Students.id " +
                "WHERE Students.classroom=" + classroom +
                " AND (event_type = 2 OR event_type = 4 OR event_type = 5) "+
                "ORDER BY Events.event_id ASC;";
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            Integer student = rs.getInt(TableEvents.student);
            Integer event_id = rs.getInt(TableEvents.event_id);
            if (targets.contains(event_id) && students.contains(student)) {
                int row = outcomes.size() + targets.indexOf(event_id);
                int col = students.indexOf(student);
                int newvalue = getTarget_typeValue(rs.getInt(TableEvents.event_type));
                if (newvalue > data[row][col]) data[row][col] = newvalue;
            }
        }
        return rs;
    }

    public static int getTarget_typeValue(Integer event_type) {
        return switch (event_type) {
            case 2 -> 1;
            case 4 -> 2;
            case 5 -> 3;
            default -> 0;
        };
    }

    public static int getOutcome_typeValue(Integer event_type) {
        return switch (event_type) {
            case 9 -> 1;
            case 10 -> 2;
            case 11 -> 3;
            default -> 0;
        };
    }

    @Override
    public int getRowCount() {
        return outcomes.size() + targets.size();
    }

    @Override
    public int getColumnCount() {
        return students.size()+1;
    }

    @Override
    public String getColumnName(int col) {
        if (col == 0) return null;
        Integer id = students.get(col-1);
        if (id != null) return (String)cacheManager.students.get(id)[0];
        else return null;
    }

    @Override
    public boolean isCellEditable(int row, int col) { return false; }

    @Override
    public Class getColumnClass(int c) {
        if (c == 0) return String[].class;
        else return String.class;
    }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            if (rowIndex < outcomes.size())
                return (String)cacheManager.outcomes.get(outcomes.get(rowIndex))[settingsManager.language];
            else
                return (String)cacheManager.targets.get(targets.get(rowIndex-outcomes.size()))[settingsManager.language];
        }

        return switch (data[rowIndex][columnIndex - 1]) {
            case 0 -> "";
            case 1 -> "/";
            case 2 -> "Λ";
            case 3 -> "Δ";
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        data[rowIndex][columnIndex-1] = (int)aValue;
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}
