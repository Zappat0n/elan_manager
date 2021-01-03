package ui.formClassroom;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEvents;
import main.ApplicationLoader;
import planning.PlanningManager;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import utils.data.RawData;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;

public class SWDBUpdater extends SwingWorker<Boolean, Integer> {
    private static final String TAG = SWDBUpdater.class.getSimpleName();
    final JTable tablePresentations;
    final JTable tablePlanning;
    final int[] rows;
    final int[] columns;
    final Integer newValue;
    final Date date;
    final ArrayList <Condition> check; // To see if we need to add a value after deleting an upper value
    final ArrayList <Condition> checkIfRemovedLinks; //To see if we need to remove a link automatically generated
    final ArrayList <Condition> checkDeletedPlanning; //To see the actual value of the presentation after deleting the planning
    final ClassroomFormData formData;
    private final PlanningManager planningManager;

    public SWDBUpdater(JTable tablePresentations, JTable tablePlanning, int[] rows, int[] columns, Integer newValue,
                       Date date, ClassroomFormData formData) {
        this.formData = formData;
        this.tablePresentations = tablePresentations;
        this.tablePlanning = tablePlanning;
        this.rows = rows;
        this.columns = columns;
        this.date = date;
        this.newValue = newValue;
        check = new ArrayList<>();
        checkIfRemovedLinks = new ArrayList<>();
        checkDeletedPlanning = new ArrayList<>();
        planningManager = new PlanningManager();
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        Statement st = ApplicationLoader.bdManager.prepareBatch();
        st.addBatch("DELETE FROM tempIds WHERE teacher = "+ApplicationLoader.settingsManager.teacher+";");
        addElementsToBatch(st);
        sendToBatch(st);
        return null;
    }

    protected void addElementsToBatch(Statement st) throws SQLException {
        for (int value : rows) {
            for (int column : columns) {
                if (column == 0) continue;
                Integer[] ev = formData.getEvent(value);
                int student = formData.getStudent(column - 1);
                if (newValue != null)
                    checkToAdd(st, student, ev[0], ev[1], getOldValue(value, column - 1),
                            (String) tablePresentations.getValueAt(value, column));
                else clearPlanning(st, value, column, student, ev);
            }
        }
    }

    protected void sendToBatch(Statement st){
        Boolean addedLinks = false;
        try {
            ResultSet rs = ApplicationLoader.bdManager.executeBatch(st);
            if (rs.next()) addedLinks = checkTempIds(st, rs);
            if (checkTempEvents(st) || addedLinks) sendToBatch(st);
            if (checkIfRemovedLinks.size() > 0) {
                checkIfRemovedLinks(st);
                //co.commit();
            }
            if (checkDeletedPlanning.size() > 0) checkDeletedPlanning(st);
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(st);
            ApplicationLoader.bdManager.closeQuietlyConnection();
        }
    }

    protected void checkToAdd(Statement st, Integer student, Integer event_id, Integer event_sub,
                              Integer oldValue, String text) throws SQLException {
        if (newValue < 4) { //Presentation
            if (newValue > oldValue) {
                addBatchEvent(st, student, event_id, event_sub, RawData.montessoriEvent_Types[newValue -1], null);
            } else {
                if (oldValue < 4) {
                    removeUpperValues(st, newValue + 1, student, event_id, event_sub, oldValue);
                    if (newValue != 0) check.add(new Condition(event_id, event_sub, student));
                    else paintValue(event_id, event_sub, student);
                }
            }
        } else { //Planning
            if (!newValue.equals(oldValue)) {
                if (oldValue > 3) planningManager.addBatchToRemovePlanning(st, date, student, event_id, event_sub, text);
                addBatchEvent(st, student, event_id, event_sub, 13, MyPopUpMenuPresentations.planning_values[newValue-4]);
            }
        }
    }

