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
    public static final Integer[] periods = { 7, 9, 11, 14 };
    private static final String[] periodsNames = { "KS1", "LKS2", "UKS2", "KS3" };
    TermEvaluationsManager manager;

    public PdfTermEvaluations(Integer classroom, Date startDate, Date endDate) {
        super(null, startDate, endDate, classroom, true);
        manager = new TermEvaluationsManager(classroom);
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
                int year = ApplicationLoader.cacheManager.getChildrenYear(student, classroom, null) + 1;
                Integer target = getTargetsForPeriod(year, area);
                if (target != null) {
                    addRow(tableBuilder, area, student, target, year);
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

    private Integer getTargetsForPeriod(int year, int area) {
        switch (year) {
            case 6: case 7: return countTargets(area,6, 7);
            case 8: case 9: return countTargets(area,8, 9);
            case 10: case 11: return countTargets(area,10, 11);
        }
        return null;

    }

    private Integer countTargets(int area, int year1, int year2) {
        Integer targets1 = manager.targets.get(area).get(year1);
        Integer targets2 = manager.targets.get(area).get(year2);
        targets1 = targets1 != null ? targets1 : 0;
        targets2 = targets2 != null ? targets2 : 0;
        return targets1 + targets2;
    }

    private String getStage(int year) {
        for (int i = 0; i <= periods.length - 1 ; i++) {
            if (year <= periods[i]) {
                return periodsNames[i];
            }
        }
        return "None";
    }

    private void addRow(Table.TableBuilder tableBuilder, int area, int student, int target, int year) {
        Row.RowBuilder rowBuilder = new Row.RowBuilder();
        Cell c = Cell.withText(ApplicationLoader.cacheManager.students.get(student)[0] + " - " + getStage(year))
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
