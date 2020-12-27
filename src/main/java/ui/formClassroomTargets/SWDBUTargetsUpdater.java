package ui.formClassroomTargets;

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

public class SWDBUTargetsUpdater extends SwingWorker {
    private static final String TAG = SWDBUTargetsUpdater.class.getSimpleName();
    Connection co;
    final JTable tableTargets;
    final MyTableModelTargets modelTargets;

    final BDManager bdManager;
    final SettingsManager settingsManager;
    final CacheManager cacheManager;
    final int[] rows;
    final int[] columns;
    final Integer newvalue;
    final Date date;
    final ArrayList <Condition> check; // To see if we need to add a value after deleting an upper value
    Integer[] types;
    //types = (row < ((MyTableModelTargets) tableTargets.getModel()).outcomes.size()) ? RawData.ncOutcomes_Types : RawData.ncTargets_Types;

    public SWDBUTargetsUpdater(BDManager bdManager, SettingsManager settingsManager, CacheManager cacheManager,
                               JTable tableTargets, int[] rows, int[] columns, Integer newvalue, Date date) {
        this.bdManager = bdManager;
        this.settingsManager = settingsManager;
        this.cacheManager = cacheManager;
        this.tableTargets = tableTargets;
        modelTargets = (MyTableModelTargets ) tableTargets.getModel();
        this.rows = rows;
        this.columns = columns;
        this.date = date;
        this.newvalue = newvalue;
        check = new ArrayList<>();
    }

    @Override
    protected Object doInBackground() throws Exception {
        Statement st = initBatch();
        for (int row : rows) {
            for (int column : columns) {
                if (column == 0) continue;
                Integer event = getEvent(row);
                Integer student = getStudent(column - 1);
                types = (row < ((MyTableModelTargets) tableTargets.getModel()).outcomes.size()) ?
                        RawData.ncOutcomes_Types : RawData.ncTargets_Types;
                addToBatch(st, student, event, getOldValue(row, column - 1), row, column);
            }
        }
        sendToBatch(st);
        return null;
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

    protected void addToBatch(Statement st, Integer student, Integer event_id, Integer oldvalue, int row, int col) throws SQLException {
        if (newvalue > oldvalue)
            addBatchEvent(st, student, event_id, types[newvalue-1]);
        else {
            removeUpperValues(st, newvalue+1, student, event_id);
            if (newvalue != 0) check.add(new Condition(event_id, types[newvalue - 1], student));
            else tableTargets.setValueAt(newvalue, row, col);
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
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co, st);
        }
    }

    private Boolean checkTempIds(Statement st, ResultSet rs) throws SQLException {
        Boolean result = false;
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
            Integer event_id = set.getInt(TableEvents.event_id);
            Integer student = set.getInt(TableEvents.student);
            Integer event_type = set.getInt(TableEvents.event_type);
            paintValue(event_id, event_type, student);
        }
        st.execute("DELETE FROM tempIds WHERE teacher = " + settingsManager.teacher + ";");
        return false;
    }

    private Boolean checkTempEvents(Statement st) throws SQLException {
        boolean result = false;
        for (Condition condition : check) {
            MySet set = new MySet(st.executeQuery(BDManager.tableEvents.getValues(getCondition(condition.student,
                    condition.event_type, condition.event_id))),
                    BDManager.tableEvents, null);
            if (set.data.size() == 0) {
                addBatchEvent(st, condition.student, condition.event_id, types[newvalue-1]);
                result = true;
            } else paintValue(condition.event_id, condition.event_type, condition.student);
        }
        check.clear();
        return result;
    }

    private void removeUpperValues(Statement st, int start, Integer student, Integer event_id) {
        for (int i = start; i < 4; i++) {
            try {
                String sql = bdManager.getTable(TableEvents.table_name).removeValue(
                        getCondition(student, types[i-1], event_id));
                st.addBatch(sql);
            } catch (SQLException e) {
                MyLogger.e(TAG, e);
                return;
            }
        }
    }

    private void paintValue(Integer event_id, Integer event_type, Integer student) {
        if (event_type == 9 || event_type == 10 || event_type == 11) {
            tableTargets.setValueAt(newvalue, modelTargets.outcomes.indexOf(event_id),
                    modelTargets.students.indexOf(student)+1);
        } else if (event_type == 2 || event_type == 4 || event_type == 5) {
            tableTargets.setValueAt(newvalue, modelTargets.outcomes.size() + modelTargets.targets.indexOf(event_id),
                    modelTargets.students.indexOf(student)+1);
        }
    }

    private void addBatchEvent(Statement st, Integer student, Integer event_id, Integer event_type) throws SQLException {
        String sql = "INSERT INTO `Events` (`date`, `student`, " +
                "`event_type`, `event_id`, `notes`, `teacher`) VALUES('" + date + "'," + student + "," +
                event_type + "," + event_id + "," + "NULL" +
                "," + settingsManager.teacher + ");";
        st.addBatch(sql);
    }

    private String getCondition(Integer student, Integer event_type, Integer event_id) {
        return TableEvents.student + "=" + student + " AND " +
                TableEvents.event_id + "=" + event_id + " AND " +
                TableEvents.event_type + " = " + event_type;
    }

    private Integer getStudent(int column) {
        return modelTargets.students.get(column);
    }

    private Integer getEvent(int row) {
        return  (row < modelTargets.outcomes.size()) ? modelTargets.outcomes.get(row) :
                modelTargets.targets.get(row - modelTargets.outcomes.size());
    }

    private Integer getOldValue(int row, int column) {
        return modelTargets.data[row][column];
    }

    private static class Condition {
        final Integer event_id;
        final Integer event_type;
        final Integer student;

        public Condition(Integer event_id, Integer event_type, Integer student) {
            this.event_id = event_id;
            this.event_type = event_type;
            this.student = student;
        }
    }
}
