package ui.formInternalReports.managers;

import bd.BDManager;
import main.ApplicationLoader;
import ui.formInternalReports.models.PdfTermEvaluations;
import ui.formReports.managers.IManager;
import utils.MyLogger;

import java.sql.*;
import java.util.HashMap;

public class TermEvaluationsManager implements IManager {
    private static final String TAG = TermEvaluationsManager.class.getSimpleName();
    private final int classroom;
    public final HashMap<Integer, HashMap<Integer, Integer>> students;
    public final HashMap<Integer, HashMap<Integer, Integer>> targets;

    public TermEvaluationsManager(int classroom) {
        this.classroom = classroom;
        students = new HashMap<>();
        targets = new HashMap<>();
        load();
    }

    // Students: name, birthday, drive_main, drive_documents, drive_photos, drive_reports
    @Override
    public String[] load() {
        Connection co = null;
        String[] result = new String[4];
        try {
            co = ApplicationLoader.bdManager.connect();
            Statement st = co.createStatement();
            for (int studentId : ApplicationLoader.cacheManager.studentsPerClassroom.get(classroom)) {
                String yearRange = getYearRange(ApplicationLoader.cacheManager.getChildrenYear(studentId, classroom, null) + 1);

                ResultSet rs = st.executeQuery(getQueryForStudents(yearRange, studentId));
                while (rs.next()) {
                    Integer area = rs.getInt("NC_subareas.area");
                    Integer points = rs.getInt("points");
                    HashMap<Integer, Integer> pointsMap = students.computeIfAbsent(studentId, k -> new HashMap<>());
                    pointsMap.put(area, points);
                }
                rs = st.executeQuery(getQueryForTargets(yearRange));

                while (rs.next()) {
                    Integer area = rs.getInt("area");
                    Integer year = rs.getInt("year");
                    Integer points = rs.getInt("points");

                    HashMap<Integer, Integer> years = targets.computeIfAbsent(area, k -> new HashMap<>());
                    years.put(year, points);
                }
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

    private String getYearRange(int year) {
        StringBuilder str = new StringBuilder("(");
        for (int i = 0; i <= PdfTermEvaluations.periods.length - 1 ; i++) {
            if (year < PdfTermEvaluations.periods[i]) {
                str.append(year).append(",");
                str.append(year + 1).append(",");
                break;
            } else {
                if (year == PdfTermEvaluations.periods[i]) {
                    str.append(year - 1).append(",");
                    str.append(year).append(",");
                    break;
                }
            }
        }
        return str.substring(0, str.length() - 1) + ")";
    }

    private String getQueryForStudents(String years, int id) {
        return "SELECT NC_subareas.area, COUNT(Events.id) AS points FROM Events JOIN Students ON Students.id = " +
                "Events.student JOIN Targets ON Events.event_id = Targets.id JOIN NC_subareas ON Targets.subarea = " +
                "NC_subareas.id WHERE Targets.year IN "+ years +" AND Students.id = " + id + " AND " +
                "Events.event_type IN (2, 5) AND Students.classroom = "+ classroom + " GROUP BY Students.id, NC_subareas.area";
    }

    private String getQueryForTargets(String years) {
        return "SELECT NC_subareas.area, Targets.year, COUNT(Targets.id) AS points FROM Targets JOIN NC_subareas ON Targets.subarea = " +
                "NC_subareas.id WHERE year IN " + years + " GROUP BY NC_subareas.area, Targets.year";
    }

}
