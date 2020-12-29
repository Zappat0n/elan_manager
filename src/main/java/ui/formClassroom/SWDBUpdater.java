package ui.formClassroom;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEvents;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import utils.data.RawData;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class SWDBUpdater extends SwingWorker {
    private static final String TAG = SWDBUpdater.class.getSimpleName();
    Connection co;
    final JTable tablePresentations;
    final JTable tablePlanning;
    final BDManager bdManager;
    final SettingsManager settingsManager;
    final CacheManager cacheManager;
    final int[] rows;
    final int[] columns;
    final Integer newvalue;
    final Date date;
    final ArrayList <Condition> check; // To see if we need to add a value after deleting an upper value
    final ArrayList <Condition> checkIfRemovedLinks; //To see if we need to remove a link automatically generated
    final ArrayList <Condition> checkDeletedPlanning; //To see the actual value of the presentation after deleting the planning
    final ArrayList <Link> links;
    final ClassroomFormData formData;

    public SWDBUpdater(BDManager bdManager, SettingsManager settingsManager, CacheManager cacheManager,
                       JTable tablePresentations, JTable tablePlanning, int[] rows, int[] columns, Integer newvalue,
                       Date date, ClassroomFormData formData) {
        this.formData = formData;
        this.bdManager = bdManager;
        this.settingsManager = settingsManager;
        this.cacheManager = cacheManager;
        this.tablePresentations = tablePresentations;
        this.tablePlanning = tablePlanning;
        this.rows = rows;
        this.columns = columns;
        this.date = date;
        this.newvalue = newvalue;
        check = new ArrayList<>();
        links = new ArrayList<>();
        checkIfRemovedLinks = new ArrayList<>();
        checkDeletedPlanning = new ArrayList<>();
    }

    @Override
    protected Object doInBackground() throws Exception {
        Statement st = initBatch();
        for (int value : rows) {
            for (int column : columns) {
                if (column == 0) continue;
                Integer[] ev = getEvent(value);
                Integer student = getStudent(column - 1);
                if (newvalue != null) addToBatch(st, student, ev[0], ev[1], getOldValue(value, column - 1),
                        (String) tablePresentations.getValueAt(value, column));
                else {
                    String text = (String) tablePresentations.getValueAt(value, column);
                    if (text == null || text.equals("")) continue;
                    int col = MyTableModelPresentations.DayOfWeekToInt(text) + 1;
                    int row = formData.students.indexOf(student);
                    removePlanning(st, student, ev[0], ev[1], text);
                    MyTableModelPlanning modelPlanning = (MyTableModelPlanning) tablePlanning.getModel();
                    modelPlanning.getValueAt(row, col).remove(modelPlanning.getValue(ev[0], ev[1]));
                    modelPlanning.fireTableCellUpdated(row, col);
                    checkDeletedPlanning.add(new Condition(ev[0], ev[1], student));
                }
            }
        }
        sendToBatch(st);
        return null;
    }

    protected void removePlanning(Statement st, Integer student, Integer event_id, Integer event_sub, String planning_value) throws SQLException {
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

    protected Statement initBatch() {
        Statement st = null;
        try {
            if (co == null || co.isClosed()) co = bdManager.connect();
            co.setAutoCommit(false);
            st = co.createStatement();
            st.addBatch("DELETE FROM tempIds WHERE teacher = "+settingsManager.teacher+";");
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        }
        return st;
    }

    protected void addToBatch(Statement st, Integer student, Integer event_id, Integer event_sub,
                              Integer oldvalue, String text) throws SQLException {
        if (newvalue < 4) {
            if (newvalue > oldvalue) {
                addBatchEvent(st, student, event_id, event_sub, RawData.montessoriEvent_Types[newvalue-1], null);
            } else {
                if (oldvalue < 4) {
                    removeUpperValues(st, newvalue+1, student, event_id, event_sub, oldvalue);
                    if (newvalue != 0) check.add(new Condition(event_id, event_sub, student));
                    else paintValue(event_id, event_sub, student);
                }
            }
        } else {
            if (!newvalue.equals(oldvalue)) {
                if (oldvalue > 3) removePlanning(st, student, event_id, event_sub, text);
                addBatchEvent(st, student, event_id, event_sub, 13, MyPopUpMenuPresentations.planning_values[newvalue-4]);
            }
        }
    }

    protected void sendToBatch(Statement st){
        Boolean addedLinks = false;
        try {
            st.executeBatch();
            co.commit();
            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) addedLinks = checkTempIds(st, rs);
            if (checkTempEvents(st) || addedLinks) sendToBatch(st);
            if (checkIfRemovedLinks.size() > 0) {
                checkIfRemovedLinks(st);
                co.commit();
            }
            if (checkDeletedPlanning.size() > 0) checkDeletedPlanning(st);
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co, st);
        }
    }

    private void checkDeletedPlanning(Statement st) throws SQLException {
        for (Condition condition : checkDeletedPlanning) {
            MySet set = new MySet(st.executeQuery(BDManager.tableEvents.getValues(getBasicCondition(condition.student,
                    condition.event_id, condition.event_sub))), BDManager.tableEvents, null);
            int value = 0;
            while (set.next()) {
                int newvalue = MyTableModelPresentations.getEvent_typeValue(set.getInt(TableEvents.event_type),
                        set.getString(TableEvents.notes), date, set.getDate(TableEvents.date));
                if (newvalue > value) value = newvalue;
            }
            String id = condition.event_id + "." + (condition.event_sub != null ? condition.event_sub : "0");
            int row = formData.presentations.indexOf(id);
            int col = formData.students.indexOf(condition.student)+1;
            String text = (String) tablePresentations.getValueAt(row, col);
            if (row != -1 && col != -1) tablePresentations.setValueAt(value, row, col);
            row = formData.students.indexOf(condition.student);
            col = MyTableModelPresentations.DayOfWeekToInt(text) + 1;
            tablePlanning.getModel().setValueAt(null, row, col);
        }
        checkDeletedPlanning.clear();
    }


    private void checkIfRemovedLinks(Statement st) throws SQLException {
        for (Condition condition : checkIfRemovedLinks) {
            for (Map.Entry<Integer[], CacheManager.PresentationLinks> entry : cacheManager.links.entrySet()) {
                Integer sub = (condition.event_sub != null) ? condition.event_sub : 0;
                Integer[] data = entry.getKey();
                if (data[0].equals(condition.event_id) && data[1].equals(sub)) {
                    for (Integer outcome : entry.getValue().outcomes) {
                        deleteBrokenLink(condition, st, 10, outcome);
                    }

                    for (Integer target : entry.getValue().targets) {
                        deleteBrokenLink(condition, st, 2, target);
                    }
                }
            }
        }
    }

    private void deleteBrokenLink(Condition condition, Statement st, Integer event_type, int nc) throws SQLException {
        String sql = getCondition(condition.student, event_type, nc, null) +
                " AND notes IS NOT NULL";
        MySet set = new MySet(st.executeQuery(BDManager.tableEvents.getValues(sql)), BDManager.tableEvents, null);
        while (set.next()) {
            Integer id = set.getInt(TableEvents.id);
            String notes = set.getString(TableEvents.notes);
            MySet set2 = new MySet(st.executeQuery(BDManager.tableEvents.getValues("id = " + notes)),
                    BDManager.tableEvents, null);
            if (!set2.next()) st.executeUpdate("DELETE FROM Events WHERE id = " + id);
        }
    }

    private Boolean checkTempIds(Statement st, ResultSet rs) throws SQLException {
        boolean result = false;
        StringBuilder sql = new StringBuilder("INSERT INTO tempIds VALUES ");
         do {
             sql.append("(").append(rs.getInt(1)).append(",").append(settingsManager.teacher).append("),");
         } while (rs.next());
        sql = new StringBuilder(sql.substring(0, sql.length() - 1) + ";");
        st.execute(sql.toString());
        MySet set = new MySet(st.executeQuery(
                "SELECT * FROM Events INNER JOIN tempIds ON id=tempIds.ids WHERE tempIds.teacher = "
                        + settingsManager.teacher + ";"), BDManager.tableEvents, null);
        set.first();
        while (set.next()) {
            Integer id = set.getInt(TableEvents.id);
            Integer event_id = set.getInt(TableEvents.event_id);
            Integer event_sub = set.getInt(TableEvents.event_sub);
            Integer student = set.getInt(TableEvents.student);
            Integer event_type = set.getInt(TableEvents.event_type);
            if (event_type != 2 && event_type != 5 && event_type != 10 && event_type != 11) {
                paintValue(event_id, event_sub, student);
                if ((newvalue == 2 || newvalue == 3) && checkLink(st, student, event_id, event_sub, id)) result = true;
            }
        }
        st.execute("DELETE FROM tempIds WHERE teacher = " + settingsManager.teacher + ";");
        return result;
    }

    private Boolean checkTempEvents(Statement st) throws SQLException {
        boolean result = false;
        for (Condition condition : check) {
            MySet set = new MySet(st.executeQuery(BDManager.tableEvents.getValues(getCondition(condition.student,
                    RawData.montessoriEvent_Types[newvalue-1], condition.event_id, condition.event_sub))),
                    BDManager.tableEvents, null);
            if (set.data.size() == 0) {
                addBatchEvent(st, condition.student, condition.event_id, condition.event_sub,
                        RawData.montessoriEvent_Types[newvalue-1], null);
                result = true;
            } else paintValue(condition.event_id, condition.event_sub, condition.student);
        }
        check.clear();
        return result;
    }

    private void removeUpperValues(Statement st, int start, Integer student, Integer event_id, Integer event_sub,
                                   Integer oldvalue) {
        for (int i = start; i < 4; i++) {
            try {
                String sql = bdManager.getTable(TableEvents.table_name).removeValue(
                        getCondition(student, RawData.montessoriEvent_Types[i-1], event_id, event_sub));
                st.addBatch(sql);
                if (newvalue < 2 && oldvalue > 1) checkIfRemovedLinks.add(new Condition(event_id, event_sub, student));
            } catch (SQLException e) {
                MyLogger.e(TAG, e);
                return;
            }
        }
    }

    private Boolean checkLink(Statement st, Integer student, Integer event_id, Integer event_sub, Integer id) throws SQLException {
        boolean result = false;
        for (Map.Entry<Integer[], CacheManager.PresentationLinks> entry : cacheManager.links.entrySet()) {
            Integer[] data = entry.getKey();
            if (data[0].equals(event_id) && data[1].equals(event_sub)) {
                for (Integer outcome : entry.getValue().outcomes) {
                    addBatchEvent(st, student, outcome, null, 10, String.valueOf(id));
                }

                for (Integer target : entry.getValue().targets) {
                    addBatchEvent(st, student, target, null, 2, String.valueOf(id));
                }
                result = true;
            }
        }
        return result;
    }

    private void addBatchEvent(Statement st, Integer student, Integer event_id, Integer event_sub, Integer event_type, String notes) throws SQLException {
        String sql = "INSERT INTO `Events` (`date`, `student`, " +
                "`event_type`, `event_id`, `event_sub`, `notes`, `teacher`) VALUES('" + date + "'," + student + "," +
                event_type + "," + event_id + "," + (event_sub != null ? event_sub : "NULL") + "," +
                (notes != null ? "'" + notes + "'" : "NULL")+ "," + settingsManager.teacher + ");";
        st.addBatch(sql);
    }

    private void paintValue(Integer event_id, Integer event_sub, Integer student) {
        String id = event_id + "." + (event_sub != null ? event_sub : "0");
        int row = formData.presentations.indexOf(id);
        int col = formData.students.indexOf(student)+1;
        if (row != -1) tablePresentations.setValueAt(newvalue, row, col);
        else MyLogger.d(TAG, "Data point lost");
        if (newvalue > 3) {
            MyTableModelPlanning model = (MyTableModelPlanning) tablePlanning.getModel();
            model.setValue(event_id, event_sub, col-1, newvalue - 3);
        }
    }

    private String getCondition(Integer student, Integer event_type, Integer event_id, Integer event_sub) {
        return getBasicCondition(student, event_id, event_sub) + " AND " +
                TableEvents.event_type + " = " + event_type;
    }

    private String getBasicCondition(Integer student, Integer event_id, Integer event_sub) {
        return TableEvents.student + "=" + student + " AND " +
                TableEvents.event_id + "=" + event_id + " AND " +
                (event_sub!=null?TableEvents.event_sub + "=" + event_sub:TableEvents.event_sub + " IS NULL");
    }

    private Integer getStudent(int column) {
        return formData.students.get(column);
    }

    private Integer[] getEvent(int row) {
        String event = formData.presentations.get(row);
        String[] ev = event.split("[.]");
        return new Integer[] {Integer.valueOf(ev[0]), (ev[1].equals("0")) ? null : Integer.valueOf(ev[1])};
    }

    private Integer getOldValue(int row, int column) {
        MyTableModelPresentations model = (MyTableModelPresentations) tablePresentations.getModel();
        return model.data[row][column];
    }


    private static class Condition {
        final Integer event_id;
        final Integer event_sub;
        final Integer student;

        public Condition(Integer event_id, Integer event_sub, Integer student) {
            this.event_id = event_id;
            this.event_sub = event_sub;
            this.student = student;
        }
    }

    private static class Link {
        final Integer event_id;
        final Integer event_sub;
        final Integer id;

        public Link(Integer event_id, Integer event_sub, Integer id) {
            this.event_id = event_id;
            this.event_sub = event_sub;
            this.id = id;
        }
    }

}
