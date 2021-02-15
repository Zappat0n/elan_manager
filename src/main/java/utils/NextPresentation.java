package utils;

import java.util.*;

public class NextPresentation {
    private final CacheManager cacheManager;
    private final SettingsManager settingsManager;
    final Integer presentation;
    final Integer presentation_sub;

    public Integer nextPresentation;
    public Integer nextPresentationSub;

    String nextPresentationText;
    public String[] linksText;

    public NextPresentation(CacheManager cacheManager, SettingsManager settingsManager, Integer presentation, Integer presentation_sub) {
        this.cacheManager = cacheManager;
        this.settingsManager = settingsManager;
        this.presentation = presentation;
        this.presentation_sub = presentation_sub;
        calculate();
    }

    private void calculate() {
        calculateNextPresentation();
        calculateNextPresentationSub();
    }

    public Boolean doExists() {
        return nextPresentation != null;
    }

    public void setLinksText(String[] linksText) {
        this.linksText = linksText;
    }

    private void calculateNextPresentation() {
        Object[] data = cacheManager.presentations.get(presentation);   //name, nombre, subarea,year,position
        Integer subarea = (Integer) data[2];
        Integer currentPriority = (Integer) data[4];
        Double year = (Double) data[3];
        ArrayList<Integer> presentations = cacheManager.presentationsPerYearAndSubarea.get(year).get(subarea);

        //Check same subarea
        int index;
        if (currentPriority == null || currentPriority == 0) {
            Collections.sort(presentations);
            index = presentations.indexOf(presentation);
            nextPresentation = index < presentations.size() - 2 ? presentations.get(index) : null;
        }
        else {
            for (Integer id : presentations) {
                Integer priority = (Integer)cacheManager.presentations.get(presentation)[4];
                if (priority == currentPriority + 1) nextPresentation = id; break;
            }
        }

        //Search for following subarea
        if (nextPresentation == null) {
            LinkedHashMap<Integer, ArrayList<Integer>> subareas = cacheManager.presentationsPerYearAndSubarea.get(year);
            List list = Arrays.asList(subareas.keySet().toArray());
            index = list.indexOf(subarea);
            if (index < list.size() - 1) {
                subarea = (Integer) list.get(index + 1);
                nextPresentation = subareas.get(subarea).get(0);
            }
        }
    }

    private void calculateNextPresentationSub() {
        ArrayList<Integer> subs = cacheManager.presentationsSubPerPresentation.get(nextPresentation);
        nextPresentationSub = (subs != null) ? subs.get(0) :  0;
    }

    public String getNextPresentationText() {
        nextPresentationText = "";
        if (nextPresentation != -1) {
            Object[] data = cacheManager.presentations.get(nextPresentation);

            Integer subarea = (Integer) data[2];
            Integer area = (Integer) cacheManager.subareasMontessori.get(subarea)[2];//name, nombre, area
            nextPresentationText += cacheManager.areasMontessori.get(area)[settingsManager.language] + " - ";
            nextPresentationText += cacheManager.subareasMontessori.get(subarea)[settingsManager.language] + "\n";
            Object[] sub = cacheManager.presentationsSub.get(nextPresentationSub); //name, nombre
            nextPresentationText += data[settingsManager.language] + ((sub != null) ? " --> " + sub[settingsManager.language] : "") + "\n";
        }
        return nextPresentationText;
    }


}
