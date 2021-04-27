package ui.formInternalReports.managers;

import bd.BDManager;
import main.ApplicationLoader;
import ui.formInternalReports.models.PdfTermEvaluations;
import ui.formReports.managers.IManager;
import utils.MyLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static ui.formInternalReports.models.InternalReportSkeleton.STAGE_YEARS;

public class TermEvaluationsManager implements IManager {
    private static final String TAG = TermEvaluationsManager.class.getSimpleName();
    private final HashMap<Integer, Integer> studentsToAdd;
    public final HashMap<Integer, HashMap<Integer, Integer>> students;
    public final HashMap<Integer, HashMap<Integer, Integer>> targets;

    public TermEvaluationsManager(HashMap<Integer, Integer> studentsToAdd) {
        this.studentsToAdd = studentsToAdd;
        students = new HashMap<>();
        targets = new HashMap<>();
        load();
    }

    // Students: name, birthday, drive_main, drive_documents, drive_photos, drive_reports
    @Override
    public String[] load() {
        ArrayList<Integer> yearsQueried = new ArrayList<>();
        Connection co = null;
        String[] result = new String[4];
        try {
            co = ApplicationLoader.bdManager.connect();
            Statement st = co.createStatement();
            for (int studentId : studentsToAdd.keySet()) {
                int age = studentsToAdd.get(studentId);
                String yearRange = getYearRange(age);
                ResultSet rs;

                if (!yearRange.contains(",")) {
                     rs = st.executeQuery(getQueryForStudentsOutcomes(studentId, Integer.parseInt(yearRange)));
                    while (rs.next()) {
                        int area = rs.getInt("NC_subareas.area");
                        Integer points = rs.getInt("points");
                        HashMap<Integer, Integer> pointsMap = students.computeIfAbsent(studentId, k -> new HashMap<>());
                        Integer value = pointsMap.get(area);
                        pointsMap.put(area, points + (value != null ? value : 0));
                    }


                    if (!yearsQueried.contains(age)) {
                        yearsQueried.add(age);
                        rs = st.executeQuery(getQueryForOutcomes());
                        while (rs.next()) {
                            Integer area = rs.getInt("area");
                            int end_month = rs.getInt("end_month");
                            int points = rs.getInt("points");
                            HashMap<Integer, Integer> years = targets.computeIfAbsent(area, k -> new HashMap<>());
                            Integer year = Math.round(((float)end_month)/12);
                            Integer value = years.get(year);
                            years.put(year, points + (value != null ? value : 0));
                        }
                    }
                } else {
                    rs = st.executeQuery(getQueryForStudentsTargets(yearRange, studentId));
                    while (rs.next()) {
                        Integer area = rs.getInt("NC_subareas.area");
                        Integer points = rs.getInt("points");
                        HashMap<Integer, Integer> pointsMap = students.computeIfAbsent(studentId, k -> new HashMap<>());
                        pointsMap.put(area, points);
                    }

                    if (!yearsQueried.contains(age)) {
                        yearsQueried.add(age);
                        rs = st.executeQuery(getQueryForTargets(yearRange));
                        while (rs.next()) {
                            Integer area = rs.getInt("area");
                            Integer year = rs.getInt("year");
                            int points = rs.getInt("points");

                            HashMap<Integer, Integer> years = targets.computeIfAbsent(area, k -> new HashMap<>());
                            Integer value = years.get(year);
                            years.put(year, points + (value != null ? value : 0));
                        }
                    }
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

        if (year <= STAGE_YEARS[0]) {
            return String.valueOf(year);
        }

        for (int i = 1; i <= STAGE_YEARS.length - 2 ; i++) {
            if (year < STAGE_YEARS[i]) {
                str.append(STAGE_YEARS[i - 1]).append(",");
                str.append(year).append(",");
                return str.substring(0, str.length() - 1) + ")";
            }
        }
        return "("+ STAGE_YEARS[STAGE_YEARS.length - 2] + "," + year + ")";
    }

    private String getQueryForStudentsTargets(String years, int id) {
        return "SELECT NC_subareas.area, COUNT(Events.id) AS points FROM Events JOIN Students ON Students.id = " +
                "Events.student JOIN Targets ON Events.event_id = Targets.id JOIN NC_subareas ON Targets.subarea = " +
                "NC_subareas.id WHERE Targets.year IN "+ years +" AND Students.id = " + id + " AND " +
                "Events.event_type IN (4, 5) GROUP BY Students.id, NC_subareas.area";
    }

    private String getQueryForStudentsOutcomes(int id, int end_year) {
        return "SELECT NC_subareas.area, COUNT(Events.id) AS points FROM Events JOIN Students ON Students.id = " +
                "Events.student JOIN Outcomes ON Events.event_id = Outcomes.id JOIN NC_subareas ON Outcomes.subarea = " +
                "NC_subareas.id WHERE Outcomes.end_month <= " + end_year * 12 + " AND Students.id = " + id +
                " AND Events.event_type IN (10, 11) GROUP BY Students.id, NC_subareas.area";
    }

    private String getQueryForTargets(String years) {
        return "SELECT NC_subareas.area, Targets.year, COUNT(Targets.id) AS points FROM Targets JOIN NC_subareas ON Targets.subarea = " +
                "NC_subareas.id WHERE year IN " + years + " GROUP BY NC_subareas.area, Targets.year";
    }

    private String getQueryForOutcomes() {
        return "SELECT NC_subareas.area, Outcomes.end_month, COUNT(Outcomes.id) AS points FROM Outcomes JOIN NC_subareas ON Outcomes.subarea = " +
                "NC_subareas.id GROUP BY NC_subareas.area, Outcomes.end_month";
    }
}
