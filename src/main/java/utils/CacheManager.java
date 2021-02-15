package utils;

import bd.BDManager;
import bd.MySet;
import bd.model.*;
import main.ApplicationLoader;
import utils.data.RawData;

import javax.swing.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;


/**
 * Created by angel on 5/05/17.
 */
public class CacheManager {
    private static final String TAG = CacheManager.class.getSimpleName();
    public final LinkedHashMap<Integer, Object[]> students; //name, birthday, drive_main, drive_documents, drive_photos, drive_reports
    public final LinkedHashMap<Integer, Object[]> teachers; //name, classroom
    public final LinkedHashMap<Integer, String> classrooms;
    public final LinkedHashMap<Integer, String[]> areasTarget; //name, nombre, subarea
    public final LinkedHashMap<Integer, String[]> areasMontessori;  //name, nombre
    public final LinkedHashMap<Integer, Object[]> subareasTarget; //name, nombre, area
    public final LinkedHashMap<Integer, Object[]> subareasMontessori; //name, nombre, area
    public final LinkedHashMap<Integer, String> observations;
    public final HashMap<Integer, Object[]> outcomes;//name,nombre,subarea,start_month,end_month
    public final HashMap<Integer, Object[]> targets; //name, nombre, subarea, year
    public final HashMap<Integer, Object[]> presentations; //name, nombre, subarea,year,priority
    public final HashMap<Integer, Object[]> presentationsSub;//name, nombre
    public final HashMap<Integer, LinkedHashMap<Integer, HashSet<Integer>>> stageAreaSubareaMontessori;
    public final LinkedHashMap<Double, LinkedHashMap<Integer, ArrayList<Integer>>> targetsPerYearAndSubarea;
    public final LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> outcomesPerMonthAndSubarea;
    public final SortedMap<Double, LinkedHashMap<Integer, ArrayList<Integer>>> presentationsPerYearAndSubarea;
    public final HashMap<Integer, ArrayList<Integer>> studentsPerClassroom;
    public final HashMap<Integer, ArrayList<Integer>> areasTargetPerStage;
    public final HashMap<Integer, ArrayList<Integer>> subareasTargetPerArea;
    public final HashMap<Integer, ArrayList<Integer>> subareasMontessoriPerArea;
    public final HashMap<Integer, ArrayList<Integer>> presentationsSubPerPresentation;
    public final HashMap<Integer, Integer> targetSubareaArea;
    public final ArrayList<Integer> ncTargets;
    public final HashMap<String, PresentationLinks> links;

    public static class PresentationLinks {
        public final ArrayList<Integer> outcomes;
        public final ArrayList<Integer> targets;
        public PresentationLinks() {
            outcomes = new ArrayList<>();
            targets = new ArrayList<>();
        }
    }

    private final JLabel labelAction;

    public CacheManager(JLabel labelAction) {
        this.labelAction = labelAction;
        students = new LinkedHashMap<>();
        teachers = new LinkedHashMap<>();
        classrooms = new LinkedHashMap<>();
        areasTarget = new LinkedHashMap<>();
        areasMontessori = new LinkedHashMap<>();
        subareasTarget = new LinkedHashMap<>();
        subareasMontessori = new LinkedHashMap<>();
        observations = new LinkedHashMap<>();
        targetsPerYearAndSubarea = new LinkedHashMap<>();
        outcomesPerMonthAndSubarea = new LinkedHashMap<>();
        presentationsPerYearAndSubarea = new TreeMap<>();
        studentsPerClassroom = new HashMap<>();
        areasTargetPerStage  = new HashMap<>();
        subareasTargetPerArea = new HashMap<>();
        subareasMontessoriPerArea = new HashMap<>();
        presentationsSubPerPresentation = new HashMap<>();
        outcomes  = new HashMap<>();
        targets = new HashMap<>();
        presentations = new HashMap<>();
        presentationsSub = new HashMap<>();
        targetSubareaArea = new HashMap<>();
        ncTargets = new ArrayList<>();
        links = new HashMap<>();
        stageAreaSubareaMontessori = new HashMap<>();
        loadData();
    }

