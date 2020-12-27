package utils;

import bd.BDManager;
import bd.MySet;
import bd.model.*;
import utils.data.RawData;

import javax.swing.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static java.time.temporal.ChronoUnit.MONTHS;


/**
 * Created by angel on 5/05/17.
 */
public class CacheManager {
    private static final String TAG = CacheManager.class.getSimpleName();
    private final BDManager bdManager;
    private final SettingsManager settingsManager;
    public final LinkedHashMap<Integer, Object[]> students; //name, birthday, drive_main, drive_documents, drive_photos, drive_reports
    public final LinkedHashMap<Integer, Object[]> teachers; //name, classroom
    public final LinkedHashMap<Integer, String> classrooms;
    public final LinkedHashMap<Integer, String[]> areasTarget; //name, nombre
    public final LinkedHashMap<Integer, String[]> areasMontessori;  //name, nombre
    public final LinkedHashMap<Integer, String[]> subareasTarget; //name, nombre
    public final LinkedHashMap<Integer, Object[]> subareasMontessori; //name, nombre, area
    public final LinkedHashMap<Integer, String> observations;
    public final HashMap<Integer, Object[]> outcomes;//name,nombre,subarea,start_month,end_month
    public final HashMap<Integer, Object[]> targets; //name, nombre, subarea, year
    public final HashMap<Integer, Object[]> presentations; //name, nombre, subarea,year,priority
    public final HashMap<Integer, Object[]> presentationssub;//name, nombre
    public final HashMap<Integer, LinkedHashMap<Integer, HashSet<Integer>>> stageAreaSubareaMontessori;
    public HashMap<Double, ArrayList<Integer>> subareasTargetperyear;
    public final LinkedHashMap<Double, LinkedHashMap<Integer, ArrayList<Integer>>> targetsperyearandsubarea;
    public final LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<Integer>>> outcomespermonthandsubarea;
    public final SortedMap<Double, LinkedHashMap<Integer, ArrayList<Integer>>> presentationsperyearandsubarea;
    public final HashMap<Integer, ArrayList<Integer>> studentsperclassroom;
    public final HashMap<Integer, ArrayList<Integer>> subareasTargetperarea;
    public final HashMap<Integer, ArrayList<Integer>> subareasMontessoriperarea;
    public final HashMap<Integer, ArrayList<Integer>> presentationssubperpresentation;
    public final HashMap<Integer, Integer> targetsubareaarea;
    public final ArrayList<Integer> ncTargets;
    public final HashMap<Integer[], PresentationLinks> links;
    public final HashMap<Integer, HashSet<Integer[]>> linksNCOutcomes;
    public final HashMap<Integer, HashSet<Integer[]>> linksNCTargets;
    public final HashMap<Integer, HashMap<String, String>> globalVars;

    public static class PresentationLinks {
        public ArrayList<Integer> outcomes;
        public ArrayList<Integer> targets;
        public PresentationLinks() {
            outcomes = new ArrayList<>();
            targets = new ArrayList<>();
        }
    }

    private final JLabel labelAction;

    public CacheManager(BDManager bdManager, SettingsManager settingsManager, JLabel labelAction, JLabel labelError) throws SQLException {
        this.bdManager = bdManager;
        this.settingsManager = settingsManager;
        this.labelAction = labelAction;
        students = new LinkedHashMap<>();
        teachers = new LinkedHashMap<>();
        classrooms = new LinkedHashMap<>();
        areasTarget = new LinkedHashMap<>();
        areasMontessori = new LinkedHashMap<>();
        subareasTarget = new LinkedHashMap<>();
        subareasMontessori = new LinkedHashMap<>();
        observations = new LinkedHashMap<>();
        targetsperyearandsubarea = new LinkedHashMap<>();
        outcomespermonthandsubarea = new LinkedHashMap<>();
        presentationsperyearandsubarea = new TreeMap<>();
        studentsperclassroom = new HashMap<>();
        subareasTargetperarea = new HashMap<>();
        subareasMontessoriperarea = new HashMap<>();
        presentationssubperpresentation = new HashMap<>();
        outcomes  = new HashMap<>();
        targets = new HashMap<>();
        presentations = new HashMap<>();
        presentationssub = new HashMap<>();
        targetsubareaarea = new HashMap<>();
        ncTargets = new ArrayList<>();
        links = new HashMap<>();
        linksNCOutcomes = new HashMap<>();
        linksNCTargets = new HashMap<>();
        stageAreaSubareaMontessori = new HashMap<>();
        globalVars = new HashMap<>();
        loadData();
    }

