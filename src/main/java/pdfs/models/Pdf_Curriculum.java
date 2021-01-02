package pdfs.models;

import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Pdf_Curriculum extends PDFForm_Curriculum {
    private static final String TAG = Pdf_Curriculum.class.getSimpleName();
    final int col_size = 40;

    public Pdf_Curriculum(SettingsManager settingsManager, CacheManager cacheManager, int stage){
        super(settingsManager, cacheManager, stage);
        nextPage();

        try {
            position = addLine("MONTESSORI PRESENTATIONS FOR " + STAGE_NAME[stage], Math.round(margin), position,
                    SIZE_TITLE, true);

            for(int area : data.keySet()) {
                position = addLine(cacheManager.areasMontessori.get(area)[settingsManager.language], Math.round(margin), position,
                        SIZE_AREA, true);
                LinkedHashMap<Integer, ArrayList<Integer>> subareas = data.get(area);
                if (subareas == null) {
                    MyLogger.d(TAG, "WARNING: Area " + area + " does not have subareas");
                    continue;
                }
                for (int subarea : subareas.keySet()) {
                    position = addLine((String)cacheManager.subareasMontessori.get(subarea)[settingsManager.language], Math.round(margin), position,
                            SIZE_SUBAREA, false);
                    ArrayList<Integer> presentations = subareas.get(subarea);
                    if (presentations == null) {
                        MyLogger.d(TAG, "WARNING: Subarea " + subarea + " does not have presentations");
                        continue;
                    }
                    for (int presentation: subareas.get(subarea)) {
                        position = addLine((String)cacheManager.presentations.get(presentation)[0],
                                Math.round(margin) + col_size, position, SIZE_PRESENTATION, false);
                        ArrayList<Integer> presentations_sub = cacheManager.presentationsSubPerPresentation.get(presentation);
                        if (presentations_sub!= null){
                            for (int presentation_sub: presentations_sub) {
                                position = addLine((String)cacheManager.presentationsSub.get(presentation_sub)[settingsManager.language],
                                        Math.round(margin) + col_size * 2, position, SIZE_PRESENTATION_SUB, false);
                            }
                        }

                    }
                }

            }
            saveFile(settingsManager.getValue(SettingsManager.REPORTS_DIR)+"prueba.pdf");
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        }
    }
}
