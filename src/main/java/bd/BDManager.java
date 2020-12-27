package bd;

import bd.model.*;
import utils.MyLogger;
import utils.SettingsManager;

import javax.swing.*;
import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Created by angel on 4/02/17.
 */
public class BDManager {
    private static final String TAG = BDManager.class.getSimpleName();
    public static final TableClassrooms tableClassrooms = new TableClassrooms();
    public static final TableContacts tableContacts = new TableContacts();
    public static final TableDriveFolderKeys tableDriveFolderKeys = new TableDriveFolderKeys();
    public static final TableEvents tableEvents = new TableEvents();
    public static final TableEventsEoY tableEventsEoY = new TableEventsEoY();
    public static final TableEventsYet tableEventsYet = new TableEventsYet();
    public static final TableEvents_type tableEvents_type = new TableEvents_type();
    public static final TableGlobal_vars tableGlobal_vars = new TableGlobal_vars();
    public static final TableLinks tableLinks = new TableLinks();
    public static final TableMedia tableMedia = new TableMedia();
    public static final TablePresentations tablePresentations = new TablePresentations();
    public static final TablePresentations_areas tablePresentations_areas = new TablePresentations_areas();
    private static final TablePresentations_direct_targets tablePresentations_direct_targets = new TablePresentations_direct_targets();
    private static final TablePresentations_indirect_targets tablePresentations_indirect_targets = new TablePresentations_indirect_targets();
    public static final TablePresentations_sub tablePresentations_sub = new TablePresentations_sub();
    public static final TablePresentations_subareas tablePresentations_subareas = new TablePresentations_subareas();
    public static final TableStudents tableStudents = new TableStudents();
    public static final TableTargets tableNC_targets = new TableTargets();
    public static final TableNC_areas tableNC_areas = new TableNC_areas();
    public static final TableNC_subareas tableNC_subareas = new TableNC_subareas();
    public static final TableOutcomes tableOutcomes = new TableOutcomes();
    private static final TableTargets_montessori tableTargets_montessori = new TableTargets_montessori();
    public static final TableObservations tableObservations = new TableObservations();
    public static final TableTeachers tableTeachers = new TableTeachers();
    public static final MyTable[] tables = {tableClassrooms, tableContacts, tableEvents, tableEvents_type, tableNC_areas,
            tableNC_subareas, tableObservations, tablePresentations, tablePresentations_sub, tablePresentations_areas,
            tablePresentations_direct_targets, tablePresentations_indirect_targets, tablePresentations_subareas,
            tableStudents, tableNC_targets, tableTargets_montessori, tableTeachers};


    private Connection co = null;
    private Statement st = null;
    private final SettingsManager settingsManager;
    private JFrame frame;
    private final String user;
    private final String password;
    public Boolean noData = false;