    private void loadData() {
        Connection co = null;
        Statement st = null;
        try {
            co = ApplicationLoader.bdManager.connect();
            st = co.createStatement();
            st.execute("START TRANSACTION READ ONLY");
            labelAction.setText("Loading classrooms...");
            String query = "SELECT * FROM ";
            MySet set = new MySet(st.executeQuery(query + TableClassrooms.table_name), BDManager.tableClassrooms, null);
            while (set.next()) {
                Integer id = set.getInt(TableClassrooms.id);
                classrooms.put(id, set.getString(TableClassrooms.name));
                if (!studentsPerClassroom.containsKey(id)) studentsPerClassroom.put(id, new ArrayList<>());
            }
            labelAction.setText("Loading observations...");
            set = new MySet(st.executeQuery(query + TableObservations.table_name), BDManager.tableObservations, null);
            while (set.next()) {
                Integer id = set.getInt(TableObservations.id);
                observations.put(id, set.getString(TableObservations.name));
            }
            labelAction.setText("Loading students...");
            set = new MySet(st.executeQuery(query + TableStudents.table_name), BDManager.tableStudents, null);
            while (set.next()) {
                Integer id = set.getInt(TableStudents.id);
                Integer classroom = set.getInt(TableStudents.classroom);
                String folder_main = set.getString(TableStudents.drive_main);
                String folder_documents = set.getString(TableStudents.drive_documents);
                String folder_photos = set.getString(TableStudents.drive_photos);
                String folder_reports = set.getString(TableStudents.drive_reports);
                students.put(id, new Object[]{set.getString(TableStudents.name), set.getDate(TableStudents.birth_date),
                        folder_main, folder_documents, folder_photos, folder_reports});
                if (studentsPerClassroom.containsKey(classroom)) studentsPerClassroom.get(classroom).add(id);
                else {
                    ArrayList<Integer> list = new ArrayList<>();
                    list.add(id);
                    studentsPerClassroom.put(classroom, list);
                }
            }
            labelAction.setText("Loading teachers...");
            set = new MySet(st.executeQuery(query + TableTeachers.table_name), BDManager.tableTeachers, null);
            while (set.next()) {
                Integer id = set.getInt(TableTeachers.id);
                teachers.put(id, new Object[]{set.getString(TableTeachers.name), set.getInt(TableTeachers.classroom)});
            }

            labelAction.setText("Loading NC areas...");
            set = new MySet(st.executeQuery(query + TableNC_areas.table_name), BDManager.tableNC_areas, null);
            while (set.next()) {
                areasTarget.put(set.getInt(TableNC_areas.id), new String[]{
                        set.getString(TableNC_areas.name), set.getString(TableNC_areas.nombre)});
            }
            labelAction.setText("Loading NC subareas...");
            set = new MySet(st.executeQuery(query + TableNC_subareas.table_name), BDManager.tableNC_subareas, null);
            while (set.next()) {
                Integer id = set.getInt(TableNC_subareas.id);
                Integer area = set.getInt(TableNC_subareas.area);
                subareasTarget.put(id, new Object[]{set.getString(TableNC_subareas.name),
                        set.getString(TableNC_subareas.nombre), area});
                targetSubareaArea.put(id,area);
                ArrayList<Integer> t = subareasTargetPerArea.computeIfAbsent(area, k -> new ArrayList<>());
                t.add(id);
            }
            labelAction.setText("Loading targets...");
            set = new MySet(st.executeQuery(query + TableTargets.table_name), BDManager.tableNC_targets, null);
            while (set.next()){
                Integer id = set.getInt(TableTargets.id);
                Double year = set.getDouble(TableTargets.year);
                Integer subarea = set.getInt(TableTargets.subarea);
                String name = set.getString(TableTargets.name);
                String nombre = set.getString(TableTargets.nombre);
                Integer nc = set.getInt(TableTargets.NC);
                if (nc == 1) ncTargets.add(nc);
                LinkedHashMap<Integer, ArrayList<Integer>> tPerSubarea = targetsPerYearAndSubarea.computeIfAbsent(year, k -> new LinkedHashMap<>());
                ArrayList<Integer> list = tPerSubarea.computeIfAbsent(subarea, k -> new ArrayList<>());
                list.add(id);
                targets.put(id, new Object[]{name, nombre,subarea,year});
                int stage = getNCStage(year);
                list = areasTargetPerStage.computeIfAbsent(stage, k -> new ArrayList<>());
                Integer area = (Integer) subareasTarget.get(subarea)[2];
                if (!list.contains(area)) {
                    list.add(area);
                }
            }
            labelAction.setText("Loading Montessori areas...");
            set = new MySet(st.executeQuery(query + TablePresentations_areas.table_name), BDManager.tablePresentations_areas, null);
            while (set.next()) {
                areasMontessori.put(set.getInt(TablePresentations_areas.id),
                        new String[]{set.getString(TablePresentations_areas.name), set.getString(TablePresentations_areas.nombre)});
            }
            labelAction.setText("Loading Montessori subareas...");
            set = new MySet(st.executeQuery(query + TablePresentations_subareas.table_name), BDManager.tablePresentations_subareas, null);
            ApplicationLoader.bdManager.getValues(co, BDManager.tablePresentations_subareas, null);
            while (set.next()) {
                Integer id = set.getInt(TablePresentations_subareas.id);
                Integer area = set.getInt(TablePresentations_subareas.area);
                subareasMontessori.put(id, new Object[]{set.getString(TablePresentations_subareas.name),
                        set.getString(TablePresentations_subareas.nombre), area});
                ArrayList<Integer> t = subareasMontessoriPerArea.computeIfAbsent(area, k -> new ArrayList<>());
                t.add(id);
            }
            labelAction.setText("Loading presentations...");
            set = new MySet(st.executeQuery(query + TablePresentations.table_name), BDManager.tablePresentations, null);
            while (set.next()){
                Integer id = set.getInt(TablePresentations.id);
                Double year = set.getDouble(TablePresentations.year);
                Integer subarea = set.getInt(TablePresentations.subarea);
                String name = set.getString(TablePresentations.name);
                String nombre = set.getString(TablePresentations.nombre);
                Integer priority = set.getInt(TablePresentations.priority);
                LinkedHashMap<Integer, ArrayList<Integer>> pPerSubarea = presentationsPerYearAndSubarea.computeIfAbsent(year, k -> new LinkedHashMap<>());
                ArrayList<Integer> list = pPerSubarea.computeIfAbsent(subarea, k -> new ArrayList<>());
                list.add(id);
                Integer area = (Integer) subareasMontessori.get(subarea)[2];
                Integer stage = getStage(year);
                LinkedHashMap<Integer, HashSet<Integer>> areas = stageAreaSubareaMontessori.get(stage);
                if (areas == null) areas = new LinkedHashMap<>();
                HashSet<Integer> subareas = areas.get(area);
                if (subareas == null) subareas = new HashSet<>();
                areas.put(area, subareas);
                subareas.add(subarea);
                stageAreaSubareaMontessori.put(stage, areas);
                presentations.put(id, new Object[]{name,nombre, subarea,year, priority});
            }
            labelAction.setText("Loading exercises...");
            set = new MySet(st.executeQuery(query + TablePresentations_sub.table_name), BDManager.tablePresentations_sub, null);
            while (set.next()){
                Integer id = set.getInt(TablePresentations_sub.id);
                Integer presentation = set.getInt(TablePresentations_sub.presentation);
                ArrayList<Integer> list = presentationsSubPerPresentation.computeIfAbsent(presentation, k -> new ArrayList<>());
                list.add(id);
                presentationsSub.put(id, new String[]{set.getString(TablePresentations_areas.name), set.getString(TablePresentations_areas.nombre)});
            }
            labelAction.setText("Loading outcomes...");
            set = new MySet(st.executeQuery(query + TableOutcomes.table_name), BDManager.tableOutcomes, null);
            while (set.next()){
                Integer id = set.getInt(TableOutcomes.id);
                String name = set.getString(TableOutcomes.name);
                String nombre = set.getString(TableOutcomes.nombre);
                Integer start_month = set.getInt(TableOutcomes.start_month);
                Integer end_month = set.getInt(TableOutcomes.end_month);
                Integer subarea = set.getInt(TableOutcomes.subarea);
                LinkedHashMap<Integer, ArrayList<Integer>> oPerSubarea =
                        outcomesPerMonthAndSubarea.computeIfAbsent(end_month, k -> new LinkedHashMap<>());
                ArrayList<Integer> list = oPerSubarea.computeIfAbsent(subarea, k -> new ArrayList<>());
                list.add(id);
                outcomes.put(id, new Object[]{name,nombre,subarea,start_month,end_month});
                double months = end_month;
                int stage = getNCStage(months / 12);
                list = areasTargetPerStage.computeIfAbsent(stage, k -> new ArrayList<>());
                Integer area = (Integer) subareasTarget.get(subarea)[2];
                if (!list.contains(area)) {
                    list.add(area);
                }

            }
            labelAction.setText("Loading links...");
            set = new MySet(st.executeQuery(query + TableLinks.table_name), BDManager.tableLinks, null);
            while (set.next()){
                int presentation = set.getInt(TableLinks.presentation);
                int presentation_sub = set.getInt(TableLinks.presentation_sub);
                Integer outcome = set.getInt(TableLinks.outcomes);
                Integer target = set.getInt(TableLinks.targets);
                String id = presentation + "." + presentation_sub;
                if (outcome != null && outcome != 0) {
                    PresentationLinks link = links.computeIfAbsent(id, k -> new PresentationLinks());
                    link.outcomes.add(outcome);
                }
                if (target != null && target != 0) {
                    PresentationLinks link = links.computeIfAbsent(id, k -> new PresentationLinks());
                    link.targets.add(target);
                }
            }
            labelAction.setText("Loading variables...");
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            year = cal.get(Calendar.MONTH) < 9 ? year - 1 : year;
            set = new MySet(st.executeQuery(query + TableGlobal_vars.table_name+ " WHERE year = " + year),
                    BDManager.tableGlobal_vars, null);
            while (set.next()){
                String name = set.getString(TableGlobal_vars.name);
                String value = set.getString(TableGlobal_vars.value);
                switch (name) {
                    case SettingsManager.START_OF_YEAR : ApplicationLoader.settingsManager.setDate_SY(value); break;
                    case SettingsManager.FIRST_TERM : ApplicationLoader.settingsManager.setDate_FT(value); break;
                    case SettingsManager.SECOND_TERM : ApplicationLoader.settingsManager.setDate_ST(value); break;
                    case SettingsManager.THIRD_TERM : ApplicationLoader.settingsManager.setDate_TT(value);
                }
            }
        } catch (Exception ex) {
            MyLogger.e(TAG, ex);
        } finally {
            BDManager.closeQuietly(co, st);
        }
    }

