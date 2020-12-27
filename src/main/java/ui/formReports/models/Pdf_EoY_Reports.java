package ui.formReports.models;

import bd.BDManager;
import bd.MySet;
import bd.model.TableTeachers;
import pdfs.tables.Cell;
import pdfs.tables.Row;
import pdfs.tables.Table;
import ui.formReports.managers.EoYManager;
import ui.formReports.managers.MediaManager;
import ui.formReports.managers.ReportManager;
import utils.CacheManager;
import utils.LanguageManager;
import utils.MyLogger;
import utils.SettingsManager;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Pdf_EoY_Reports extends PDFForm_Reports {
    private static final String TAG = Pdf_EoY_Reports.class.getSimpleName();
    private PDPage page;
    private Integer position;

    BDManager bdManager;
    CacheManager cacheManager;
    SettingsManager settingsManager;

    private EoYManager eoyManager;
    private java.sql.Date date;
    private HashMap<Integer, String> data;
    public Boolean isEmpty = false;
    DefaultListModel log;

    public Pdf_EoY_Reports(BDManager bdManager, Connection co, CacheManager cacheManager, SettingsManager settingsManager,
                           Integer studentId, Integer classroom, Date reportDate, BufferedImage logo,
                           ReportManager reportManager, Boolean addCommentsSection, Boolean includePictures,
                           DefaultListModel log) {
        super(bdManager, cacheManager, settingsManager, co, studentId, classroom, reportDate, "EoY", logo);
        try {
            this.bdManager = bdManager;
            this.cacheManager = cacheManager;
            this.settingsManager = settingsManager;
            this.log = log;

            eoyManager = new EoYManager(bdManager, settingsManager, reportManager, null,
                    new java.sql.Date(reportDate.getTime()), classroom, studentId);
            data = eoyManager.load();
            if (data.size() == 0) {
                isEmpty = true;
                return;
            }
            nextPage();
            position = 740;
            float scale = 0.5f;
            addImage(page, logo, Math.round(margin), position, String.valueOf(scale));
            addPhrase(page, Math.round(margin + logo.getWidth()  * scale) + 100 , position + Math.round(logo.getHeight()  * scale) - 20);
            position -= 15;
            position = addTopTable();
            position -= 30;
            position = addMiddleTable();
            nextPage();
            position = 780;
            position = addBottomTable();
            if (addCommentsSection) position = addComments();
            if (includePictures) {
                addPictures();
            } else {
                doc.save(fileName);
            }
        } catch (Exception e) {
            MyLogger.e(TAG, e);
            log.insertElementAt(e.getMessage(), 0);
        } finally {
            try {
                if (isEmpty) log.insertElementAt("No data for EoY report", 0);
                else log.insertElementAt("EoY report created: " + fileName, 0);
                if (isEmpty || !includePictures) doc.close();
            } catch (Exception e) {
                MyLogger.e(TAG, e);
                log.insertElementAt(e.getMessage(), 0);
            }
        }
    }

    private void addPictures() {
        try {
            String sDate1="01/09/2019";
            Date date1=new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
            String sDate2="30/06/2020";
            Date date2=new SimpleDateFormat("dd/MM/yyyy").parse(sDate2);
            MediaManager manager = new MediaManager(cacheManager,settingsManager, bdManager, studentId,
                    new java.sql.Date(date1.getTime()), new java.sql.Date(date2.getTime()), doc, font, margin, fileName,
                    log);
            manager.execute();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(String fileName) {
        try {
            doc.save(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nextPage() {
        page = new PDPage(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
        doc.addPage(page);
        position = 790;
    }

    private void addPhrase(PDPage page, int x, int y) throws IOException {
        PDPageContentStream contents = new PDPageContentStream(doc, page,
                PDPageContentStream.AppendMode.APPEND, true);
        float leading = 1.5f * 10;
        contents.beginText();
        contents.setFont(font, 12);
        contents.setStrokingColor(Color.black);
        contents.setNonStrokingColor(Color.black);
        contents.newLineAtOffset(x, y);
        contents.showText(settingsManager.language == 0 ? "Final interview" : "Entrevista Final");
        contents.newLineAtOffset(0, -leading * 1.5f);
        contents.showText(cacheManager.getNameStageofClassroom(classroom));
        contents.endText();
        contents.close();
    }

    private int addTopTable() throws IOException {
        Table.TableBuilder tableBuilder = new Table.TableBuilder();
        tableBuilder.addColumnOfWidth(300);
        tableBuilder.addColumnOfWidth(200);
        tableBuilder.setFontSize(8, 8);
        tableBuilder.setFont(font);

        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.NAME[settingsManager.language] + ":" +
                cacheManager.students.get(studentId)[0]);
        addCellToRowBuilder(rowBuilder, LanguageManager.DATE[settingsManager.language] + ":" +
                new SimpleDateFormat("MMMM").format(reportDate) + " " +
                new SimpleDateFormat("yyyy").format(reportDate));
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.AGE[settingsManager.language] + ":" +
                cacheManager.getChildrenAge(studentId));
        addCellToRowBuilder(rowBuilder, LanguageManager.GUIDE[settingsManager.language] + ":" +
                cacheManager.teachers.get(eoyManager.teacher)[0]);
        tableBuilder.addRow(rowBuilder.build());

        Table table = tableBuilder.build();
        PDPageContentStream contents = new PDPageContentStream(doc, page,PDPageContentStream.AppendMode.APPEND, true);
        addTable(page, Math.round(margin), position, table);
        contents.close();
        return Math.round(position - Math.round(table.getHeight()));
    }

    private int addMiddleTable() throws IOException {
        Table.TableBuilder tableBuilder = new Table.TableBuilder();
        tableBuilder.addColumnOfWidth(200);
        tableBuilder.addColumnOfWidth(300);
        tableBuilder.setFontSize(8, 8);
        tableBuilder.setFont(font);

        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.PSED[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(0) != null ? data.get(0).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.WORKINITIATIVE[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(1) != null ? data.get(1).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.FREEDOM[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(2) != null ? data.get(2).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.PATTERNS[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(3) != null ? data.get(3).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.CHALLENGES[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(4) != null ? data.get(4).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.BEHAVIOUR[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(5) != null ? data.get(5).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.CONCENTRATION[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(6) != null ? data.get(6).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.SELF[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(7) != null ? data.get(7).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.RELATIONSHIPS[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(8) != null ? data.get(8).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.LIMITS[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(9) != null ? data.get(9).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.MOVES[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(10) != null ? data.get(10).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        Table table = tableBuilder.build();
        PDPageContentStream contents = new PDPageContentStream(doc, page,PDPageContentStream.AppendMode.APPEND, true);
        addTable(page, Math.round(margin), position, table);
        contents.close();
        return Math.round(position - Math.round(table.getHeight()));
    }

    private int addBottomTable() throws IOException {
        Table.TableBuilder tableBuilder = new Table.TableBuilder();
        tableBuilder.addColumnOfWidth(200);
        tableBuilder.addColumnOfWidth(300);
        tableBuilder.setFontSize(8, 8);
        tableBuilder.setFont(font);

        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.LITERACY[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(11) != null ? data.get(11).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.MATHS[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(12) != null ? data.get(12).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.UNDERSTANDING[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(13) != null ? data.get(13).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.PHYSICAL_DEV[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(14) != null ? data.get(14).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.ARTS[settingsManager.language]);
        addCellToRowBuilder(rowBuilder, data.get(15) != null ? data.get(15).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        Table table = tableBuilder.build();
        PDPageContentStream contents = new PDPageContentStream(doc, page,PDPageContentStream.AppendMode.APPEND, true);
        addTable(page, Math.round(margin), position, table);
        contents.close();
        return Math.round(position - Math.round(table.getHeight()));
    }

    private Integer addComments() throws IOException {
        Table.TableBuilder tableBuilder = new Table.TableBuilder();
        tableBuilder.addColumnOfWidth(500);
        tableBuilder.setFontSize(8, 8);
        tableBuilder.setFont(font);

        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, LanguageManager.COMMENTS[settingsManager.language]);
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        addCellToRowBuilder(rowBuilder, data.get(16) != null ? data.get(16).replace("\t", "") : "");
        tableBuilder.addRow(rowBuilder.build());

        Table table = tableBuilder.build();
        PDPageContentStream contents = new PDPageContentStream(doc, page,PDPageContentStream.AppendMode.APPEND, true);
        addTable(page, Math.round(margin), position, table);
        contents.close();
        return Math.round(position - Math.round(table.getHeight()));
    }

    private void addCellToRowBuilder(Row.RowBuilder rowBuilder, String text) {
        rowBuilder.add(Cell.withText(text)
                .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                .withAllBorders());
    }

    private String getTeachers() {
        if (co == null) co = bdManager.connect();
        StringBuilder result = null;
        MySet set = bdManager.getValues(co, BDManager.tableTeachers, TableTeachers.classroom + "=" + classroom);
        while (set.next()) {
            if (result == null) result = new StringBuilder(set.getString(TableTeachers.nick));
            else result.append(" y ").append(set.getString(TableTeachers.nick));
        }
        return result.toString();
    }
}
