package pdfs.models;

import bd.BDManager;
import pdfs.tables.Table;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import org.apache.pdfbox.pdmodel.PDPage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Created by angel on 24/04/17.
 */
public class Pdf_EYPupilReports extends PDFForm_Reports {
    private static final String TAG = Pdf_EYPupilReports.class.getSimpleName();
    private final int fontMaintTitleSize = 18;
    private final int fontSize = 9;
    private Integer position;
    private PDPage page;
    private final int[] titleData = new int[2];
    private LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>> model;

    public Pdf_EYPupilReports(BDManager bdManager, CacheManager cacheManager, SettingsManager settingsManager,
                              Connection co, Integer studentId, Integer classroom, Date reportDate, BufferedImage logo) {
        super(bdManager, cacheManager, settingsManager, co, "Targets", studentId, classroom, reportDate, null, logo);
        try {
            if (co == null) co = bdManager.connect();
            nextPage();
            position = createHeadPage(page, "Early Years Monthly Report", studentName, (new Date()).getTime(),
                    null, null);
            model = loadModel(new Double[]{2.5, 5d});
            loadEvents(co, studentId);
            checkCurrentStage();
            checkNextStage();
            doc.save(fileName);
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            try {
                doc.close();
            } catch (IOException e) {
                MyLogger.e(TAG, e);
            }
            if (connectedHere) BDManager.closeQuietly(co);
        }
    }

    private void checkCurrentStage() throws IOException {
        ArrayList<Point> pointsToAdd = new ArrayList<>();
        boolean addTitle = true;
        for (final Integer areaId : model.keySet()) {
            boolean addAreaTitle = true;
            final LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> areamodel = model.get(areaId);
            final LinkedHashMap<Integer, LinkedHashMap<Integer, Point>> areaevents = events_targets.get(areaId);
            int reached = 0;
            for (final Integer subareaId : areamodel.keySet()) {
                final LinkedHashMap<Integer, Double> subareamodel = areamodel.get(subareaId);
                final LinkedHashMap<Integer, Point> subareevents = (areaevents !=null) ? areaevents.get(subareaId) : null;
                pointsToAdd.clear();
                for (final Integer target : subareamodel.keySet()) {
                    if (2.5 != subareamodel.get(target)) continue;
                    Point point = (subareevents!= null) ? subareevents.get(target) : null;
                    if (point == null) point = new Point((String) cacheManager.targets.get(target)[0], 1);
                    if (point.points >= 2) reached++;
                    pointsToAdd.add(point);
                }
                if (pointsToAdd.size() > 0) {
                    if (addTitle) {
                        position = addTitle(page, stages[stageId] + " TARGETS", Math.round(margin),
                                position-30, fontMaintTitleSize, false) + 30;
                        addTitle = false;
                    }
                    if (addAreaTitle) {
                        drawNextTable((String)cacheManager.subareasTarget.get(subareaId)[0], pointsToAdd.toArray(new Point[0]),
                                cacheManager.areasTarget.get(areaId)[0]);
                        addAreaTitle = false;
                    } else
                        drawNextTable((String)cacheManager.subareasTarget.get(subareaId)[0], pointsToAdd.toArray(new Point[0]),
                                null);
                }
            }
            addPercentageToTitle(Math.round(reached * 100 / (double) getAreaSize(areamodel, 2.5)) + "%");
        }
    }

    private void checkNextStage() throws IOException {
        ArrayList<Point> pointsToAdd = new ArrayList<>();
        boolean addTitle = true;
        for (final Integer areaId : model.keySet()) {
            boolean addAreaTitle = true;
            final LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> areamodel = model.get(areaId);
            final LinkedHashMap<Integer, LinkedHashMap<Integer, Point>> areaevents = events_targets.get(areaId);
            int reached = 0;
            for (final Integer subareaId : areamodel.keySet()) {
                final LinkedHashMap<Integer, Double> subareamodel = areamodel.get(subareaId);
                final LinkedHashMap<Integer, Point> subareevents = (areaevents !=null) ? areaevents.get(subareaId) : null;
                pointsToAdd.clear();
                for (final Integer target : subareamodel.keySet()) {
                    if (5 != subareamodel.get(target)) continue;
                    Point point = (subareevents!= null) ? subareevents.get(target) : null;
                    if (point != null) {
                        pointsToAdd.add(point);
                        reached++;
                    }
                }
                if (pointsToAdd.size() > 0) {
                    if (addTitle) {
                        position = addTitle(page, "YEAR 1 TARGETS ALREADY REACHED", Math.round(margin),
                                position-30, fontMaintTitleSize, false) + 30;
                        addTitle = false;
                    }
                    if (addAreaTitle) {
                        drawNextTable((String)cacheManager.subareasTarget.get(subareaId)[0], pointsToAdd.toArray(new Point[0]),
                                cacheManager.areasTarget.get(areaId)[0]);
                        addAreaTitle = false;
                    } else
                        drawNextTable((String)cacheManager.subareasTarget.get(subareaId)[0], pointsToAdd.toArray(new Point[0]),
                                null);
                }
            }
            addPercentageToTitle(Math.round(reached * 100 / (double) getAreaSize(areamodel, 5)) + "%");
        }
    }

    private void drawNextTable(String title, Point[] pointsToAdd,
                               String areaTitle) throws IOException {
        Table table = drawTableWithPoints(new int[]{Math.round(page.getMediaBox().getWidth()-2*margin-80*3),80,80,80},
                new String[]{title, "Emerging", "Expected", "Exceeding"},
                pointsToAdd, 9);
        int nextPos = position - Math.round(table.getHeight());

        int limit = (areaTitle!=null)?90:30;
        if (nextPos < limit) nextPage();
        if (areaTitle != null) {
            position -= 30;
            titleData[0] = position;
            titleData[1] = doc.getPages().indexOf(page);
            position = addTitle(page, areaTitle, Math.round(margin), position, 12, false) + 20;
        }
        position = addTable(page,Math.round(margin), position, table);
    }

    private void addPercentageToTitle(String percentange) throws IOException {
        if (titleData[0]==0 && titleData[1]==0) return;
        PDPage temp = doc.getPage(titleData[1]);
        int x = Math.round(temp.getMediaBox().getWidth() - margin - 14 * font.getStringWidth(percentange) / 1000);
        int fontTitleSize = 12;
        addTitle(temp, percentange, x, titleData[0], fontTitleSize, false);
        titleData[0] = 0;
        titleData[1] = 0;
    }

    private void nextPage() {
        page = createPage();
        doc.addPage(page);
        position = 800;
    }
}