    public Integer getClassroomId(String classroom) {
        for (Integer id: classrooms.keySet()) {
            if (classrooms.get(id).equals(classroom)) return id;
        }
        return null;
    }

    public Integer getClassroomId(Integer studentId) {
        for (Integer id: classrooms.keySet()) {
            ArrayList<Integer> list = studentsPerClassroom.get(id);
            if (list.contains(studentId)) return id;
        }
        return null;
    }

    public Vector<String> getClassroomsNames(){
        Vector<String> result = new Vector<>();
        boolean first=true;
        for (String classroom: classrooms.values()) {
            if (!first) {
                result.add(classroom);
            } else first = false;
        }
        return result;
    }

    public String getTargetName(int targetId) {
        Object[] result = targets.get(targetId);
        return result != null ? (String) result[ApplicationLoader.settingsManager.language] : null;
    }

    public String getOutcomeName(int targetId) {
        Object[] result = outcomes.get(targetId);
        return result != null ? (String) result[ApplicationLoader.settingsManager.language] : null;
    }

    public Integer getTargetSubarea(int targetId) {
        Object[] result = targets.get(targetId);
        return result != null ? (Integer) result[1] : null;
    }

    public String getTargetSubareaName(int id) {
        Object[] result = subareasTarget.get(id);
        int pos = (ApplicationLoader.settingsManager.language == 0) ? 0 : 1;
        return result != null ? (String) result[pos] : null;
    }

