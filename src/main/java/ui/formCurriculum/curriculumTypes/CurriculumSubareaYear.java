package ui.formCurriculum.curriculumTypes;

import main.ApplicationLoader;
import ui.formCurriculum.Curriculum;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

public class CurriculumSubareaYear extends Curriculum {
    final TreeMap<Double, TreeMap<Integer, ArrayList<Integer>>> presentations;

    public CurriculumSubareaYear(int stage, int area, boolean nc) {
        super(stage, area, nc);
        presentations = new TreeMap<>();
        load();
    }

    private Data getData(int id) {
        Object[] d;
        if (nc) {
            d = ApplicationLoader.cacheManager.targets.get(id);//name, nombre, subarea, year
        } else {
            d = ApplicationLoader.cacheManager.presentations.get(id);//name, nombre, subarea, year, position
        }
        return new Data((String)d[ApplicationLoader.settingsManager.language], (Double)d[3], (int)d[2]);
    }

    private String getSubareaName(int subarea) {
        if (nc){
            return (String)ApplicationLoader.cacheManager.subareasTarget.get(subarea)[
                    ApplicationLoader.settingsManager.language];
        } else {
            return (String)ApplicationLoader.cacheManager.subareasMontessori.get(subarea)[
                    ApplicationLoader.settingsManager.language];
        }
    }

    private String getItemName(int id) {
        if (nc) {
            return (String)ApplicationLoader.cacheManager.targets.get(id)[
                    ApplicationLoader.settingsManager.language];
        } else {
            return (String)ApplicationLoader.cacheManager.presentations.get(id)[
                    ApplicationLoader.settingsManager.language];
        }
    }

    private void load() {
        Set<Integer> items = nc ? ApplicationLoader.cacheManager.targets.keySet() :
                ApplicationLoader.cacheManager.presentations.keySet();
        for (int id : items) {
            Data data = getData(id);
            if (includePresentation(data.year, data.subarea)) {
                TreeMap<Integer, ArrayList<Integer>> presentationsForYear = presentations.computeIfAbsent(data.year,
                        k -> new TreeMap<>());
                ArrayList<Integer> pr = presentationsForYear.computeIfAbsent(data.subarea, k -> new ArrayList<>());
                pr.add(id);
            }
        }

        for (double year : presentations.keySet()) {
            addTitle("\n--- Year " + year + " ---\n");
            TreeMap<Integer, ArrayList<Integer>> subareas = presentations.get(year);
            for (int subarea : subareas.keySet()) {
                ArrayList<Integer> ids = subareas.get(subarea);
                addLine(0, subarea, getSubareaName(subarea));
                for (int id : ids) {
                    addLine(1, id, getItemName(id));
                    if (!nc) {
                        ArrayList<Integer> subs = ApplicationLoader.cacheManager.presentationsSubPerPresentation.get(id);
                        if (subs != null) for (int sub : subs) {
                            addLine(2, sub, (String)ApplicationLoader.cacheManager.presentationsSub.get(sub)[
                                    ApplicationLoader.settingsManager.language]);
                        }
                    }
                }
            }

        }
    }

    private class Data {
        String name;
        double year;
        int subarea;

        public Data(String name, double year, int subarea) {
            this.name = name;
            this.year = year;
            this.subarea = subarea;
        }
    }
}