    public BDManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        user = settingsManager.user;
        password = settingsManager.getValue(SettingsManager.PWD);
        if (user == null || password == null) {
            noData = true;
            showError("Unable to connect to database.\nUser data is missing.");
        }
    }

    public Connection connect(){
        if (noData) return null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", user);
        connectionProps.put("password", password);
        String link = settingsManager.getValue(SettingsManager.LINK)+TimeZone.getDefault().getID() ;
        try {
            co = DriverManager.getConnection(link, connectionProps);
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
            showError("No puedo conectar con la base de datos");
            return null;
        }
        return co;
    }

    public void importFile(File file) {
        Connection co = null;
        String line;
        try {
            String[] values;
            FileReader reader = new FileReader(file);
            BufferedReader textReader = new BufferedReader(reader);
            String tableName = textReader.readLine();
            MyTable table =getTable(tableName);
            if (table == null) {
                MyLogger.d(TAG, "No he podido encontrar una tabla con el nombre: " + tableName);
                return;
            }
            String[] keys = Arrays.copyOfRange(table.fields, 1, table.fields.length);
            co = connect();
            while ((line = textReader.readLine()) != null ) {
                if (line.equals("")) return;
                if (line.contains("\t")) values = line.split("\t");
                else if (line.contains(";")) values = line.split(";");
                else values = line.split(",");

                addValue(co, table, keys, values);
            }
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            closeQuietly(co);
        }
    }

    public MyTable getTable (String tableName) {
        for (MyTable table : tables) if (table.getName().equals(tableName)) return table;
        return null;
    }

    public LinkedHashMap<String, Integer> getAllNameAndIdForTable(Connection co, MyTable table) {
        try {
            MySet set = getValues(co, table, null);

            if (set.getCount() == 0) return null;
            LinkedHashMap<String, Integer> data = new LinkedHashMap(set.getCount());
            while (set.next()) {
                data.put(set.getString("name"), set.getInt("id"));
            }
            return data;
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        }
        return null;
    }

    public Vector<String> getAllNameForTable(MyTable table) {
        Connection co = null;
        try {
            co = connect();
            MySet set = getValues(co, table, null);

            if (set.getCount() == 0) return null;
            Vector<String> data = new Vector<>(set.getCount());
            while (set.next()) {
                if (set.getInt("id") != -1) data.add(set.getString("name"));
            }
            return data;
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            closeQuietly(co);
        }
        return null;
    }

    public void addFrame(JFrame frame) {
        this.frame = frame;
    }

    private void showError(String error) {
        JOptionPane.showMessageDialog(frame, error, "ERROR", JOptionPane.ERROR_MESSAGE);
    }

    public void addValue(MyTable table, String[] key, String[] value) {
        Connection co =  null;
        try {
            co = connect();
            addValue(co, table, key, value);
        } finally {
            closeQuietly(co);
        }
    }

    public Integer addEvent(Connection co, Date date, Integer studentId, Integer event_type, Integer event_id,
                            Integer event_sub, String notes) {
        PreparedStatement ps = null;
        try {
            if (co == null || co.isClosed()) co = connect();
            //co.setAutoCommit(false);
            ps = co.prepareStatement("INSERT INTO `Events` (`date`, `student`, " +
                            "`event_type`, `event_id`, `event_sub`, `notes`, `teacher`) VALUES (?, ?, ?, ?, ?, ?, ?);",
                    Statement.RETURN_GENERATED_KEYS);
            prepareToAddEvent(ps, date, studentId, event_type, event_id, event_sub, notes);
            if (ps.executeUpdate() != 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    Integer id = rs.getInt(1);
                    MyLogger.d(TAG, id + " : Event added.");
                    return id;
                }
            }
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(ps);
        }
        return null;
    }

    private void prepareToAddEvent(PreparedStatement ps, Date date, Integer studentId, Integer event_type, Integer event_id,
                                   Integer event_sub, String notes) {
        try {
            ps.setDate(1, date);
            if (studentId != null) ps.setInt(2, studentId);
            else ps.setNull(2, Types.INTEGER);
            if (event_type != null) ps.setInt(3, event_type);
            else ps.setNull(3, Types.INTEGER);
            if (event_id != null) ps.setInt(4, event_id);
            else ps.setNull(4, Types.INTEGER);
            if (event_sub != null) ps.setInt(5, event_sub);
            else ps.setNull(5, Types.INTEGER);
            if (notes != null) ps.setString(6, notes);
            else ps.setNull(6, Types.LONGVARCHAR);
            if (settingsManager.teacher != null) ps.setInt(7,  settingsManager.teacher);
            else ps.setNull(7, Types.INTEGER);
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        }
    }

    public void addOrEditEventForStudentAndTypeAndId(Connection co, Date date, Integer studentId, Integer event_type,
                                                Integer event_id, Integer event_sub, String notes) {
        MySet set = getValues(co, tableEvents, TableEvents.student +"=" + studentId + " AND "
                + TableEvents.event_type + "=" + event_type + " AND " + TableEvents.event_id + "=" + event_id);
        if (set.next()) {
            Integer id = set.getInt(TableEvents.id);
            String query = "UPDATE " + TableEvents.table_name + " SET " +
                    TableEvents.notes + "='" + notes + "' WHERE id=" + id;
            executeQueryUpdate(co, query);
        } else {
            addEvent(co, date, studentId, event_type, event_id, event_sub, notes);
        }
    }

    public void addMedia(Date date, int student, int presentation, Integer presentation_sub, String comment,
                         String fileId){
        try {
            co = connect();
            String query = "Insert into Media (date, student, presentation, presentation_sub, comment, fileId)"
                    + " values (?, ?, ?, ?, ?, ?)";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = co.prepareStatement(query);
            preparedStmt.setDate (1, date);
            preparedStmt.setInt   (2, student);
            preparedStmt.setInt(3, presentation);
            if (presentation_sub != null) preparedStmt.setInt(4, presentation_sub);
            else preparedStmt.setNull(4, java.sql.Types.INTEGER);
            if (comment != null) preparedStmt.setString(5, comment);
            else preparedStmt.setNull(5, Types.VARCHAR);
            preparedStmt.setString(6, fileId);
            preparedStmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(co);
        }
    }

    public HashMap<Integer, ArrayList<String>> getContacts(Connection co, ArrayList<Integer> students) {
        HashMap<Integer, ArrayList<String>> contacts = new HashMap<>();
        HashMap<Integer, ArrayList<String>> result = new HashMap<>();
        MySet set = getValues(co, BDManager.tableContacts, null);

        while (set.next()) {
            String email = set.getString(TableContacts.email);
            Integer student1 = set.getInt(TableContacts.student1);
            Integer student2 = set.getInt(TableContacts.student2);
            Integer student3 = set.getInt(TableContacts.student3);
            Integer student4 = set.getInt(TableContacts.student4);
            Integer student5 = set.getInt(TableContacts.student5);
            if (student1 != null) {
                ArrayList<String> emails = contacts.computeIfAbsent(student1, k-> new ArrayList<>());
                emails.add(email);
            }
            if (student2 != null) {
                ArrayList<String> emails = contacts.computeIfAbsent(student2, k-> new ArrayList<>());
                emails.add(email);
            }
            if (student3 != null) {
                ArrayList<String> emails = contacts.computeIfAbsent(student3, k-> new ArrayList<>());
                emails.add(email);
            }
            if (student4 != null) {
                ArrayList<String> emails = contacts.computeIfAbsent(student4, k-> new ArrayList<>());
                emails.add(email);
            }
            if (student5 != null) {
                ArrayList<String> emails = contacts.computeIfAbsent(student5, k-> new ArrayList<>());
                emails.add(email);
            }
        }

        for (Integer student : students) {
            ArrayList<String> contact = contacts.get(student);
            if (contact != null) result.put(student, contact);
        }
        return result;
    }

    public void addValue(Connection co, MyTable table, String[] key, String[] value) {
        if (value == null || table == null) return;
        executeQueryUpdate(co, table.addValue(key, value));
    }

    public void updateValues(Connection co, MyTable table, String[] key, String[] value, String condition) {
        if (value == null || table == null) return;
        executeQueryUpdate(co, table.updateValue(key, value, condition));
    }

    public MySet getValues(Connection connection, MyTable table, String condition) {
        if (table == null) return null;
        return executeQuery(connection, table.getValues(condition), table, null);
    }

    public MySet getValues(Connection connection, MyTable table, String[] keys, String condition) {
        if (table == null) return null;
        return executeQuery(connection, table.getValues(keysToString(keys), condition), table, keys);
    }

    public void removeValue(Connection co, MyTable table, String condition, Boolean confirm) {
        if (confirm && JOptionPane.showConfirmDialog(frame, "¿Borrar datos?") != JOptionPane.YES_OPTION) return;
        if (table == null) return;
        executeQueryUpdate(co, table.removeValue(condition));
    }

    public static String getSqlOrValues(String variable, int[] values) {
        if (values.length == 0) return null;
        StringBuilder c = new StringBuilder("(" + variable + "=");
        boolean first = true;
        for (int value : values) {
            if (first) {
                c.append(value);
                first = false;
            } else c.append(" OR ").append(variable).append("=").append(value);
        }
        return c + ")";
    }

    private synchronized MySet executeQuery(Connection co, String query, MyTable table, String[] keys) {
        try {
            if (co == null) co = connect();
            st = co.createStatement();
            return new MySet(st.executeQuery(query), table, keys);
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
        } finally {
            closeQuietlyStatement();
        }
        return null;
    }

    public synchronized void executeQueryUpdate(Connection connection, String query) {
        try {
            st = connection.createStatement();
            st.executeUpdate(query);
        } catch (SQLException e) {
            MyLogger.e(TAG, e);
            showError("No he podido realizar la operación\n"+e.getMessage());
        } finally {
            closeQuietlyStatement();
        }
    }

    public static void closeQuietly(Connection connection, PreparedStatement ps) {
        try { ps.close(); } catch (Exception e) { MyLogger.e(TAG, e); }
        try { connection.close(); } catch (Exception e) { MyLogger.e(TAG, e); }
    }

    public static void closeQuietly(Connection connection, Statement st) {
        try { st.close(); } catch (Exception e) { MyLogger.e(TAG, e); }
        try { connection.close(); } catch (Exception e) { MyLogger.e(TAG, e); }
    }

    public static void closeQuietly(Connection connection, Statement st, ResultSet rs) {
        try { rs.close(); } catch (Exception e) { MyLogger.e(TAG, e); }
        try { st.close(); } catch (Exception e) { MyLogger.e(TAG, e); }
        try { connection.close(); } catch (Exception e) { MyLogger.e(TAG, e); }
    }

    private void closeQuietlyStatement(){
        try { st.close(); } catch (Exception e) { MyLogger.e(TAG, e); }
    }

    public static void closeQuietly(ResultSet rs){
        try { rs.close(); } catch (Exception e) { MyLogger.e(TAG, e); }
    }

    public static void closeQuietly(PreparedStatement ps){
        try { ps.close(); } catch (Exception e) { MyLogger.e(TAG, e); }
    }

    public static void closeQuietly(Statement st){
        try { st.close(); } catch (Exception e) { MyLogger.e(TAG, e); }
    }

    public static void closeQuietly(Connection connection){
        try {connection.close();} catch (Exception e) { MyLogger.e(TAG, e); }
    }

    private String keysToString(String[] keys) {
        StringBuilder values = new StringBuilder(keys[0]);
        for (int i = 1; i < keys.length; i++) {
            values.append(", ").append(keys[i]);
        }
        return values.toString();
    }

    public static String encodeString(String text){
        return text.replace("'", "''").replace("\t", "        ");
    }
}
