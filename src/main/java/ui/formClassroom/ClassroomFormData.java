package ui.formClassroom;

import utils.CacheManager;
import utils.data.RawData;

import javax.swing.*;
import java.sql.Date;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ClassroomFormData {
    private final JPanel frame;
    private final CacheManager cacheManager;
    private JTable tablePresentations;
    public final ArrayList<String> presentations;
    public final ArrayList<Integer> students;
    public final ArrayList<Double> dates;
    Integer classroom = null;
    Integer stage = null;
    Integer area = null;

    public ClassroomFormData(JPanel frame, CacheManager cacheManager) {
        this.frame = frame;
        this.cacheManager = cacheManager;
        presentations = new ArrayList<>();
        students = new ArrayList<>();
        dates = new ArrayList<>();
    }

    public void setTables(JTable tablePresentations){
        this.tablePresentations = tablePresentations;
    }

    public void clear() {
        presentations.clear();
        students.clear();
        dates.clear();
        classroom = null;
        stage = null;
        area = null;
    }

    public void getData(Integer _classroom, Integer _stage, Integer _area) {
        boolean changed = false;
        if (!_classroom.equals(classroom)) {
            classroom = _classroom;
            students.clear();
            dates.clear();
            changed = true;
        }
        if (!_stage.equals(stage)) {
            stage = _stage;
            changed = true;
        }
        if (!_area.equals(area)) {
            area = _area;
            changed = true;
            presentations.clear();
            double min = RawData.yearsmontessori[stage][0];
            double max = RawData.yearsmontessori[stage][1];

            for (double year : cacheManager.presentationsperyearandsubarea.keySet()) {
                if (year >= min && year < max) {
                    LinkedHashMap<Integer, ArrayList<Integer>> presentationspersubarea =
                            cacheManager.presentationsperyearandsubarea.get(year);
                    for (int subarea : presentationspersubarea.keySet()) {
                        ArrayList<Integer> list = cacheManager.subareasMontessoriperarea.get(area);
                        if (list != null && list.contains(subarea))
                            for (int presentation : presentationspersubarea.get(subarea)) {
                                presentations.add(presentation + ".0");
                                ArrayList<Integer> subs = cacheManager.presentationssubperpresentation.get(presentation);
                                if (subs != null) for (Integer sub : subs)
                                    presentations.add(presentation + "." + sub);
                            }
                    }
                }
            }
        }

        if (students.size() == 0) loadStudents(classroom);

        if (changed && classroom != null && stage != null && area != null) {
            ((MyTableModelPresentations) tablePresentations.getModel()).loadData();
        }
    }

    public void loadStudents(Integer _classroom) {
        classroom = _classroom;
        students.clear();
        dates.clear();
        ArrayList<Integer> _students = cacheManager.studentsperclassroom.get(classroom);
        if (_students == null) {
            JOptionPane.showMessageDialog(frame, "There are not students for this classroom", "Error",
                    JOptionPane.ERROR_MESSAGE);
            classroom = null;
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault()).minusMonths(1);
        for (Integer student: _students) {
            ZonedDateTime birthday = ((Date)cacheManager.students.get(student)[1]).toLocalDate().atStartOfDay(ZoneId.systemDefault());

            Double age = ChronoUnit.MONTHS.between(birthday, now)/12d;
            boolean added = false;
            for (int i = 0; i < dates.size(); i++) {
                if (age<dates.get(i)) {
                    students.add(i, student);
                    dates.add(i, age);
                    added = true;
                    break;
                }
            }
            if (!added) {
                students.add(student);
                dates.add(age);
            }
        }
    }
}