package ui.formClassroom;

import bd.BDManager;
import bd.EventCondition;
import bd.MySet;
import bd.model.TableEvents;
import links.MultipleLinkManager;
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
    final int[] rows;
    final int[] columns;
    final Integer newValue;
    final Date date;
    final ArrayList <EventCondition> check; // To see if we need to add a value after deleting an upper value
    final ArrayList <EventCondition> checkIfRemovedLinks; //To see if we need to remove a link automatically generated
    final ArrayList <EventCondition> checkDeletedPlanning; //To see the actual value of the presentation after deleting the planning
    private final PlanningManager planningManager;
    private final ClassroomForm form;
    private final MultipleLinkManager multipleLinkManager;

    public SWDBUpdater(ClassroomForm form, int[] rows, int[] columns, Integer newValue, Date date) {
        this.form = form;
        this.rows = rows;
        this.columns = columns;
        this.date = date;
        this.newValue = newValue;
        check = new ArrayList<>();
        checkIfRemovedLinks = new ArrayList<>();
        checkDeletedPlanning = new ArrayList<>();
        planningManager = new PlanningManager();
        multipleLinkManager = new MultipleLinkManager(form, date, newValue);
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
                Integer[] ev = form.formData.getEvent(value);
                int student = form.formData.getStudent(column - 1);
                if (newValue != null)
                    checkToAdd(st, student, ev[0], ev[1], getOldValue(value, column - 1),
                            (String) form.tablePresentations.getValueAt(value, column));
                else clearPlanning(st, value, column, student, ev);
            }
        }
    }

    protected void sendToBatch(Statement st){
        Boolean addedLinks = false;
        try {
            ResultSet rs = ApplicationLoader.bdManager.executeBatch(st);
            if (rs.next()) addedLinks = multipleLinkManager.checkForLinksInInsertedIds(st, rs);
            if (checkTempEvents(st) || addedLinks) sendToBatch(st);
            if (checkIfRemovedLinks.size() > 0) {
                multipleLinkManager.checkIfRemovedLinks(st, checkIfRemovedLinks);
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
                    if (newValue != 0) check.add(new EventCondition(event_id, event_sub, student));
                    else form.paintValue(event_id, event_sub, student, newValue);
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
        String text = (String) form.tablePresentations.getValueAt(value, column);
        if (text == null || text.equals("")) return;
        int col = MyTableModelPresentations.DayOfWeekToInt(text) + 1;
        int row = form.formData.students.indexOf(student);
        planningManager.addBatchToRemovePlanning(st, date, student, ev[0], ev[1], text);
        MyTableModelPlanning modelPlanning = (MyTableModelPlanning) form.tablePlanning.getModel();
        modelPlanning.getValueAt(row, col).remove(modelPlanning.getValue(ev[0], ev[1]));
        modelPlanning.fireTableCellUpdated(row, col);
        checkDeletedPlanning.add(new EventCondition(ev[0], ev[1], student));
    }

    private void checkDeletedPlanning(Statement st) throws SQLException {
        for (EventCondition condition : checkDeletedPlanning) {
            MySet set = new MySet(st.executeQuery(BDManager.tableEvents.getValues(condition.getBasicCondition())),
                    BDManager.tableEvents, null);
            int value = 0;
            while (set.next()) {
                int newValue = MyTableModelPresentations.getEvent_typeValue(set.getInt(TableEvents.event_type),
                        set.getString(TableEvents.notes), date, set.getDate(TableEvents.date));
                if (newValue > value) value = newValue;
            }
            int row = form.formData.presentations.indexOf(condition.getCode());
            int col = form.formData.students.indexOf(condition.student)+1;
            String text = (String) form.tablePresentations.getValueAt(row, col);
            if (row != -1 && col != -1) form.tablePresentations.setValueAt(value, row, col);
            row = form.formData.students.indexOf(condition.student);
            col = MyTableModelPresentations.DayOfWeekToInt(text) + 1;
            form.tablePlanning.getModel().setValueAt(null, row, col);
        }
        checkDeletedPlanning.clear();
    }

    private Boolean checkTempEvents(Statement st) throws SQLException {
        boolean result = false;
        for (EventCondition condition : check) {
            MySet set = new MySet(st.executeQuery(BDManager.tableEvents.getValues(EventCondition.getCondition(condition.student,
                    RawData.montessoriEvent_Types[newValue-1], condition.event_id, condition.event_sub))),
                    BDManager.tableEvents, null);
            if (set.data.size() == 0) {
                addBatchEvent(st, condition.student, condition.event_id, condition.event_sub,
                        RawData.montessoriEvent_Types[newValue -1], null);
                result = true;
            } else form.paintValue(condition.event_id, condition.event_sub, condition.student, newValue);
        }
        check.clear();
        return result;
    }

    private void removeUpperValues(Statement st, int start, Integer student, Integer event_id, Integer event_sub,
                                   Integer oldValue) {
        for (int i = start; i < 4; i++) {
            try {
                String sql = ApplicationLoader.bdManager.getTable(TableEvents.table_name).removeValue(
                        EventCondition.getCondition(student, RawData.montessoriEvent_Types[i-1], event_id, event_sub));
                st.addBatch(sql);
                if (newValue < 3 && oldValue > 0) checkIfRemovedLinks.add(new EventCondition(event_id, event_sub, student));
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

    private Integer getOldValue(int row, int column) {
        MyTableModelPresentations model = (MyTableModelPresentations) form.tablePresentations.getModel();
        return model.data[row][column];
    }
}
