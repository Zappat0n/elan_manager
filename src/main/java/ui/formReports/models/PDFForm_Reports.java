package ui.formReports.models;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEvents;
import bd.model.TableStudents;
import pdfs.boxedtexts.BoxedText;
import pdfs.tables.Cell;
import pdfs.tables.Row;
import pdfs.tables.Table;
import pdfs.tables.TableDrawer;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import utils.data.RawData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

/**
 * Created by angel on 8/04/17.
 */
public class PDFForm_Reports {
    private static final String TAG = PDFForm_Reports.class.getSimpleName();
    //final int[][] language = {{0,0}, {3, 4}};
    final CacheManager cacheManager;
    final BDManager bdManager;
    final SettingsManager settingsManager;
    final int fontTitleSize = 12;
    final int fontSize = 10;
    final ArrayList<Integer> ncTargets;
    public final PDDocument doc;
    PDFont font = null;
    public String fileName;
    public final float margin = 30;
    int line_space;
    final Integer studentId;
    public final String studentName;
    final Integer classroom;
    //ArrayList<Point> points;
    java.util.Date birthDate;
    public final java.util.Date reportDate;
    public final java.util.Date changeDate;
    public java.util.Date lastReportDate;
    final static String[] stages ={"Early Years", "Nursery", "Reception", "Year 1", "Year 2", "Year 3", "Year 4", "Year 5", "Year 6", "Year 7", "Year 8"};
    final String[] stagesShort ={"EY", "Nursery", "Reception", "Y1", "Y2", "Y3", "Y4", "Y5", "Y6", "Y7", "Y8"};
    final String reportType;
    Integer stageId;
    Integer month;
    Connection co;
    Boolean connectedHere = false;
    final BufferedImage logo;
    final LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, Point>>> events_targets;
    final LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, Point>>> events_outcomes;
    final SortedMap<Date, String> notes;


