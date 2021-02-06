package ui.formReports.models;

import pdfs.tables.Cell;
import pdfs.tables.Row;
import pdfs.tables.Table;
import pdfs.tables.TableDrawer;
import utils.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;

public class PresentationPage extends PDPage {
    private static final String TAG = PresentationPage.class.getSimpleName();
    private final CacheManager cacheManager;
    private final SettingsManager settingsManager;
    private static String TEXT_ACTIVITY;
    private static String TEXT_DATE;
    private static String TEXT_TARGETS;
    private static String TEXT_COMMENTS;
    private static String TEXT_NEXT;
    private static String TEXT_NEXT_LINKS;
    private final PDDocument doc;
    private final float margin;
    private BufferedImage picture;
    private final Date date;
    private final int presentation;
    private final Integer presentationSub;
    private Integer position;
    private float pictureHeight;
    private float pictureWidth;
    private final int tableWidth;
    private final String comments;
    private final String outcomes;
    private final String targets;
    private final NextPresentation nextPresentation;
    final PDFont font;

    public PresentationPage(CacheManager cacheManager , SettingsManager settingsManager, PDDocument doc, float margin,
                            BufferedImage picture, Date date, int student, int presentation,
                            Integer presentationSub, PDFont font, String comments, String outcomes, String targets,
                            NextPresentation nextPresentation) {

        super(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
        this.cacheManager = cacheManager;
        this.settingsManager = settingsManager;
        this.doc = doc;
        this.margin = margin;
        this.picture = picture;
        this.date = date;
        this.presentation = presentation;
        this.presentationSub = presentationSub;
        this.font = font;
        this.comments = comments;
        this.outcomes = outcomes;
        this.targets = targets;
        this.nextPresentation = nextPresentation;

        tableWidth = Math.round(PDRectangle.A4.getWidth() - margin * 2);
        TEXT_ACTIVITY   = settingsManager.language == 0 ? "Activity: " : "Actividad: ";
        TEXT_DATE       = settingsManager.language == 0 ? "Date: " : "Fecha: ";
        TEXT_TARGETS    = settingsManager.language == 0 ? "National Curriculum targets" : "Objetivos del 'National Curriculum' trabajados";
        //TEXT_STEPS      = settingsManager.language == 0 ? "Preliminary steps" : "Pasos preliminares";
        //TEXT_WORKED     = settingsManager.language == 0 ? "Targets worked" : "Objetivos trabajados";
        TEXT_COMMENTS   = settingsManager.language == 0 ? "Comments" : "Comentarios";
        TEXT_NEXT       = settingsManager.language == 0 ? "Curriculum next activity" : "Siguiente actividad del curriculum";
        TEXT_NEXT_LINKS = settingsManager.language == 0 ? "Curriculum next learning goal" : "Siguiente objetivo de aprendizaje del curriculum";
        createPage();
    }

    private void createPage(){
        position = Math.round(PDRectangle.A4.getHeight() - margin);
        position = Math.round(position - addTopTable(position)  - margin);
        position = Math.round(position - addImage() - 10);
        if (!outcomes.equals("") || !targets.equals(""))
            position = Math.round(position - addTargetsTable() - 10);
        position = Math.round(position - addSimpleTable(TEXT_COMMENTS, comments, null, null) - 10);
        if (nextPresentation.doExists())
            position = Math.round(position - addSimpleTable(TEXT_NEXT, nextPresentation.getNextPresentationText(),
                    TEXT_NEXT_LINKS, nextPresentation.linksText) - 10);
    }

    private int addTopTable(int y)  {
        Table.TableBuilder tableBuilder = createTableBuilder(2, 8,0.75f);

        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        rowBuilder.add(Cell.withText(TEXT_ACTIVITY + getActivity())
                .setHorizontalAlignment(Cell.HorizontalAlignment.CENTER)
                .withAllBorders());
        rowBuilder.add(Cell.withText(TEXT_DATE + date.toString())
                .setHorizontalAlignment(Cell.HorizontalAlignment.CENTER)
                .withAllBorders());
        tableBuilder.addRow(rowBuilder.build());

        return Math.round(drawTable(tableBuilder, y));
    }

    private Float addTargetsTable() {
        Table.TableBuilder tableBuilder = createTableBuilder(1, 7,0.5f);

        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        rowBuilder.add(Cell.withText(TEXT_TARGETS)
                .setHorizontalAlignment(Cell.HorizontalAlignment.CENTER)
                .withAllBorders());
        tableBuilder.addRow(rowBuilder.build());
/*
        rowBuilder = new Row.RowBuilder();
        rowBuilder.add(Cell.withText(TEXT_STEPS)
                .setHorizontalAlignment(Cell.HorizontalAlignment.CENTER)
                .withAllBorders());
        rowBuilder.add(Cell.withText(TEXT_WORKED)
                .setHorizontalAlignment(Cell.HorizontalAlignment.CENTER)
                .withAllBorders());
        tableBuilder.addRow(rowBuilder.build());*/

        if (!outcomes.equals("")) {
            rowBuilder = new Row.RowBuilder();
            rowBuilder.add(Cell.withText(outcomes)
                    .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                    .withAllBorders());
            tableBuilder.addRow(rowBuilder.build());
        }

        if (!targets.equals("")) {
            rowBuilder = new Row.RowBuilder();
            rowBuilder.add(Cell.withText(targets)
                    .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                    .withAllBorders());
            tableBuilder.addRow(rowBuilder.build());
        }

        return drawTable(tableBuilder, position);
    }

    private Float addSimpleTable(String title, String text, String title2, String[] text2) {
        Table.TableBuilder tableBuilder = createTableBuilder(1, 7, 0.5f);

        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        rowBuilder.add(Cell.withText(title)
                .setHorizontalAlignment(Cell.HorizontalAlignment.CENTER)
                .withAllBorders());
        tableBuilder.addRow(rowBuilder.build());

        rowBuilder = new Row.RowBuilder();
        rowBuilder.add(Cell.withText(text)
                .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                .withAllBorders());
        tableBuilder.addRow(rowBuilder.build());

        if (title2 != null && (text2[0].length() > 0 || text2[1].length() > 0)) {
            rowBuilder = new Row.RowBuilder();
            rowBuilder.add(Cell.withText(title2)
                    .setHorizontalAlignment(Cell.HorizontalAlignment.CENTER)
                    .withAllBorders());
            tableBuilder.addRow(rowBuilder.build());

            for (String t : text2) {
                if (t != null && !t.equals("")) {
                    rowBuilder = new Row.RowBuilder();
                    rowBuilder.add(Cell.withText(t)
                            .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                            .withAllBorders());
                    tableBuilder.addRow(rowBuilder.build());
                }
            }
        }

        return drawTable(tableBuilder, position);
    }

    private Table.TableBuilder createTableBuilder(int columns, int fontSize, float percentage) {
        Table.TableBuilder tableBuilder = new Table.TableBuilder();
        switch (columns) {
            case 1 : tableBuilder.addColumnOfWidth(Math.round(tableWidth)); break;
            case 2 : {
                tableBuilder.addColumnOfWidth(Math.round(tableWidth * percentage));
                tableBuilder.addColumnOfWidth(Math.round(tableWidth * (1 - percentage)));
            }
        }
        tableBuilder.setFontSize(8, fontSize);
        tableBuilder.setFont(font);
        return tableBuilder;
    }

    private String getActivity() {
        return  cacheManager.presentations.get(presentation)[settingsManager.language] +
                ((presentationSub != null && presentationSub != -1) ?
                        " --> " + cacheManager.presentationsSub.get(presentationSub)[settingsManager.language] : "");
    }

    private Float drawTable(Table.TableBuilder tableBuilder, int y) {
        PDPageContentStream contents;
        try {
            Table table = tableBuilder.build();
            contents = new PDPageContentStream(doc, this, PDPageContentStream.AppendMode.APPEND, true);
            (new TableDrawer(contents, table, margin, y)).draw();
            contents.close();
            return table.getHeight();
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        }
        return 0f;
    }

    private Float addImage() {
        PDPageContentStream contentStream = null;
        PDImageXObject pdImage;
        try {
            calculateSize();
            picture = ImageUtils.resizeImage(picture, Math.round(pictureWidth), Math.round(pictureHeight));
            pdImage = LosslessFactory.createFromImage(doc,picture);
            contentStream = new PDPageContentStream(doc, this,
                    PDPageContentStream.AppendMode.APPEND, true, true);

            int x = Math.round((PDRectangle.A4.getWidth() - pictureWidth) / 2);
            contentStream.drawImage(pdImage, x, position - pictureHeight, pictureWidth, pictureHeight);
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        } finally {
            if (contentStream != null) {
                try { contentStream.close(); } catch (IOException e) { MyLogger.e(TAG, e); }
            }
        }
        return pictureHeight;
    }

    private void calculateSize() {
        float ratio = (((Integer)picture.getHeight()).floatValue() / picture.getWidth());
        float maxHeight = (600 - margin * 2) * ratio;
        maxHeight = (maxHeight > 450) ? 450 : maxHeight;
        if (picture.getHeight() > maxHeight) {
            pictureHeight = maxHeight;
            pictureWidth = pictureHeight / ratio;
        } else {
            pictureHeight = picture.getHeight();
            pictureWidth = picture.getWidth();
        }
    }
}
