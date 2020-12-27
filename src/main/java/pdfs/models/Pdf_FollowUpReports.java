package pdfs.models;

import bd.BDManager;
import pdfs.boxedtexts.BoxedText;
import pdfs.tables.Cell;
import pdfs.tables.Row;
import pdfs.tables.Table;
import ui.formChildData.ChildDataFormListItem;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

public class Pdf_FollowUpReports extends PDFForm_Reports {
    private static final String TAG = Pdf_FollowUpReports.class.getSimpleName();
    private final int fontTitleSize = 10;
    private final int fontSize = 8;
    private final int line_space = 8;
    private final int[] titleData = new int[2];
    private final int bottom_limit = 20;
    private Integer positionX;
    private Integer positionY;
    private PDPage page;
    private Integer months;
    Boolean left = true;
    private LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>> model;
    private LinkedHashMap<Integer, LinkedHashMap<Integer[], LinkedHashMap<Integer, Double>>> outcomes;
    Boolean closeco = false;
    int available_rows;
    int current_rows;
    int lang;
    DefaultListModel log;

    public Pdf_FollowUpReports(BDManager bdManager, CacheManager cacheManager, SettingsManager settingsManager,
                               Connection connection, Integer studentId, Integer classroom, Date reportDate,
                               Date changeDate, BufferedImage logo, Boolean addComment, DefaultListModel log) {
        super(bdManager, cacheManager, settingsManager, connection, "Targets", studentId, classroom, reportDate, changeDate, logo);

        try {
            if (connection == null) {co = bdManager.connect(); closeco = true; } else co = connection;
            LocalDate d1 = reportDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate d2 = ((java.sql.Date) birthDate).toLocalDate();
            months = (int) ChronoUnit.MONTHS.between(d2, d1);
            page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            doc.addPage(page);
            positionY = Math.round(PDRectangle.A4.getWidth() - margin);
            positionX = Math.round(margin);
            lang = settingsManager.language;
            positionY = createHeadPage(page, ((lang==1)?"Informe trimestral de ":"End of Term Report for ") +
                            stages[stageId], studentName, reportDate.getTime());
            createBody();
            if (addComment) addCommentary();
            doc.save(fileName);
        } catch (Exception ex) {
            MyLogger.e(TAG, ex);
        } finally {
            try {
                log.insertElementAt("Targets report created: " + fileName, 0);
                doc.close();
                if (closeco) co.close();
            } catch (Exception e) {
                MyLogger.e(TAG, e);
                log.insertElementAt(e.getMessage(), 0);
            }
        }
    }

    private void addCommentary() throws IOException {
        if (notes.size() == 0) return;
        LocalDate date = notes.lastKey().toLocalDate();
        LocalDate now = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        long days = ChronoUnit.DAYS.between(now, date);
        if (days > 30) return;
        String note = notes.get(notes.lastKey());

        //String text = "Durante estos meses hemos prestado especial atención a su adaptación, la cual ha sido muy positiva.\n" +
        //        "A través del trabajo diario con los materiales del ambiente y otras actividades, hemos observado mayor independencia y autonomía. ";
        BoxedText bt = new BoxedText(note, fontSize, 380, font);
        int height = line_space * 5 + Math.round(bt.getHeight());
        if (positionY - height < bottom_limit) nextPage();
        positionY = addTitle(page, (lang==1)?"COMENTARIO GENERAL":"COMMENT", positionX, positionY - line_space * 3, fontTitleSize, false);
        positionY = addBoxedText(page, 380, bt, positionX, positionY - line_space, font, fontSize);
    }

    private void createBody() throws IOException {
        switch (stageId) {
            case 0:
                outcomes = loadOutcomes();
                model = loadModel(new Double[]{2.5});
                loadEvents(co, studentId);
                checkCurrentStage(2.5);
                //checkNextStage(5);
                break;
            case 1: case 2:
                outcomes = loadOutcomes();
                model = loadModel(new Double[]{2.5, 5d});
                loadEvents(co, studentId);
                checkCurrentStage(5);
                //checkNextStage(5);
                break;
            case 3:
                outcomes = loadOutcomes();
                model = loadModel(new Double[]{5d, 6d});
                loadEvents(co, studentId);
                checkCurrentStage(6);
                //checkNextStage(5);
                break;
            case 4:
                model = loadModel(new Double[]{6d, 7d});
                loadEvents(co, studentId);
                //checkPriorStage(6);
                checkCurrentStage(7);
                //checkNextStage(8);
                break;
            case 5:
                model = loadModel(new Double[]{7d, 8d});
                loadEvents(co, studentId);
                //checkPriorStage(7);
                checkCurrentStage(8);
                //checkNextStage(9);
                break;
            case 6:
                model = loadModel(new Double[]{8d, 9d});
                loadEvents(co, studentId);
                //checkPriorStage(8);
                checkCurrentStage(9);
                break;
            case 7:
                model = loadModel(new Double[]{9d, 10d});
                loadEvents(co, studentId);
                //checkPriorStage(8);
                checkCurrentStage(10);
                break;
            case 8:
                model = loadModel(new Double[]{10d, 11d});
                loadEvents(co, studentId);
                //checkPriorStage(8);
                checkCurrentStage(11);
                break;
            case 9:
                model = loadModel(new Double[]{10d, 11d});
                loadEvents(co, studentId);
                //checkPriorStage(8);
                checkCurrentStage(11);
                break;
            default:
                return;
        }
    }