    public LinkedHashMap<Integer, ArrayList<Integer>> getOutcomesPerYear(Double year) {
        Integer[] months = RawData.getOutcomeMonthsperYear(year);
        if (months == null) return null;
        else {
            LinkedHashMap<Integer, ArrayList<Integer>> result = new LinkedHashMap<>();
            for (Integer endMonth : months) {
                LinkedHashMap<Integer, ArrayList<Integer>> map = outcomesPerMonthAndSubarea.get(endMonth);
                for (Integer key : map.keySet()) {
                    ArrayList<Integer> al = result.get(key);
                    if (al == null) al = new ArrayList<>();
                    al.addAll(map.get(key));
                    result.put(key, al);
                }
            }
            return result;
        }
    }

    public Double getOutcomeYear(Integer end_month) {
        if (end_month <= 36) return 2.5;
        else if (end_month < 48) return 3d;
        else if (end_month < 60) return 4d;
        else return 5d;
    }

    public Integer getStage(double year) {
        int i = 0;
        for (double value : RawData.yearMontessoriStage) {
            if (year < value) return i;
            else i++;
        }
        return i;
    }

    public Integer getNCStage(double year) {
        int i = 0;
        for (int value : RawData.yearsNC) {
            if (year <= value) return RawData.yearsNC[i];
            else i++;
        }
        return RawData.yearsNC[i];
    }

