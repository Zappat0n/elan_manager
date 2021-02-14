package ui.formInternalReports.models;

import bd.BDManager;
import bd.MySet;
import bd.model.TableEvents;
import bd.model.TableStudents;
import main.ApplicationLoader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import pdfs.boxedtexts.BoxedText;
import pdfs.tables.Cell;
import pdfs.tables.Row;
import pdfs.tables.Table;
import pdfs.tables.TableDrawer;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import utils.data.RawData;

import javax.imageio.ImageIO;
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
public class InternalReportSkeleton {
    private static final String TAG = InternalReportSkeleton.class.getSimpleName();
    protected Connection co;
    protected final Date startDate;
    protected final Date endDate;
    protected final Boolean portrait;
    protected final int classroom;
    //final int[][] language = {{0,0}, {3, 4}};
    protected PDPage page;
    final int fontTitleSize = 12;
    final int fontSize = 10;
    final ArrayList<Integer> ncTargets;
    protected final PDDocument doc;
    protected PDFont font = null;
    protected String fileName;
    public final float margin = 30;
    protected int line_space;
    public java.util.Date lastReportDate;
    protected final static String[] stages ={"Early Years", "Nursery", "Reception", "Year 1", "Year 2", "Year 3",
            "Year 4", "Year 5", "Year 6", "Year 7", "Year 8"};
    final String[] stagesShort ={"EY", "Nursery", "Reception", "Y1", "Y2", "Y3", "Y4", "Y5", "Y6", "Y7", "Y8"};
    protected Integer stageId;
    protected BufferedImage logo;
    protected Integer position;

    protected InternalReportSkeleton(Connection co, Date startDate, Date endDate, int classroom, Boolean portrait) {
        this.co = co;
        this.startDate = startDate;
        this.endDate = endDate;
        this.portrait = portrait;
        this.classroom = classroom;
        fileName = getFileName();
        try {
            logo = ImageIO.read(getClass().getResourceAsStream("logo.png"));
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        }
        doc = new PDDocument();
        try {
            font = PDType1Font.HELVETICA;
                    //PDType0Font.load(doc, getClass().getResourceAsStream("Verdana.ttf"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ncTargets = new ArrayList<>();
    }

    private String getFileName() {
        String name = RawData.classrooms[classroom-1].replace(" ", "-")+"_"+startDate+"_"+endDate+".pdf";
        return ApplicationLoader.settingsManager.getValue(SettingsManager.REPORTS_DIR) + name;
    }


    protected void nextPage() {
        if (portrait) {
            page = createPage();
            doc.addPage(page);
            position = 800;
        } else {
            page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            doc.addPage(page);
            position = 490;
        }
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

    void createLegend(PDPage page, PDFont font) {
        Table.TableBuilder tableBuilder =new Table.TableBuilder();
        tableBuilder.addColumnOfWidth(20);
        tableBuilder.addColumnOfWidth(120);
        tableBuilder.setFontSize(10, 8);
        tableBuilder.setFont(font);
        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        rowBuilder.add(Cell.withText("/").withAllBorders());
        rowBuilder.add(Cell.withText((ApplicationLoader.settingsManager.language==1)?"Cercano":"Close").withAllBorders());
        tableBuilder.addRow(rowBuilder.build());
        rowBuilder = new Row.RowBuilder();
        rowBuilder.add(Cell.withText("Λ").withAllBorders());
        rowBuilder.add(Cell.withText((ApplicationLoader.settingsManager.language==1)?"Conseguido":"Achived").withAllBorders());
        tableBuilder.addRow(rowBuilder.build());
        rowBuilder = new Row.RowBuilder();
        rowBuilder.add(Cell.withText("Δ").withAllBorders());
        rowBuilder.add(Cell.withText((ApplicationLoader.settingsManager.language==1)?"Sobrepasado":"Exceeded").withAllBorders());
        tableBuilder.addRow(rowBuilder.build());

        try {
            addTable(page, 520, 580, tableBuilder.build());
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        }

    }

    PDPage createPage() {
        PDPage page = new PDPage(PDRectangle.A4);
        page.setRotation(0);
        this.line_space = 10;
        return page;
    }

    public Integer addImage(PDPage page, BufferedImage image, int x, int y, String _scale) throws IOException {

        //BufferedImage image = ImageIO.read(new File(imagePath));
        PDImageXObject pdImage = LosslessFactory.createFromImage(doc,image);
                //System.out.println(new File(imagePath).getAbsolutePath());
        //PDImageXObject pdImage = PDImageXObject.createFromFile(file, doc);
        PDPageContentStream contentStream = new PDPageContentStream(doc, page,
                PDPageContentStream.AppendMode.APPEND, true, true);
        float scale = Float.parseFloat(_scale);
        float height = pdImage.getHeight()*Float.parseFloat(String.valueOf(scale));
        contentStream.drawImage(pdImage, x, y, pdImage.getWidth()*scale, height);
        contentStream.close();
        return y;
    }

    protected Integer addTitle(PDPage page, String message, int x, int y, int fontSize, Boolean bold) throws IOException {
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

    Table drawTableWithPoints(int[] columns, String[] title, Point[] pointsindex, int fontSize) {
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
        return drawTable(columns, title, data, 12, fontSize, color);
    }

    private Table drawTable(int[] columns, String[] title, String[][] data,
                            @SuppressWarnings("SameParameterValue") int fontTitleSize, int fontSize, Boolean[] color) {
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

    protected Integer addTable(PDPage page, int x, int y, Table table) throws IOException {
        PDPageContentStream contents = new PDPageContentStream(doc, page,PDPageContentStream.AppendMode.APPEND, true);
        (new TableDrawer(contents, table, x, y)).draw();
        contents.close();
        return Math.round(y - Math.round(table.getHeight()));
    }

    LinkedHashMap<Integer, LinkedHashMap<Integer[], LinkedHashMap<Integer, Double>>> loadOutcomes() {
        LinkedHashMap<Integer, LinkedHashMap<Integer[], LinkedHashMap<Integer, Double>>> subarea = new LinkedHashMap<>();
        LinkedHashMap<Integer[], LinkedHashMap<Integer, Double>> periods;
        LinkedHashMap<Integer, Double> outcomes;

        for (Integer outcomeId : ApplicationLoader.cacheManager.outcomes.keySet()) {
            Object[] outcome = ApplicationLoader.cacheManager.outcomes.get(outcomeId); //name,nombre,subarea,start_month,end_month
            int subareaId = (int)outcome[2];

            periods = subarea.computeIfAbsent(subareaId, k -> new LinkedHashMap<>());

            Integer[] period = {(int)outcome[3], (int)outcome[4]};
            if (period[0] == 0) continue;

            outcomes = periods.computeIfAbsent(period, k -> new LinkedHashMap<>());
            outcomes.put(outcomeId, ApplicationLoader.cacheManager.getOutcomeYear((int)outcome[4]));
        }
        return subarea;
    }

    LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>> loadModel(Double[] years) {
        LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>> area = new LinkedHashMap<>();
        LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> subarea;
        LinkedHashMap<Integer, Double> items;
        for (Double year : years) {
            LinkedHashMap<Integer, ArrayList<Integer>> targetsperSubarea = ApplicationLoader.cacheManager.targetsPerYearAndSubarea.get(year);
            for (Object areaId : ApplicationLoader.cacheManager.areasTargetPerStage.get(year)) {
                Integer _areaId = (Integer) areaId;
                ArrayList<Integer> subareas = ApplicationLoader.cacheManager.subareasTargetPerArea.get(_areaId);
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
