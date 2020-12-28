package ui.formReports.models;

import bd.BDManager;
import pdfs.tables.Cell;
import pdfs.tables.Row;
import pdfs.tables.Table;
import ui.formReports.managers.ReportManager;
import ui.formReports.managers.YetManager;
import utils.CacheManager;
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
import java.util.Date;
import java.util.LinkedHashMap;

public class Pdf_Yet_Reports extends PDFForm_Reports {
    private static final String TAG = Pdf_Yet_Reports.class.getSimpleName();
    private final String[] first_column = {"You have done this well...", "Even better if...",
            "Task for next term.", "Staff name:"};
    private PDPage page;
    private Integer position;
    private double year;
    private String notes;
    public Integer teacher;
    private LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>> model;
    public Boolean isEmpty = false;
    DefaultListModel<String> log;
    String[] data;
    YetManager yetManager;
    ReportManager reportManager;

    public Pdf_Yet_Reports(BDManager bdManager, Connection co, CacheManager cacheManager, SettingsManager settingsManager,
                           Integer studentId, Integer classroom, Date reportDate, BufferedImage logo,
                           ReportManager reportManager, DefaultListModel<String> log) {
        super(bdManager, cacheManager, settingsManager, co, studentId, classroom, reportDate, "Yet", logo);
        try {
            this.reportManager = reportManager;
            this.log = log;
            yetManager = new YetManager(bdManager, settingsManager, reportManager, null, null,
                    null, null, new java.sql.Date(reportDate.getTime()), classroom, studentId);
            data = yetManager.load();
            if (data[1] == null && data[2] == null && data[3] == null) {
                isEmpty = true; return;
            }

            nextPage();
            float scale = 0.5f;
            addImage(page, logo, Math.round(margin), position, String.valueOf(scale));
            addPhrase(page, Math.round(margin + logo.getWidth()  * scale) + 50 , position + Math.round(logo.getHeight()  * scale)- 8);
            position -= 20;
            position = addTitle(page, "End of term report. The power of Yet:", Math.round(margin) , position, 12, true);
            position += 10;

            position = addTable();
            doc.save(fileName);
        } catch (Exception e) {
            MyLogger.e(TAG, e);
            log.insertElementAt(e.getMessage(), 0);
        } finally {
            try {
                if (isEmpty) log.insertElementAt("No data for yet report", 0);
                else log.insertElementAt("Yet report created: " + fileName, 0);
                doc.close();
            } catch (IOException e) {
                MyLogger.e(TAG, e);
                log.insertElementAt(e.getMessage(), 0);
            }
        }
    }

    private void nextPage() {
        page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
        doc.addPage(page);
        position = 490;
    }

    private void addPhrase(PDPage page, int x, int y) throws IOException {
        if (data[0] == null) return;
        PDPageContentStream contents = new PDPageContentStream(doc, page,
                PDPageContentStream.AppendMode.APPEND, true);
        float leading = 1.5f * 10;
        String[] phrase = data[0].replace("\t", "        ").split("\n");
        contents.beginText();
        contents.setFont(font, 10);
        contents.setStrokingColor(Color.black);
        contents.setNonStrokingColor(Color.black);
        contents.newLineAtOffset(x, y);
        for (int i = 0 ; i < phrase.length - 1; i++) {
            float size = 10 * font.getStringWidth(phrase[i]) / 1000;
            if (size > 800 - x) {
                StringBuilder line = new StringBuilder();
                String[] words = phrase[i].split(" ");
                for (String word : words) {
                    size = 10 * font.getStringWidth(line + " " + word) / 1000;
                    if (size > 800 - x) {
                        contents.showText(line.toString());
                        contents.newLineAtOffset(0, -leading);
                        y -= 10 + 2;
                        line = new StringBuilder(" " + word);
                    } else line.append(" ").append(word);
                }
                contents.showText(line.toString());
            } else {
                contents.showText(" " + phrase[i]);
            }
            contents.newLineAtOffset(0, -leading);
            y -= 10 + 2;
        }
        //contents.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
        contents.showText(" " + phrase[phrase.length-1]);
        contents.endText();
        contents.close();
    }

    private int addTable() throws IOException {
        Table.TableBuilder tableBuilder = new Table.TableBuilder();
        tableBuilder.addColumnOfWidth(200);
        tableBuilder.addColumnOfWidth(600);
        tableBuilder.setFontSize(8, 8);
        tableBuilder.setFont(font);

        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        String[] titles = new String[] {"Name: " + cacheManager.students.get(studentId)[0],
                "Term " + reportManager.getTerm() + ". " + reportManager.getAcademicYears() + ". " +
                        PDFForm_Reports.stages[stageId]+ "."};
        for (String titleCell : titles) {
            rowBuilder.add(Cell.withText(titleCell)
                    .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                    .withAllBorders());
        }

        tableBuilder.addRow(rowBuilder.build());

        if (data != null) {
            for (int i = 0; i < first_column.length; i++) {
                rowBuilder = new Row.RowBuilder();
                Cell c = Cell.withText(first_column[i])
                        .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                        .withAllBorders();
                rowBuilder.add(c);

                if (i < 3) {
                    String cells = "";
                    if (data[i+1] != null) cells = data[i+1];//note[i].replace("<TAB>","\n")+"<ADD COLUMN>";
                    c = Cell.withText(cells)
                            .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                            .withAllBorders();
                    rowBuilder.add(c);
                } else if (i == 3 && classroom != null) {
                    c = Cell.withText(getTeachers())
                            .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                            .withAllBorders();
                    rowBuilder.add(c);
                } else {
                    rowBuilder.add(Cell.withText(" \n \n \n \n").withAllBorders());
                }
                tableBuilder.addRow(rowBuilder.build());
            }
        }

        Table table = tableBuilder.build();
        PDPageContentStream contents = new PDPageContentStream(doc, page,PDPageContentStream.AppendMode.APPEND, true);
        addTable(page, Math.round(margin), position, table);
        contents.close();
        return Math.round(position - Math.round(table.getHeight()));
    }

    private String getTeachers() {
        StringBuilder teachers = null;
        boolean first = true;
        for (Object[] data : cacheManager.teachers.values()) {
            if (data[1] == classroom) {
                if (first) {
                    teachers = new StringBuilder((String) data[0]);
                    first = false;
                }
                else teachers.append(" & ").append(data[0]);
            }
        }
        return teachers != null ? teachers.toString() : "";
    }
}