    protected void clearPlanning(Statement st, int value, int column, int student, Integer[] ev) throws SQLException {
        String text = (String) tablePresentations.getValueAt(value, column);
        if (text == null || text.equals("")) return;
        int col = MyTableModelPresentations.DayOfWeekToInt(text) + 1;
        int row = formData.students.indexOf(student);
        planningManager.addBatchToRemovePlanning(st, date, student, ev[0], ev[1], text);
        MyTableModelPlanning modelPlanning = (MyTableModelPlanning) tablePlanning.getModel();
        modelPlanning.getValueAt(row, col).remove(modelPlanning.getValue(ev[0], ev[1]));
        modelPlanning.fireTableCellUpdated(row, col);
        checkDeletedPlanning.add(new Condition(ev[0], ev[1], student));
    }

    private void checkDeletedPlanning(Statement st) throws SQLException {
        for (Condition condition : checkDeletedPlanning) {
            MySet set = new MySet(st.executeQuery(BDManager.tableEvents.getValues(getBasicCondition(condition.student,
                    condition.event_id, condition.event_sub))), BDManager.tableEvents, null);
            int value = 0;
            while (set.next()) {
                int newValue = MyTableModelPresentations.getEvent_typeValue(set.getInt(TableEvents.event_type),
                        set.getString(TableEvents.notes), date, set.getDate(TableEvents.date));
                if (newValue > value) value = newValue;
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
            for (Map.Entry<int[], CacheManager.PresentationLinks> entry : ApplicationLoader.cacheManager.links.entrySet()) {
                int sub = (condition.event_sub != null) ? condition.event_sub : 0;
                int[] data = entry.getKey();
                if (data[0] == condition.event_id && data[1] == sub) {
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

    private Boolean checkTempEvents(Statement st) throws SQLException {
        boolean result = false;
        for (Condition condition : check) {
            MySet set = new MySet(st.executeQuery(BDManager.tableEvents.getValues(getCondition(condition.student,
                    RawData.montessoriEvent_Types[newValue -1], condition.event_id, condition.event_sub))),
                    BDManager.tableEvents, null);
            if (set.data.size() == 0) {
                addBatchEvent(st, condition.student, condition.event_id, condition.event_sub,
                        RawData.montessoriEvent_Types[newValue -1], null);
                result = true;
            } else paintValue(condition.event_id, condition.event_sub, condition.student);
        }
        check.clear();
        return result;
    }

    private void removeUpperValues(Statement st, int start, Integer student, Integer event_id, Integer event_sub,
                                   Integer oldValue) {
        for (int i = start; i < 4; i++) {
            try {
                String sql = ApplicationLoader.bdManager.getTable(TableEvents.table_name).removeValue(
                        getCondition(student, RawData.montessoriEvent_Types[i-1], event_id, event_sub));
                st.addBatch(sql);
                if (newValue < 3 && oldValue > 0) checkIfRemovedLinks.add(new Condition(event_id, event_sub, student));
            } catch (SQLException e) {
                MyLogger.e(TAG, e);
                return;
            }
        }
    }

    private void addBatchEvent(Statement st, Integer student, Integer event_id, Integer event_sub, Integer event_type, String notes) throws SQLException {
        String sql = "INSERT INTO `Events` (`date`, `student`, `event_type`, `event_id`, `event_sub`, `notes`, `teacher`) " +
                "VALUES('" + date + "'," + student + "," + event_type + "," + event_id + "," +
                (event_sub != null ? event_sub : "NULL") + "," + (notes != null ? "'" + notes + "'" : "NULL")+ "," +
                ApplicationLoader.settingsManager.teacher + ");";
        st.addBatch(sql);
    }

    private void paintValue(Integer event_id, Integer event_sub, Integer student) {
        String id = event_id + "." + (event_sub != null ? event_sub : "0");
        int row = formData.presentations.indexOf(id);
        int col = formData.students.indexOf(student)+1;
        if (row != -1) tablePresentations.setValueAt(newValue, row, col);
        else MyLogger.d(TAG, "Data point lost");
        if (newValue > 3) {
            MyTableModelPlanning model = (MyTableModelPlanning) tablePlanning.getModel();
            model.setValue(event_id, event_sub, col-1, newValue - 3);
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
}