    public Integer getStageOfClassroom(Integer classroom) {
        Integer result;
        switch (classroom) {
            case 1 : result = 0; break;
            case 2 :  case 3 : result = 1; break;
            case 4 : result = 2; break;
            default : result = null;
        }
        return result;
    }

    public String getNameStageOfClassroom(Integer classroom) {
        String result;
        switch (classroom) {
            case 1 : result =  LanguageManager.INFANT_COMMUNITY[ApplicationLoader.settingsManager.language]; break;
            case 2 : case 3 : result =  LanguageManager.CHILDRENS_HOUSE[ApplicationLoader.settingsManager.language]; break;
            case 4 : case 5 : result =  LanguageManager.PRIMARY[ApplicationLoader.settingsManager.language]; break;
            case 6 : result =  "Children's House"; break;
            default : result = null;
        }
        return result;
    }


    public ArrayList<Integer> getPresentations(int subarea, double startYear, double endYear) {
        ArrayList<Integer> list = new ArrayList<>();
        presentations.forEach((key,value) -> {//name, nombre, subarea,year
            if (subarea == (Integer)value[2]) {
                double year = (Double) value[3];
                if (year >= startYear && year < endYear) list.add(key);
            }
        });
        return list;
    }

    public ArrayList<Integer> loadStudentsSortedByAge(int classroom) {
        HashMap<Integer, java.sql.Date> map = new HashMap<>();
        for (int id : studentsPerClassroom.get(classroom)) {
            map.put(id, (Date) students.get(id)[1]);
        }

        LinkedList<Map.Entry<Integer, Date>> list = new LinkedList<>(map.entrySet());
        Comparator<Map.Entry<Integer, Date>> comparator = Map.Entry.comparingByValue();
        list.sort(comparator.reversed());

        ArrayList<Integer> temp = new ArrayList<>();
        for ( Map.Entry<Integer, Date> pair : list) {
            temp.add(pair.getKey());
        }

        return temp;
    }

    public LinkedHashMap<String, Integer[]> searchPresentationWithText(String text, int stage){
        LinkedHashMap<String, Integer[]> result = new LinkedHashMap<>();
        double min = RawData.yearsmontessori[stage][0];
        double max = RawData.yearsmontessori[stage][1];
        //new Object[]{name,subarea,year}
        for (Integer id : presentations.keySet()) {
            Object[] data = presentations.get(id);
            Double year = (Double) data[3];
            if (year < min || year >= max) continue;
            String name = (String) data[ApplicationLoader.settingsManager.language];
            if (name.toLowerCase().contains(text)) {
                result.put(name, new Integer[]{id, (Integer) data[2]});
            }
        }
        return result;
    }

    public DefaultListModel<String> getClassroomsListModel(){
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String classroom : RawData.classrooms) {
            model.addElement(classroom);
        }
        return model;
    }

    public DefaultListModel<String> getStudentsListModel(int classroom){
        DefaultListModel<String> model = new DefaultListModel<>();
        for (int id : studentsPerClassroom.get(classroom)) {
            model.addElement((String)students.get(id)[0]);
        }
        return model;
    }

    public String getChildrenAge(Integer student) {
        Date birthday = (Date)students.get(student)[1];
        LocalDate dateBefore = Instant.ofEpochMilli(birthday.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(new java.util.Date().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        long months = MONTHS.between(dateBefore, dateAfter);

        return months / 12 + (ApplicationLoader.settingsManager.language == 0 ? " years and " : " años y ") + months%12 +
                (ApplicationLoader.settingsManager.language == 0 ? " months" : " meses");
    }

    public Integer getChildrenYear(int student, int classroom, Date date) {
        Date birthday = (Date)students.get(student)[1];
        Integer[] classroomYears = RawData.yearsPerClassroom[classroom];
        date = date == null ? new Date(new java.util.Date().getTime()) : date;

        LocalDate dateBefore = Instant.ofEpochMilli(birthday.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        long yearsLong = YEARS.between(dateBefore, dateAfter);
        int years = (int) yearsLong;

        if (Arrays.stream(classroomYears).anyMatch(x -> x == years)) {
            return years;
        } else {
            if (years < classroomYears[0]) {
                return classroomYears[0];
            }
            if (years > classroomYears[2]) {
                return classroomYears[2];
            }
        }

        return null;
    }

}