    private void nextPage() {
        positionY = (!(doc.getNumberOfPages() == 1 && left)) ? Math.round(PDRectangle.A4.getWidth() - margin) : 518;
        if (left) {
            positionX = 400 + Math.round(margin);
            if (positionY == 518) createLegend(page,
                    font);
        } else {
            positionX = Math.round(margin);
            page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            doc.addPage(page);
        }
        left = !left;
    }

    Integer createHeadPage(PDPage page, String title, String studentName, Long dateOfReport) throws IOException {
        addImage(page, logo, 400 - logo.getWidth()/8 - positionX, positionY - logo.getHeight()/16, "0.2");
        int fontMaintTitleSize = 12;
        positionY = addTitle(page, title, positionX, positionY, fontMaintTitleSize, false) -15;
        addTitle(page, ((lang==1)?"Alumno: ":"Pupil: ") + studentName, positionX, positionY, fontTitleSize, false);
        positionY = addTitle(page, ((lang==1)?"Fecha de nacimiento: ":"Date of birth: ") +
                        new java.sql.Date(birthDate.getTime()), 200 + positionX, positionY, fontTitleSize, false);
        positionY = addTitle(page, ((lang==1)?"Fecha del informe: ":"Date of report: ") + new java.sql.Date(dateOfReport),
                positionX, positionY, fontTitleSize, false);
        /*
        positionY = addTitle(page, ((lang==1)?"Informe anterior: ":"Last Report: ") +
                        (changeDate != null ? new java.sql.Date(changeDate.getTime()) : "#N/A"),
                200 + positionX, positionY, fontTitleSize, false);
        */
        return positionY;
    }

    private void checkCurrentStage(double year) throws IOException {
        ArrayList<Point> pointsToAdd = new ArrayList<>();
        LinkedHashMap<Integer[], LinkedHashMap<Integer, Double>> areaoutcomesmodel = null;
        LinkedHashMap<Integer, Point> subareaoutcomes = null;
        LinkedHashMap<String, Point[]> data = new LinkedHashMap<>();
        available_rows = 33;
        current_rows = 1;

        for (final Integer areaId : model.keySet()) {
            final LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> areamodel = model.get(areaId);
            final LinkedHashMap<Integer, LinkedHashMap<Integer, Point>> areaoutcomes = events_outcomes.get(areaId);
            final LinkedHashMap<Integer, LinkedHashMap<Integer, Point>> areatargets = events_targets.get(areaId);
            int reached = 0;

            for (final Integer subareaId : areamodel.keySet()) {
                final LinkedHashMap<Integer, Double> subareamodel = areamodel.get(subareaId);
                if (outcomes != null) {
                    areaoutcomesmodel = outcomes.get(subareaId);
                    subareaoutcomes = (areaoutcomes !=null) ? areaoutcomes.get(subareaId) : null;
                }
                final LinkedHashMap<Integer, Point> subareatargets = (areatargets !=null) ? areatargets.get(subareaId) : null;
                pointsToAdd.clear();
                if (areaoutcomesmodel != null)
                    for (Integer[] period : areaoutcomesmodel.keySet()) {
                        boolean old;
                        old = months > period[1] && year != 2.5d;
                        LinkedHashMap<Integer, Double> outcomes = areaoutcomesmodel.get(period);
                        for (Integer outcome : outcomes.keySet()) {
                            if (outcomes.get(outcome) != year) continue;
                            Point point = (subareaoutcomes!= null) ? subareaoutcomes.get(outcome) : null;
                            if (point != null) {
                                if (point.points >= 2) reached++;
                                if (!old) {
                                    point.isRed = false;
                                    pointsToAdd.add(point);
                                } else if (old && point.points < 2) {
                                    point.isRed = true;
                                    pointsToAdd.add(point);
                                }
                            } else {
                                point = new Point((String) cacheManager.outcomes.get(outcome)[settingsManager.language], 3);
                                point.isRed = old;
                                pointsToAdd.add(point);
                            }
                        }
                    }

                if (subareamodel != null)
                    for (final Integer target : subareamodel.keySet()) {
                        double targetyear = subareamodel.get(target);
                        boolean old;
                        old = year > targetyear;
                        Point point = (subareatargets!= null) ? subareatargets.get(target) : null;
                        if (point != null) {
                            if (point.points >= 2) reached++;
                            if (!old) {
                                point.isRed = false;
                                pointsToAdd.add(point);
                            } else if (old && point.points < 2) {
                                point.isRed = true;
                                pointsToAdd.add(point);
                            }
                        } else {
                            point = new Point((String) cacheManager.targets.get(target)[settingsManager.language], 3);
                            point.isRed = old;
                            pointsToAdd.add(point);
                        }
                    }
                if (pointsToAdd.size() > 0) {
                    data.put(cacheManager.subareasTarget.get(subareaId)[lang], pointsToAdd.toArray(new Point[0]));
                }
            }

            if (data.size() > 0) {
                addTable(createTable(cacheManager.areasTarget.get(areaId)[lang], data));
            }
            data.clear();
            //addPercentageToTitle(String.valueOf(Math.round(reached * 100 / (double)getAreaSize(areamodel, year))) + "%");
        }
    }

