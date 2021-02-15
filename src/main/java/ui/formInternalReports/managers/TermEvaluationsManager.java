package ui.formInternalReports.managers;

import bd.BDManager;
import main.ApplicationLoader;
import ui.formReports.managers.IManager;
import utils.MyLogger;
import utils.data.RawData;

import java.sql.*;
import java.util.HashMap;

public class TermEvaluationsManager implements IManager {
    private static final String TAG = TermEvaluationsManager.class.getSimpleName();
    private final int classroom;
    private final Date startDate;
    private final Date endDate;
    public final HashMap<Integer, HashMap<Integer, Integer>> students;
    public final HashMap<Integer, HashMap<Integer, Integer>> targets;

    public TermEvaluationsManager(int classroom, Date startDate, Date endDate) {
        this.classroom = classroom;
        this.startDate = startDate;
        this.endDate = endDate;
        students = new HashMap<>();
        targets = new HashMap<>();
        load();
    }

    @Override
    public String[] load() {
        Connection co = null;
        String[] result = new String[4];
        try {
            co = ApplicationLoader.bdManager.connect();
            Statement st = co.createStatement();
            ResultSet rs = st.executeQuery(getQueryForStudents());
            while (rs.next()) {
                Integer student = rs.getInt("Students.id");
                Integer area = rs.getInt("NC_subareas.area");
                Integer points = rs.getInt("points");
                HashMap<Integer, Integer> pointsMap = students.computeIfAbsent(student, k -> new HashMap<>());
                pointsMap.put(area, points);
            }
            rs = st.executeQuery(getQueryForTargets(RawData.yearsPerClassroom[classroom]));

            while (rs.next()) {
                Integer area = rs.getInt("area");
                Integer year = rs.getInt("year");
                Integer points = rs.getInt("points");

                HashMap<Integer, Integer> years = targets.computeIfAbsent(area, k -> new HashMap<>());
                years.put(year, points);
            }
        } catch (Exception ex) {
            MyLogger.e(TAG, ex);
        } finally {
            BDManager.closeQuietly(co);
        }
        return result;
    }

    @Override
    public void save() {

    }

    private String getQueryForStudents() {
        return "SELECT Students.id, NC_subareas.area, COUNT(Events.id) AS points FROM Events JOIN Students ON Students.id = " +
                "Events.student JOIN Targets ON Events.event_id = Targets.id JOIN NC_subareas ON Targets.subarea = " +
                "NC_subareas.id WHERE Events.`date` > '"+ startDate +"' AND Events.`date`  < '"+ endDate +"' AND " +
                "Events.event_type IN (2, 5) AND Students.classroom = "+ classroom + " GROUP BY Students.id, NC_subareas.area";
    }

    private String getQueryForTargets(Integer[] years) {
        StringBuilder str = new StringBuilder("(");
        for (int year : years) {
            str.append(year).append(",");
        }
        str = new StringBuilder(str.substring(0, str.length() - 1) + ")");

        return "SELECT NC_subareas.area, Targets.year, COUNT(Targets.id) AS points FROM Targets JOIN NC_subareas ON Targets.subarea = " +
                "NC_subareas.id WHERE year IN " + str + " GROUP BY NC_subareas.area, Targets.year";
    }

}
