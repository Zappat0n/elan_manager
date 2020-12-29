package ui.formCurriculum.curriculumTypes;

import ui.formCurriculum.Curriculum;
import utils.CacheManager;
import utils.SettingsManager;

import java.util.ArrayList;
import java.util.TreeMap;

public class CurriculumSubareaYear extends Curriculum {
    final TreeMap<Double, TreeMap<Integer, ArrayList<Integer>>> presentations;

    public CurriculumSubareaYear(CacheManager cacheManager, SettingsManager settingsManager, int stage, int area) {
        super(cacheManager, settingsManager, stage, area);
        presentations = new TreeMap<>();
        load();
    }

    private void load() {
        for (int id : cacheManager.presentations.keySet()) {
            Object[] data = cacheManager.presentations.get(id); //name, nombre, subarea, year, position
            Double year = (Double) data[3];
            int subarea = (int)data[2];
            if (includePresentation(year, subarea)) {
                TreeMap<Integer, ArrayList<Integer>> presentationsForYear = presentations.computeIfAbsent(year,
                        k -> new TreeMap<>());
                ArrayList<Integer> pr = presentationsForYear.computeIfAbsent(subarea, k -> new ArrayList<>());
                pr.add(id);
            }
        }

        for (double year : presentations.keySet()) {
            addTitle("\n--- Year " + year + " ---\n");
            TreeMap<Integer, ArrayList<Integer>> subareas = presentations.get(year);
            for (int subarea : subareas.keySet()) {
                ArrayList<Integer> ids = subareas.get(subarea);
                addLine(0, subarea, (String)cacheManager.subareasMontessori.get(subarea)[settingsManager.language]);
                for (int id : ids) {
                    addLine(1, id, (String)cacheManager.presentations.get(id)[settingsManager.language]);
                    ArrayList<Integer> subs = cacheManager.presentationssubperpresentation.get(id);
                    if (subs != null) for (int sub : subs) {
                        addLine(2, sub, (String)cacheManager.presentationsSub.get(sub)[settingsManager.language]);
                    }
                }
            }

        }
    }
}