    private Table createTable(String areaTitle, LinkedHashMap<String, Point[]> data) throws IOException {
        if (current_rows > 2) current_rows += 2;

        if (available_rows - current_rows < 9) {
            nextPage();
            current_rows = 1;
            calculateAvailableRows();
        }

        Table.TableBuilder tableBuilder = createTableBuilder();
        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        rowBuilder.add(Cell.withText(areaTitle.toUpperCase())
                .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                .setBackgroundColor(Color.ORANGE)
                .withAllBorders());
        rowBuilder.add(Cell.withText("").withAllBorders().setBackgroundColor(Color.ORANGE));
        tableBuilder.addRow(rowBuilder.build());
        current_rows+=1;

        for (String title: data.keySet()) {
            Point[] points = data.get(title);
            //rows += calculateRows(points) + ((rows==0)?1:0);
            /*
            if (!checkTableSize(rows, tableBuilder, oldrows)) {
                tableBuilder = createTableBuilder();
                rows = points.length + 2;
            }*/
            rowBuilder = new Row.RowBuilder();
            rowBuilder.add(Cell.withText(title.toUpperCase())
                    .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                    .withAllBorders());
            rowBuilder.add(Cell.withText("").withAllBorders());
            current_rows+=1;

            if (available_rows - current_rows < 4) {
                tableBuilder = addCurrentTable(tableBuilder);
                current_rows = 0;
            }

            tableBuilder.addRow(rowBuilder.build());
            for (Point point: points) {
                current_rows += getNumberRows(point);
                if (current_rows >= available_rows) {
                    tableBuilder = addCurrentTable(tableBuilder);
                    current_rows = 1;
                }
                Cell cell = Cell.withText(point.name)
                        .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                        .setBackgroundColor(ChildDataFormListItem.getColor(settingsManager, point.date))
                        .withAllBorders();
                cell.setTextColor(!point.isRed ? Color.black : Color.gray);
                rowBuilder = new Row.RowBuilder();
                rowBuilder.add(cell);
                String text;
                double value = (double) point.points / (double) point.size;
                if (value == 0) {
                    text = "";
                } else if (value <= 1) {
                    text = "/";
                } else if (value <= 2) {
                    text = "Λ";
                } else {
                    text = "Δ";
                }
                cell = Cell.withText(text).withAllBorders();
                cell.setTextColor(!point.isRed ? Color.black : Color.gray);
                rowBuilder.add(cell);
                tableBuilder.addRow(rowBuilder.build());
            }
        }
        return tableBuilder.build();
    }

    private Table.TableBuilder addCurrentTable(Table.TableBuilder tableBuilder) throws IOException {
        addTable(page, positionX, positionY, tableBuilder.build());
        nextPage();
        calculateAvailableRows();
        return createTableBuilder();
    }

    private Integer getNumberRows(Point point) {
        return ((Double)(Math.floor(point.name.length() / 84)+1)).intValue();
    }

    private void calculateAvailableRows() {
        available_rows = (positionY - bottom_limit) / 15;
    }

    private Table.TableBuilder createTableBuilder() {
        Table.TableBuilder tableBuilder =new Table.TableBuilder();
        tableBuilder.addColumnOfWidth(360);
        tableBuilder.addColumnOfWidth(20);
        tableBuilder.setFontSize(fontTitleSize, fontSize);
        tableBuilder.setFont(font);
        return tableBuilder;
    }

    private Boolean checkTableSize(int newrows, Table.TableBuilder tableBuilder, int oldrows) throws IOException {
        int nextPos = Math.round(positionY -
                newrows*(font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize+7));
        if (nextPos < bottom_limit) {
            if (oldrows == 0) nextPage();
            positionY = addTable(page, positionX, positionY, tableBuilder.build());
            if (oldrows != 0) nextPage();
            return false;
        }
        return true;
    }

    private void addTable(Table table) throws IOException {
        //int nextPos = positionY - Math.round(table.getHeight());
        //if (nextPos < bottom_limit && table.getHeight() < 520) nextPage();
        if (positionY < 80) nextPage();
        positionY = addTable(page, positionX, positionY, table) - line_space;
    }

    private void addPercentageToTitle(String percentange) throws IOException {
        if (titleData[0]==0 && titleData[1]==0) return;
        PDPage temp = doc.getPage(titleData[1]);
        int x = positionX + 400 - Math.round(fontTitleSize * font.getStringWidth(percentange) / 1000);
        addTitle(temp, percentange, x, titleData[0], fontTitleSize, false);
        titleData[0] = 0;
        titleData[1] = 0;
    }
}
