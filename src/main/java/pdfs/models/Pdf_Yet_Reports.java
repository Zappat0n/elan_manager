package pdfs.models;

import bd.BDManager;
import bd.model.TableEvents;
import pdfs.tables.Cell;
import pdfs.tables.Row;
import pdfs.tables.Table;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedHashMap;

public class Pdf_Yet_Reports extends PDFForm_Reports {
    private static final String TAG = Pdf_Yet_Reports.class.getSimpleName();
    private final String[] first_column = {"You have done this well...", "Even better if...",
            "Task for next term.", "Staff name:", "Family Comment:"};
    private PDPage page;
    private Integer position;
    private double year;
    private String notes;
    public Integer teacher;
    private LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>> model;

    public Pdf_Yet_Reports(BDManager bdManager, Connection co, CacheManager cacheManager, SettingsManager settingsManager,
                           Integer studentId, Integer classroom, Date reportDate, BufferedImage logo) {
        super(bdManager, cacheManager, settingsManager, co, "Yet", studentId, classroom,
                reportDate, null, logo);
        try {
            //studentId = 102;
            loadEndEvents(co);
            nextPage();
            float scale = 0.5f;
            addImage(page, logo, Math.round(margin), position, String.valueOf(scale));
            String[] message = {"How does [the child] achieve his independence?", "He does it by means of constant activity.",
                    "How does he become free?","By means of constant effort", "María Montessori."};
            addPhrase(page, message, Math.round(margin + logo.getWidth()  * scale) + 100 , position + Math.round(logo.getHeight()  * scale)- 8);
            position -= 40;
            position = addTitle(page, "End of term report", Math.round(margin) , position, 12, true);
            position = addTitle(page, "The power of Yet:", Math.round(margin) , position, 12, true);

            position = addTable();

            //model = loadModel(new Double[]{year});
            //loadEndEvents(co, studentId);
            //if (year == 2.5) year = 0;
            //checkCurrentStage();
            doc.save(fileName);
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            try {
                MyLogger.d(TAG, "File created: " + fileName);
                doc.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void nextPage() {
        page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
        doc.addPage(page);
        position = 490;
    }

    private void addPhrase(PDPage page, String[] message, int x, int y) throws IOException {
        PDPageContentStream contents = new PDPageContentStream(doc, page,
                PDPageContentStream.AppendMode.APPEND, true);
        float leading = 1.5f * 12;
        contents.beginText();
        contents.setFont(font, 12);
        contents.setStrokingColor(Color.black);
        contents.setNonStrokingColor(Color.black);
        contents.newLineAtOffset(x, y);
        for (int i = 0 ; i < message.length - 1; i++) {
            contents.showText(message[i]);
            contents.newLineAtOffset(0, -leading);
            y -= 12 + 2;
        }
        contents.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contents.showText(message[message.length-1]);

        contents.endText();
        contents.close();

    }

    private int addTable() throws IOException {

        Table.TableBuilder tableBuilder = new Table.TableBuilder();
        tableBuilder.addColumnOfWidth(200);
        tableBuilder.addColumnOfWidth(300);
        tableBuilder.addColumnOfWidth(300);
        tableBuilder.setFontSize(8, 8);
        tableBuilder.setFont(font);

        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        String[] titles = new String[] {"Name: " + cacheManager.students.get(studentId)[0],
                "Term 2. 2018/19 ", stages[stageId]};
        for (String titleCell : titles) {
            rowBuilder.add(Cell.withText(titleCell)
                    .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                    .withAllBorders());
        }

        tableBuilder.addRow(rowBuilder.build());

        if (notes != null) {
            String[] note = notes.split("<LINE>");
            for (int i = 0; i < first_column.length; i++) {
                rowBuilder = new Row.RowBuilder();
                Cell c = Cell.withText(first_column[i])

                        .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                        .withAllBorders();
                rowBuilder.add(c);

                if (i < 3) {
                    if (note[i] != null) {
                        //String[] cells = note[i].split("<TAB>");
                        String cells = note[i].replace("<TAB>","\n")+"<ADD COLUMN>";
                        c = Cell.withText(cells)
                                .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                                .withAllBordersButRight(1);
                        rowBuilder.add(c);
                        c = Cell.withText("")
                                .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                                .withAllBordersButLeft(1);
                        rowBuilder.add(c);
                    }

                } else if (i == 3 && teacher != null) {
                    c = Cell.withText(cacheManager.teachers.get(teacher)[0])
                            .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                            .withAllBordersButRight(1);
                    rowBuilder.add(c);
                    rowBuilder.add(Cell.withText("").withAllBordersButLeft(1));
                } else {
                    rowBuilder.add(Cell.withText(" \n \n \n \n").withAllBordersButRight(1));
                    rowBuilder.add(Cell.withText("").withAllBordersButLeft(1));
                }
                tableBuilder.addRow(rowBuilder.build());
            }
        }

        Table table = tableBuilder.build();
        PDPageContentStream contents = new PDPageContentStream(doc, page,PDPageContentStream.AppendMode.APPEND, true);
        addTable(page, Math.round(margin), position, table);
        contents.close();
        return Math.round(position - Math.round(table.getHeight()));

            /*
        int height;
        BoxedText bt = null;
        if (year == 0 && areaId == 10) {
            height = 0;
        } else {
            bt = new BoxedText(boxedText, fontSize, page.getMediaBox().getWidth()-2*margin, PDType1Font.HELVETICA);
            height = Math.round(bt.getHeight());
        }

        int subheight = (sub == null) ? 0 : Math.round(font.getHeight(fontSize) + line_space * 3);

        int nextPos = position - Math.round(table.getHeight()) - height - line_space *3 -subheight;
        if (nextPos < 30) nextPage();
        position = addTable(page,Math.round(margin), position, table);
        position = position - line_space *3;

        if (bt != null) position = addBoxedText(page, page.getMediaBox().getWidth()-2*margin, bt,
                Math.round(margin), position, PDType1Font.HELVETICA, fontSize);
        if (sub != null)position = addTitle(page, sub, Math.round(margin), position - line_space*3, fontSize, false);*/
    }

    void loadEndEvents(Connection co) throws SQLException {
        String query = "SELECT * FROM Events WHERE student=" + studentId + " AND event_type=20";
        Statement st = co.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            notes = rs.getString(TableEvents.notes);
            teacher = rs.getInt(TableEvents.teacher);
        }
        BDManager.closeQuietly(rs);
        BDManager.closeQuietly(st);
    }

}
