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
import java.util.HashMap;

public class PdfTermEvaluations extends InternalReportSkeleton {
    private static final String TAG = PdfTermEvaluations.class.getSimpleName();
    private final HashMap<Integer, Integer> studentsToAdd;
    TermEvaluationsManager manager;

    public PdfTermEvaluations(int stage, Date startDate, Date endDate, Date reportDate) {
        super(null, startDate, endDate, reportDate, stage, true);
        studentsToAdd = new HashMap<>();
        setStudentsToAdd();
        manager = new TermEvaluationsManager(studentsToAdd);
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

    private void setStudentsToAdd() {
        for (Integer student : ApplicationLoader.cacheManager.students.keySet()) {
            //name, birthday, drive_main, drive_documents, drive_photos, drive_reports, classroom
            Object[] data = ApplicationLoader.cacheManager.students.get(student);
            Integer classroom = (Integer) data[6];
            if (classroom != -1) {
                int year = ApplicationLoader.cacheManager.getChildrenYear(student, classroom, reportDate) + 1;
                if ((year <= STAGE_YEARS[stageId]) && (year > (stageId > 0 ? STAGE_YEARS[stageId-1] : 0))) {
                    studentsToAdd.put(student, year);
                }
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

            for (int student : studentsToAdd.keySet()) {
                int year = studentsToAdd.get(student);
                Integer target = getTargetsForPeriod(year, area);
                if (target != null && target != 0) {
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
            case 1: case 2: case 3: case 4: case 5:
                return countTargets(area, 1, year);
            case 6: case 7:
                return countTargets(area,6, 7);
            case 8: case 9:
                return countTargets(area,8, 9);
            case 10: case 11:
                return countTargets(area,10, 11);
        }
        return null;
    }

    private Integer countTargets(int area, int year1, int year2) {
        int total = 0;
        for (int year = year1; year <= year2 ; year++) {
            Integer value = manager.targets.get(area).get(year);
            total += (value != null ? value : 0);
        }
        return total;
    }

    private String getStage(int year) {
        for (int i = 0; i <= STAGE_YEARS.length - 1 ; i++) {
            if (year <= STAGE_YEARS[i]) {
                return STAGE_NAMES[i];
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

        HashMap<Integer, Integer> studentMap = manager.students.get(student);
        if (studentMap !=  null) {
            Integer points = studentMap.get(area);

            int p = points == null ? 0 : ((points * 10000) /target) / 100;
            String value = (points == null ? 0 : points) + "/" + target + ": " + p + "%";
            c = Cell.withText(value)
                    .setHorizontalAlignment(Cell.HorizontalAlignment.LEFT)
                    .withAllBorders();
            rowBuilder.add(c);
            tableBuilder.addRow(rowBuilder.build());
        }
    }
}
