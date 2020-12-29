package pdfs.models;

import pdfs.MyPDFGraphicsStreamEngine;
import pdfs.tables.Cell;
import pdfs.tables.Row;
import pdfs.tables.Table;
import pdfs.tables.TableDrawer;
import utils.MyLogger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.IOException;

public class PDFForm {
    private static final String TAG = PDFForm.class.getSimpleName();
    protected final int line_space = 8;
    protected final float margin = 30;
    private final PDDocument doc;
    protected PDPage page;
    private PDFont font = null;
    int position;

    public PDFForm() {
        doc = new PDDocument();
        try {
            font = PDType0Font.load(doc, getClass().getResourceAsStream("Verdana.ttf"));
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        }
    }

    protected void nextPage() {
        page = createPage();
        doc.addPage(page);
        position = 560;
    }

    PDPage createPage() {
        PDPage page = new PDPage((new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())));
        page.setRotation(0);
        return page;
    }

    @SuppressWarnings("SameParameterValue")
    protected Integer addLine(String message, int x, int y) throws IOException {
        if (position - 14 - line_space < 30) {
            nextPage();
            y = 570;
        }
        PDPageContentStream contents = new PDPageContentStream(doc, page,
                PDPageContentStream.AppendMode.APPEND, true);
        contents.beginText();
        contents.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contents.setStrokingColor(Color.black);
        contents.setNonStrokingColor(Color.black);
        contents.newLineAtOffset(x, y);
        contents.showText(message);
        contents.endText();
        contents.close();
        return y - 14 - line_space;
    }

    protected Integer addTable(int[] columns, String[] title, String[][] data, int y,
                               int fontTitleSize) throws IOException {
        Table.TableBuilder tableBuilder = new Table.TableBuilder();
        for (int width : columns) tableBuilder.addColumnOfWidth(width);
        tableBuilder.setFontSize(fontTitleSize, 8);
        tableBuilder.setFont(font);

        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        for (String titleCell : title) {
            rowBuilder.add(Cell.withText(titleCell)
                    .setHorizontalAlignment((data != null) ?
                            Cell.HorizontalAlignment.CENTER : Cell.HorizontalAlignment.LEFT)
                    .withAllBorders());
        }
        tableBuilder.addRow(rowBuilder.build());
        double headMult;
        if (data != null) {
            for(String[] line : data) {
                rowBuilder = new Row.RowBuilder();
                int i = 0;
                for (String cell : line) {
                    rowBuilder.add(Cell.withText(cell)
                            .setHorizontalAlignment(i == 0 ? Cell.HorizontalAlignment.LEFT : Cell.HorizontalAlignment.CENTER)
                            .withAllBorders());
                    i++;
                }
                tableBuilder.addRow(rowBuilder.build());
            }
            headMult = (double)(data.length + 1) / (double)data.length;
        } else headMult = 1;

        PDPageContentStream contents = new PDPageContentStream(doc, page,PDPageContentStream.AppendMode.APPEND, true);

        MyPDFGraphicsStreamEngine c = new MyPDFGraphicsStreamEngine(page);
        
        Table table = tableBuilder.build();
        (new TableDrawer(contents, table, 15, y)).draw();
        contents.close();
        double height = table.getHeight() * headMult;
        return Math.round(y - Math.round(height) - line_space*2);
    }

    protected void saveFile(String fileName){
        try {
            doc.save(fileName);
            MyLogger.d(TAG, "Pdf saved: " + fileName);
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        } finally {
            try {
                doc.close();
            } catch (IOException e) {
                MyLogger.e(TAG, e);
            }
        }
    }

}