    PDFForm_Reports(BDManager bdManager, CacheManager cacheManager, SettingsManager settingsManager, Connection co,
                    Integer studentId, Integer classroom, java.util.Date reportDate, String reportType,
                    BufferedImage logo) {
        this.logo = logo;
        this.cacheManager = cacheManager;
        this.bdManager = bdManager;
        this.reportType = reportType;
        this.settingsManager = settingsManager;
        this.studentId = studentId;
        studentName = (String)cacheManager.students.get(studentId)[0];
        this.classroom = (classroom!=null) ? classroom : cacheManager.getClassroomId(studentId);
        this.reportDate = reportDate;
        this.changeDate = null;
        doc = new PDDocument();
        notes = new TreeMap<>();
        try {
            font = //PDType1Font.HELVETICA;
                    PDType0Font.load(doc, getClass().getResourceAsStream("Verdana.ttf"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //PDType1Font.HELVETICA_BOLD;

        //points = new ArrayList<>();
        events_targets = new LinkedHashMap<>();
        events_outcomes = new LinkedHashMap<>();
        ncTargets = new ArrayList<>();
        initialize(co);
    }

    private void initialize(Connection co) {
        if (co == null) {
            connectedHere = true;
            this.co = bdManager.connect();
        } else this.co = co;
        MySet set = bdManager.getValues(co, BDManager.tableStudents, TableStudents.id + "=" + studentId);
        if (set.next()) {
            birthDate = set.getDate(TableStudents.birth_date);
        }
        calculateStage(cacheManager.getClassroomId(studentId));
        fileName = getFileName();
    }

    void calculateStage(int classroom) { //1 comundi, 2 cdb, 3 taller
        Integer years = getYears();
        if (years == null || years < 3) stageId = 0;
        else stageId = years - 2;
    }

    private Integer getYears() {
        if (birthDate == null) return null;
        int startMonth = Calendar.DECEMBER;
        int day = 31;
        Calendar now = Calendar.getInstance();
        now.setTime(reportDate != null ? reportDate : new java.util.Date());
        Calendar start = Calendar.getInstance();
        if (now.get(Calendar.MONTH)<Calendar.SEPTEMBER) start.set(now.get(Calendar.YEAR)-1, startMonth, day);
        else start.set(now.get(Calendar.YEAR), startMonth, day);

        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);
        month = birth.get(Calendar.MONTH)+1;
        return Period.between(
                LocalDate.of(birth.get(Calendar.YEAR), month, birth.get(Calendar.DAY_OF_MONTH)),
                LocalDate.of(start.get(Calendar.YEAR), start.get(Calendar.MONTH) + 1, start.get(Calendar.DAY_OF_MONTH))).getYears();
    }

    public String createDocument(){return fileName;}

    Integer createHeadPage(PDPage page, String title, String studentName, Long dateOfReport,
                           Long dateOfBirth, Long dateOfChange) throws IOException {
        Integer position = 750;
        position = addImage(page, logo, 492 - Math.round(margin), position + 32, "0.2");
        position = addTitle(page, title, Math.round(margin), position, 18, false) - 5;
        addTitle(page, "Pupil: " + studentName, Math.round(margin), position, 12, false);
        position = addTitle(page, "Date of report: " + new Date(dateOfReport), 400, position, 12, false);
        if (dateOfBirth != null) {
            addTitle(page, "Date of birth: " + new Date(dateOfBirth), Math.round(margin), position, 12, false);
            return addTitle(page, "Date of change to FS: " + new Date(dateOfChange), 360, position, 12, false) + 10;
        } else return position +10;
    }

    void createLegend(PDPage page, int positionX, int positionY, PDFont font, int fontTitleSize, int fontSize) {
        Table.TableBuilder tableBuilder =new Table.TableBuilder();
        tableBuilder.addColumnOfWidth(20);
        tableBuilder.addColumnOfWidth(120);
        tableBuilder.setFontSize(fontTitleSize, fontSize);
        tableBuilder.setFont(font);
        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        rowBuilder.add(Cell.withText("/").withAllBorders());
        rowBuilder.add(Cell.withText((settingsManager.language==1)?"Cercano":"Close").withAllBorders());
        tableBuilder.addRow(rowBuilder.build());
        rowBuilder = new Row.RowBuilder();
        rowBuilder.add(Cell.withText("Λ").withAllBorders());
        rowBuilder.add(Cell.withText((settingsManager.language==1)?"Conseguido":"Achived").withAllBorders());
        tableBuilder.addRow(rowBuilder.build());
        rowBuilder = new Row.RowBuilder();
        rowBuilder.add(Cell.withText("Δ").withAllBorders());
        rowBuilder.add(Cell.withText((settingsManager.language==1)?"Sobrepasado":"Exceeded").withAllBorders());
        tableBuilder.addRow(rowBuilder.build());

        try {
            addTable(page, positionX, positionY, tableBuilder.build());
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        }

    }

    PDPage createPage(int line_space) {
        PDPage page = new PDPage(PDRectangle.A4);
        page.setRotation(0);
        this.line_space = line_space;
        return page;
    }

    public Integer addImage(PDPage page, BufferedImage image, int x, int y, String _scale) throws IOException {

        //BufferedImage image = ImageIO.read(new File(imagePath));
        PDImageXObject pdImage = LosslessFactory.createFromImage(doc,image);
        PDPageContentStream contentStream = new PDPageContentStream(doc, page,
                PDPageContentStream.AppendMode.APPEND, true, true);
        float scale = Float.parseFloat(_scale);
        float height = pdImage.getHeight()*Float.parseFloat(String.valueOf(scale));
        contentStream.drawImage(pdImage, x, y, pdImage.getWidth()*scale, height);
        contentStream.close();
        return y;
    }

    Integer addTitle(PDPage page, String message, int x, int y, int fontSize, Boolean bold) throws IOException {
        PDPageContentStream contents = new PDPageContentStream(doc, page,
                PDPageContentStream.AppendMode.APPEND, true);
        contents.beginText();
        contents.setFont(!bold ? font : PDType1Font.HELVETICA_BOLD, fontSize);
        contents.setStrokingColor(Color.black);
        contents.setNonStrokingColor(Color.black);
        contents.newLineAtOffset(x, y);
        contents.showText(message);
        contents.endText();
        contents.close();
        return y - fontSize - line_space;
    }

    Integer addBoxedText(PDPage page, float width, BoxedText bt, int x, int y, PDFont pdfFont, int fontSize) throws IOException {
        PDPageContentStream contents = new PDPageContentStream(doc, page,PDPageContentStream.AppendMode.APPEND, true);
        contents.beginText();
        contents.setFont(pdfFont, fontSize);
        contents.newLineAtOffset(x+ bt.innerMargin, y);
        if (bt.lines.size()>0)
            for (String line: bt.lines) {
                float charSpacing = 0;
                if (line.length() > 1) {
                    float size = fontSize * pdfFont.getStringWidth(line) / 1000;
                    float free = bt.lineWidth - size;
                    if (free > 0) {
                        charSpacing = free / (line.length() - 1);
                    }
                }
                if (!bt.shortLines.contains(bt.lines.indexOf(line))) contents.setCharacterSpacing(charSpacing);
                else contents.setCharacterSpacing(0);
                contents.showText(line);
                contents.newLineAtOffset(0, -bt.leading);
            } else {
                bt.lines.add("");
                bt.lines.add("");
                bt.lines.add("");
                bt.lines.add("");
            }

        contents.endText();
        float height = (bt.lines.size() + 1) * bt.leading;
        contents.setStrokingColor(Color.black);
        contents.addRect(x, y + bt.leading, width, -height);
        contents.stroke();
        contents.close();
        return y - Math.round(height - bt.leading) - line_space;
    }

    Table drawTableWithPoints(int[] columns, String[] title, Point[] pointsindex, int fontTitleSize, int fontSize) {
        String[][] data = null;
        Boolean[] color = null;
        if (pointsindex != null) {
            color = new Boolean[pointsindex.length];
            data = new String[pointsindex.length][columns.length];
            for (int i = 0; i < pointsindex.length; i++) {
                Point point = pointsindex[i];
                data[i][0] = point.name;
                double value = (double) point.points / (double) point.size;
                if (value == 0) {
                    data[i][1] = "";
                    data[i][2] = "";
                    data[i][3] = "";
                } else if (value <= 1) {
                    data[i][1] = "/";
                    data[i][2] = "/";
                    data[i][3] = "/";
                } else if (value <= 2) {
                    data[i][1] = "Λ";
                    data[i][2] = "Λ";
                    data[i][3] = "Λ";
                } else {
                    data[i][1] = "Δ";
                    data[i][2] = "Δ";
                    data[i][3] = "Δ";
                }
                color[i] = point.isRed;
            }
        }
        return drawTable(columns, title, data, fontTitleSize, fontSize, color);
    }

    private Table drawTable(int[] columns, String[] title, String[][] data, int fontTitleSize, int fontSize,
                            Boolean[] color) {
        Table.TableBuilder tableBuilder = new Table.TableBuilder();
        for (int width : columns) tableBuilder.addColumnOfWidth(width);
        tableBuilder.setFontSize(fontTitleSize, fontSize);
        tableBuilder.setFont(font);

        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        for (String titleCell : title) {
            rowBuilder.add(Cell.withText(titleCell)
                    .setHorizontalAlignment((data != null) ?
                            Cell.HorizontalAlignment.CENTER : Cell.HorizontalAlignment.LEFT)
                    .withAllBorders());
        }
        tableBuilder.addRow(rowBuilder.build());
        if (data != null) {
            for(String[] line : data) {
                rowBuilder = new Row.RowBuilder();
                int i = 0;
                Boolean red = color[i];
                for (String cell : line) {
                    Cell c = Cell
                            .withText(cell)
                            .setHorizontalAlignment(i == 0 ? Cell.HorizontalAlignment.LEFT : Cell.HorizontalAlignment.CENTER)
                            .withAllBorders();
                    c.setTextColor(!red || i == 0 ? Color.black : Color.red);
                    rowBuilder.add(c);
                    i++;
                }
                tableBuilder.addRow(rowBuilder.build());
            }

        }
        return tableBuilder.build();
    }

    void addTable(PDPage page, int x, int y, Table table) throws IOException {
        PDPageContentStream contents = new PDPageContentStream(doc, page,PDPageContentStream.AppendMode.APPEND, true);

        (new TableDrawer(contents, table, x, y)).draw();
        contents.close();
        table.getHeight();
    }

    LinkedHashMap<Integer, LinkedHashMap<Integer[], LinkedHashMap<Integer, Double>>> loadOutcomes() {
        LinkedHashMap<Integer, LinkedHashMap<Integer[], LinkedHashMap<Integer, Double>>> subarea = new LinkedHashMap<>();
        LinkedHashMap<Integer[], LinkedHashMap<Integer, Double>> periods;
        LinkedHashMap<Integer, Double> outcomes;

        for (Integer outcomeId : cacheManager.outcomes.keySet()) {
            Object[] outcome = cacheManager.outcomes.get(outcomeId); //name,nombre,subarea,start_month,end_month
            int subareaId = (int)outcome[2];

            periods = subarea.computeIfAbsent(subareaId, k -> new LinkedHashMap<>());

            Integer[] period = {(int)outcome[3], (int)outcome[4]};
            if (period[0] == 0) continue;

            outcomes = periods.computeIfAbsent(period, k -> new LinkedHashMap<>());
            outcomes.put(outcomeId, cacheManager.getOutcomeYear((int)outcome[4]));
        }
        return subarea;
    }

    LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>> loadModel(Double[] years) {
        LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>> area = new LinkedHashMap<>();
        LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> subarea;
        LinkedHashMap<Integer, Double> items;
        for (Double year : years) {
            LinkedHashMap<Integer, ArrayList<Integer>> targetsperSubarea = cacheManager.targetsperyearandsubarea.get(year);
            for (Object areaId : RawData.areasTargetperStage.get(year)) {
                Integer _areaId = (Integer) areaId;
                ArrayList<Integer> subareas = cacheManager.subareasTargetperarea.get(_areaId);
                if (subareas != null) {
                    subarea = area.get(_areaId);
                    if (subarea == null) subarea = new LinkedHashMap<>();
                    for (Integer subareaId : subareas) {
                        ArrayList<Integer> targets = targetsperSubarea.get(subareaId);
                        items = subarea.get(subareaId);
                        if (targets != null) {
                            if (items == null) items = new LinkedHashMap<>();
                            for (Integer target : targetsperSubarea.get(subareaId)) {
                                items.put(target, year);
                            }
                            subarea.put(subareaId, items);
                        } else if (items == null) subarea.put(subareaId, new LinkedHashMap<>());
                    }
                    area.put(_areaId, subarea);
                }
            }
        }
        return area;
    }

    public int getAreaSize(LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> area, double year) {
        int count = 0;
        for (LinkedHashMap<Integer, Double> sub : area.values()) {
            for (Double targetyear : sub.values()) {
                if (targetyear == year) count++;
            }
        }
        return count;
    }

    void loadEvents(Connection co, Integer studentId) {
        MySet set = bdManager.getValues(co, BDManager.tableEvents, TableEvents.student + "=" + studentId);
        HashMap<Point, Date> pointToDate = new HashMap<>();
        while (set.next()) {
            Integer event_type = set.getInt(TableEvents.event_type);
            switch (event_type) {
                case 2 -> processTarget(true, pointToDate, 2, set.getInt(TableEvents.event_id),
                        set.getDate(TableEvents.date));
                case 4 -> processTarget(true, pointToDate, 1, set.getInt(TableEvents.event_id),
                        set.getDate(TableEvents.date));
                case 5 -> processTarget(true, pointToDate, 3, set.getInt(TableEvents.event_id),
                        set.getDate(TableEvents.date));
                case 9 -> processTarget(false, pointToDate, 1, set.getInt(TableEvents.event_id),
                        set.getDate(TableEvents.date));
                case 10 -> processTarget(false, pointToDate, 2, set.getInt(TableEvents.event_id),
                        set.getDate(TableEvents.date));
                case 11 -> processTarget(false, pointToDate, 3, set.getInt(TableEvents.event_id),
                        set.getDate(TableEvents.date));
                case 12 -> notes.put(set.getDate(TableEvents.date), set.getString(TableEvents.notes));
                case 15 -> {
                    java.util.Date date = set.getDate(TableEvents.date);
                    if (lastReportDate == null || date.after(lastReportDate)) lastReportDate = date;
                }
            }
        }
        if (lastReportDate != null)
            for (Point point : pointToDate.keySet()) {
                if (pointToDate.get(point).after(lastReportDate)) point.setRed();
            }
    }

    private void processTarget(Boolean isTarget, HashMap<Point, Date> pointToDate, int event_points, int targetId, Date date){
        try {
            Object[] target = (isTarget) ? cacheManager.targets.get(targetId) : cacheManager.outcomes.get(targetId);//name,subarea,year, nombre
            LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, Point>>> events =
                    (isTarget) ? events_targets : events_outcomes;

            Integer subareaId = (int)target[2];//name, nombre, subarea, year
            Integer areaId = cacheManager.targetsubareaarea.get(subareaId);
            LinkedHashMap<Integer, LinkedHashMap<Integer, Point>> subarea;
            if (!events.containsKey(areaId)) {
                subarea = new LinkedHashMap<>();
                events.put(areaId, subarea);
            } else subarea = events.get(areaId);

            LinkedHashMap<Integer, Point> items;
            if (!subarea.containsKey(subareaId)) {
                items = new LinkedHashMap<>();
                subarea.put(subareaId, items);
            } else items = subarea.get(subareaId);

            if (!items.containsKey(targetId)) {
                Point point = new Point((String) target[settingsManager.language], 1);
                point.setPoints(event_points, date);
                pointToDate.put(point, date);
                items.put(targetId, point);
            } else {
                Point point = items.get(targetId);
                if (event_points > point.points) point.setPoints(event_points, date);
                if (pointToDate.get(point).before(date)) pointToDate.put(point, date);
            }
        } catch (Exception ex) {
            MyLogger.d(TAG, "Error with target:" + targetId);
            MyLogger.e(TAG, ex);
        }
    }

    private String getFileName() {
        String stage = stagesShort[stageId];
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

        return settingsManager.getValue(SettingsManager.REPORTS_DIR) +
                formatter.format(reportDate) + "-" + reportType + "-" + stage + " " + studentName + ".pdf";
    }


    public static class Point {
        final String name;
        final int size;
        int points = 0;
        boolean isRed = false;
        Date date;
        public Point(String name, int size) {
            this.name = name;
            this.size = size;
        }

        public void addPoints(int points) {
            this.points = this.points + points;        }
        public void setPoints(int points, Date date) {
            this.points = points; this.date = date;
        }
        public void setRed() {
            isRed = true;
        }
    }
}