    private void loadData() throws SQLException {
        Connection co = null;
        Statement st = null;
        try {
            co = bdManager.connect();
            st = co.createStatement();
            st.execute("START TRANSACTION READ ONLY");
            labelAction.setText("Loading classrooms...");
            String query = "SELECT * FROM ";
            MySet set = new MySet(st.executeQuery(query + TableClassrooms.table_name), BDManager.tableClassrooms, null);
            while (set.next()) {
                Integer id = set.getInt(TableClassrooms.id);
                classrooms.put(id, set.getString(TableClassrooms.name));
                if (!studentsperclassroom.containsKey(id)) studentsperclassroom.put(id, new ArrayList<>());
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
                if (studentsperclassroom.containsKey(classroom)) studentsperclassroom.get(classroom).add(id);
                else {
                    ArrayList<Integer> list = new ArrayList<>();
                    list.add(id);
                    studentsperclassroom.put(classroom, list);
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
                subareasTarget.put(id, new String[]{set.getString(TableNC_subareas.name),
                        set.getString(TableNC_subareas.nombre)});
                targetsubareaarea.put(id,area);
                ArrayList<Integer> t = subareasTargetperarea.computeIfAbsent(area, k -> new ArrayList<>());
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
                LinkedHashMap<Integer, ArrayList<Integer>> tpersubarea = targetsperyearandsubarea.computeIfAbsent(year, k -> new LinkedHashMap<>());
                ArrayList<Integer> list = tpersubarea.computeIfAbsent(subarea, k -> new ArrayList<>());
                list.add(id);
                targets.put(id, new Object[]{name, nombre,subarea,year});
            }
            labelAction.setText("Loading Montessori areas...");
            set = new MySet(st.executeQuery(query + TablePresentations_areas.table_name), BDManager.tablePresentations_areas, null);
            while (set.next()) {
                areasMontessori.put(set.getInt(TablePresentations_areas.id),
                        new String[]{set.getString(TablePresentations_areas.name), set.getString(TablePresentations_areas.nombre)});
            }
            labelAction.setText("Loading Montessori subareas...");
            set = new MySet(st.executeQuery(query + TablePresentations_subareas.table_name), BDManager.tablePresentations_subareas, null);
            bdManager.getValues(co, BDManager.tablePresentations_subareas, null);
            while (set.next()) {
                Integer id = set.getInt(TablePresentations_subareas.id);
                Integer area = set.getInt(TablePresentations_subareas.area);
                subareasMontessori.put(id, new Object[]{set.getString(TablePresentations_subareas.name),
                        set.getString(TablePresentations_subareas.nombre), area});
                ArrayList<Integer> t = subareasMontessoriperarea.computeIfAbsent(area, k -> new ArrayList<>());
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
                LinkedHashMap<Integer, ArrayList<Integer>> ppersubarea = presentationsperyearandsubarea.computeIfAbsent(year, k -> new LinkedHashMap<>());
                ArrayList<Integer> list = ppersubarea.computeIfAbsent(subarea, k -> new ArrayList<>());
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
            labelAction.setText("Loading subpresentations...");
            set = new MySet(st.executeQuery(query + TablePresentations_sub.table_name), BDManager.tablePresentations_sub, null);
            while (set.next()){
                Integer id = set.getInt(TablePresentations_sub.id);
                Integer presentation = set.getInt(TablePresentations_sub.presentation);
                ArrayList<Integer> list = presentationssubperpresentation.computeIfAbsent(presentation, k -> new ArrayList<>());
                list.add(id);
                presentationssub.put(id, new String[]{set.getString(TablePresentations_areas.name), set.getString(TablePresentations_areas.nombre)});
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
                LinkedHashMap<Integer, ArrayList<Integer>> opersubarea =
                        outcomespermonthandsubarea.computeIfAbsent(end_month, k -> new LinkedHashMap<>());
                ArrayList<Integer> list = opersubarea.computeIfAbsent(subarea, k -> new ArrayList<>());
                list.add(id);
                outcomes.put(id, new Object[]{name,nombre,subarea,start_month,end_month});
            }
            labelAction.setText("Loading links...");
            set = new MySet(st.executeQuery(query + TableLinks.table_name), BDManager.tableLinks, null);
            while (set.next()){
                Integer presentation = set.getInt(TableLinks.presentation);
                Integer presentation_sub = set.getInt(TableLinks.presentation_sub);
                Integer outcome = set.getInt(TableLinks.outcomes);
                Integer target = set.getInt(TableLinks.targets);
                Integer[] id = {presentation, presentation_sub};
                if (outcome != null && outcome != 0) {
                    PresentationLinks link = links.computeIfAbsent(id, k -> new PresentationLinks());
                    link.outcomes.add(outcome);
                    HashSet<Integer[]> linkNC = linksNCOutcomes.computeIfAbsent(outcome, k -> new HashSet<>());
                    linkNC.add(id);
                }
                if (target != null && target != 0) {
                    PresentationLinks link = links.computeIfAbsent(id, k -> new PresentationLinks());
                    link.targets.add(target);
                    HashSet<Integer[]> linkNC = linksNCTargets.computeIfAbsent(outcome, k -> new HashSet<>());
                    linkNC.add(id);
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
                    case SettingsManager.START_OF_YEAR -> settingsManager.setDate_SY(value);
                    case SettingsManager.FIRST_TERM -> settingsManager.setDate_FT(value);
                    case SettingsManager.SECOND_TERM -> settingsManager.setDate_ST(value);
                    case SettingsManager.THIRD_TERM -> settingsManager.setDate_TT(value);
                }
            }
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
            ArrayList<Integer> list = studentsperclassroom.get(id);
            if (list.contains(studentId)) return id;
        }
        return null;
    }

    public Vector<String> getClasroomsName(){
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
        return result != null ? (String) result[settingsManager.language] : null;
    }

    public String getOutcomeName(int targetId) {
        Object[] result = outcomes.get(targetId);
        return result != null ? (String) result[settingsManager.language] : null;
    }

    public Integer getTargetSubarea(int targetId) {
        Object[] result = targets.get(targetId);
        return result != null ? (Integer) result[1] : null;
    }

    public String getTargetSubareaName(int id) {
        Object[] result = subareasTarget.get(id);
        int pos = (settingsManager.language == 0) ? 0 : 1;
        return result != null ? (String) result[pos] : null;
    }

    public LinkedHashMap<Integer, ArrayList<Integer>> getOutcomesPerYear(Double year) {
        Integer[] months = RawData.getOutcomeMonthsperYear(year);
        if (months == null) return null;
        else {
            LinkedHashMap<Integer, ArrayList<Integer>> result = new LinkedHashMap<>();
            for (Integer endmonth : months) {
                LinkedHashMap<Integer, ArrayList<Integer>> map = outcomespermonthandsubarea.get(endmonth);
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

    public Integer getStageofClassroom(Integer classroom) {
        return switch (classroom) {
            case 1 -> 0;
            case 2, 3 -> 1;
            case 4 -> 2;
            default -> null;
        };
    }

    public String getNameStageofClassroom(Integer classroom) {
        return switch (classroom) {
            case 1 -> LanguageManager.INFANT_COMMUNITY[settingsManager.language];
            case 2, 3 -> LanguageManager.CHILDRENS_HOUSE[settingsManager.language];
            case 4, 5 -> LanguageManager.PRIMARY[settingsManager.language];
            case 6 -> "Children's House";
            default -> null;
        };
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

    public ArrayList<Integer> loadStudensSortedByAge(int classroom) {
        HashMap<Integer, java.sql.Date> map = new HashMap<>();
        for (int id : studentsperclassroom.get(classroom)) {
            map.put(id, (Date) students.get(id)[1]);
        }

        LinkedList<Map.Entry<Integer, Date>> list = new LinkedList(map.entrySet());
        Comparator<Map.Entry<Integer, Date>> comparator = Comparator.comparing(Map.Entry::getValue);
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
            String name = (String) data[settingsManager.language];
            if (name.toLowerCase().contains(text)) {
                result.put(name, new Integer[]{id, (Integer) data[2]});
            }
        }
        return result;
    }

    public DefaultListModel getClassroomsListModel(){
        DefaultListModel model = new DefaultListModel();
        for (String classroom : RawData.classrooms) {
            model.addElement(classroom);
        }
        return model;
    }

    public DefaultListModel getStudentsListModel(int classroom){
        DefaultListModel model = new DefaultListModel();
        for (int id : studentsperclassroom.get(classroom)) {
            model.addElement(students.get(id)[0]);
        }
        return model;
    }

    public String getChildrenAge(Integer student) {
        Date birthday = (Date)students.get(student)[1];
        LocalDate dateBefore = Instant.ofEpochMilli(birthday.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(new java.util.Date().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        long months = MONTHS.between(dateBefore, dateAfter);

        return months / 12 + (settingsManager.language == 0 ? " years and " : " años y ") + months%12 +
                (settingsManager.language == 0 ? " months" : " meses");
    }
}
