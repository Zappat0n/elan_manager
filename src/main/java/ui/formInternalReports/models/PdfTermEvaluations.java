package ui.formInternalReports.models;

import main.ApplicationLoader;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import pdfs.tables.Cell;
import pdfs.tables.Row;
import pdfs.tables.Table;
import ui.formInternalReports.managers.TermEvaluationsManager;
import utils.MyLogger;

import java.io.IOException;
import java.sql.Date;

public class PdfTermEvaluations extends InternalReportSkeleton {
    private static final String TAG = PdfTermEvaluations.class.getSimpleName();
    TermEvaluationsManager manager;

    public PdfTermEvaluations(Integer classroom, Date startDate, Date endDate) {
        super(null, startDate, endDate, classroom, true);
        manager = new TermEvaluationsManager(classroom, startDate, endDate);
        try {
            addTables();
            doc.save(fileName);
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            try {
                doc.close();
            } catch (Exception e) {
                MyLogger.e(TAG, e);
            }
        }
    }

    private void addTables() throws IOException {
        nextPage();
        position += 20;
        for ( int area : manager.targets.keySet()) {
            Table.TableBuilder tableBuilder = new Table.TableBuilder();
            tableBuilder.addColumnOfWidth(150);
            tableBuilder.addColumnOfWidth(75);
            tableBuilder.setFontSize(8, 8);
            tableBuilder.setFont(font);
            addTitle(tableBuilder, area);

            for (int student : ApplicationLoader.cacheManager.studentsPerClassroom.get(classroom)) {
                Integer year = ApplicationLoader.cacheManager.getChildrenYear(student, classroom, startDate);
                Integer target = manager.targets.get(area).get(year);
                if (target != null) {
                    addRow(tableBuilder, area, student, target);
                }
            }

            Table table = tableBuilder.build();

            if (position - Math.round(table.getHeight()) < 30) {
                nextPage();
            } else {
                position -= 20;
            }

            PDPageContentStream contents = new PDPageContentStream(doc, page,PDPageContentStream.AppendMode.APPEND, true);
            addTable(page, Math.round(margin), position, table);
            contents.close();
            position = Math.round(position - Math.round(table.getHeight()));
        }
    }

    private void addTitle(Table.TableBuilder tableBuilder, int area) {
        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        Cell c = Cell.withText(ApplicationLoader.cacheManager.areasTarget.get(area)[ApplicationLoader.settingsManager.language])
                .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                .withAllBorders();
        rowBuilder.add(c);
        c = Cell.withText("")
                .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                .withAllBorders();
        rowBuilder.add(c);
        tableBuilder.addRow(rowBuilder.build());
    }

    private void addRow(Table.TableBuilder tableBuilder, int area, int student, int target) {
        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        Cell c = Cell.withText(ApplicationLoader.cacheManager.students.get(student)[0])
                .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                .withAllBorders();
        rowBuilder.add(c);

        Integer points = manager.students.get(student).get(area);
        int p = points == null ? 0 : ((points * 10000) /target) / 100;
        String value = (points == null ? 0 : points) + "/" + target + ": " + p + "%";
        c = Cell.withText(value)
                .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                .withAllBorders();
        rowBuilder.add(c);

        tableBuilder.addRow(rowBuilder.build());
    }
}
