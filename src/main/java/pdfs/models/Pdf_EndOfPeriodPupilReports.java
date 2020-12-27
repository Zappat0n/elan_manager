package pdfs.models;

import bd.BDManager;
import bd.model.TableEvents;
import pdfs.boxedtexts.BoxedText;
import pdfs.tables.Table;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by angel on 8/04/17.
 */
public class Pdf_EndOfPeriodPupilReports extends PDFForm_Reports {
    private static final String TAG = Pdf_EndOfPeriodPupilReports.class.getSimpleName();
    private PDPage page;
    private Integer position;
    private double year;
    private final HashMap<Integer, String> notes = new HashMap<>();
    private LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>> model;

    public Pdf_EndOfPeriodPupilReports(BDManager bdManager, CacheManager cacheManager, SettingsManager settingsManager,
                                       Integer studentId, String studentName, Integer classroom, Date reportDate,
                                       Date changeDate, BufferedImage logo, String mainTitle, double yearToLoad) {
        super(bdManager, cacheManager, settingsManager, null, "End", studentId, classroom, reportDate, changeDate, logo);
        try {
            year = yearToLoad;
            co = bdManager.connect();
            nextPage();
            position = createHeadPage(page, mainTitle, studentName,
                    reportDate.getTime(), birthDate.getTime(), changeDate.getTime());
            model = loadModel(new Double[]{year});
            loadEndEvents(co, studentId);
            if (year == 2.5) year = 0;
            checkCurrentStage();
            doc.save(fileName);
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            BDManager.closeQuietly(co);
            try {
                doc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void nextPage() {
        page = createPage();
        doc.addPage(page);
        position = 800;
    }

    private void checkCurrentStage() throws IOException {
        ArrayList<Point> pointsToAdd = new ArrayList<>();
        int i = 1;
        for (final Integer areaId : model.keySet()) {
            final LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> areamodel = model.get(areaId);
            final LinkedHashMap<Integer, LinkedHashMap<Integer, Point>> areaevents = events_targets.get(areaId);
            pointsToAdd.clear();
            for (final Integer subareaId : areamodel.keySet()) {
                final LinkedHashMap<Integer, Double> subareamodel = areamodel.get(subareaId);
                final LinkedHashMap<Integer, Point> subareevents = (areaevents !=null) ? areaevents.get(subareaId) : null;
                Point point = new Point(cacheManager.subareasTarget.get(subareaId)[0], subareamodel.size());
                for (final Integer target : subareamodel.keySet()) {
                    Point p= (subareevents!= null) ? subareevents.get(target) : null;
                    if (p != null) point.addPoints(p.points);
                }
                pointsToAdd.add(point);
            }
            double noteId;
            if (year == 0 && areaId == 11) noteId = 10d;
            else noteId= year*100+areaId;
            drawNextTableAndBoxedTextAndSub(i + "." + cacheManager.areasTarget.get(areaId)[0].toUpperCase(), pointsToAdd.toArray(new Point[0]),
                    notes.get((int) noteId), areaId, null);
            i++;
        }

        double noteId= year*100+91;
        drawNextTableAndBoxedTextAndSub(i+". NEXT STEPS", null,
                notes.get((int) noteId), 91,
                "Guide.......................................................................................    Signed.........................................");
        noteId= year*100+92;
        drawNextTableAndBoxedTextAndSub(i+1+". PARENT / TUTOR COMMENTS", null,
                notes.get((int) noteId), 92,
                "Parent/Tutor............................................................................    Signed.........................................");
    }

    private void drawNextTableAndBoxedTextAndSub(String title, Point[] pointsToAdd,
                                                 String boxedText, Integer areaId, String sub) throws IOException {
        Table table = drawTableWithPoints(
                (pointsToAdd!=null) ? new int[]{Math.round(page.getMediaBox().getWidth()-2*margin-80*3),80,80,80} :
                        new int[]{Math.round(page.getMediaBox().getWidth()-2*margin)},
                (pointsToAdd!=null) ? new String[]{title, "Emerging", "Expected", "Exceeding"} : new String[]{title},
                pointsToAdd, 12, 10);
        int height;
        BoxedText bt = null;
        if (year == 0 && areaId == 10) {
            height = 0;
        } else {
            bt = new BoxedText(boxedText, 10, page.getMediaBox().getWidth()-2*margin, PDType1Font.HELVETICA);
            height = Math.round(bt.getHeight());
        }

        int subheight = (sub == null) ? 0 : Math.round(table.getFontHeight() + line_space * 3);

        int nextPos = position - Math.round(table.getHeight()) - height - line_space *3 -subheight;
        if (nextPos < 30) nextPage();
        position = addTable(page,Math.round(margin), position, table);
        position = position - line_space *3;

        if (bt != null) position = addBoxedText(page, page.getMediaBox().getWidth()-2*margin, bt,
                Math.round(margin), position, PDType1Font.HELVETICA, 10);
        if (sub != null)position = addTitle(page, sub, Math.round(margin), position - line_space*3, 10, false);
    }

    void loadEndEvents(
            Connection co, Integer studentId) throws SQLException {
        String query = "SELECT * FROM Events WHERE student=" + studentId;
        Statement st = co.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            Integer event_points = null;
            int type = rs.getInt(TableEvents.event_type);
            int targetId = rs.getInt(TableEvents.event_id);
            switch (type) {
                case 2 -> event_points = 2;
                case 4 -> event_points = 1;
                case 5 -> event_points = 3;
                case 99 -> notes.put(targetId, rs.getString(TableEvents.notes));
            }
            if (event_points == null) continue;

            Integer subareaId = cacheManager.getTargetSubarea(targetId);
            Integer areaId = cacheManager.targetsubareaarea.get(subareaId);
            LinkedHashMap<Integer, LinkedHashMap<Integer, Point>> subarea;
            if (!events_targets.containsKey(areaId)) {
                subarea = new LinkedHashMap<>();
                events_targets.put(areaId, subarea);
            } else subarea = events_targets.get(areaId);

            LinkedHashMap<Integer, Point> items;
            if (!subarea.containsKey(subareaId)) {
                items = new LinkedHashMap<>();
                subarea.put(subareaId, items);
            } else items = subarea.get(subareaId);

            Integer event_id = rs.getInt(TableEvents.event_id);
            java.sql.Date date = rs.getDate(TableEvents.date);
            Point point = items.get(event_id);
            if (point == null) {
                point = new Point(cacheManager.getTargetName(targetId), 1);
                point.setPoints(event_points, date);
                items.put(event_id, point);
            } else {
                if (event_points > point.points) point.setPoints(event_points, date);
            }
        }
        BDManager.closeQuietly(rs);
        BDManager.closeQuietly(st);
    }
}
