package pdfs.models;

import main.ApplicationLoader;
import ui.formClassroom.MyTableModelPlanning;
import utils.MyLogger;
import utils.SettingsManager;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class Pdf_Planning extends PDFForm{
    private static final String TAG = Pdf_Planning.class.getSimpleName();
    private final JTable tablePlanning;
    private final Date date;
    private final Integer classroom;


    public Pdf_Planning(JTable tablePlanning, Integer classroom, Date date) {
        super();
        this.tablePlanning = tablePlanning;
        this.date = date;
        this.classroom = classroom;
    }

    public void createDocument(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        cal.set(Calendar.WEEK_OF_YEAR, week+1);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String ini = sdf.format(cal.getTime());
        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        String end = sdf.format(cal.getTime());
        try {
            nextPage();
            String text = "WEEKLY PLANNER FOR: " + ApplicationLoader.cacheManager.classrooms.get(classroom) +
                    String.join("", Collections.nCopies(90, " ")) + "WEEK: " + ini + " - " + end;
            int margin = 30;
            position = addLine(text, margin, 560);

            MyTableModelPlanning model = (MyTableModelPlanning) tablePlanning.getModel();
            String[][] data = new String[model.getRowCount()][6];
            String[][] data2 = new String[model.getRowCount()][2];
            for (int i = 0; i < model.getRowCount(); i++) {
                data[i] = new String[6];
                data2[i] = new String[2];
                for (int j = 0; j < 6; j++) {
                    String cell = String.join(",", (String[]) model.getValueAt(i, j).toArray());
                    if (j == 0 && cell.contains(" "))  cell = cell.substring(0, cell.indexOf(" ")+2)+".";
                    data[i][j] = cell;
                    if (j == 0) data2[i][j] = cell;
                    if (j == 1) data2[i][j] = "";
                }
            }

            position = addTable(new int[]{70, 150, 150, 150, 150, 150},
                    new String[]{"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"}, data, position+20,
                    8);
            nextPage();
            text = "OBSERVATIONS FOR: " + ApplicationLoader.cacheManager.classrooms.get(classroom) +
                    String.join("", Collections.nCopies(95, " ")) + "WEEK: " + ini + " - " + end;
            position = addLine(text, margin, 560);

            position = addTable(new int[]{70, 750},
                    new String[]{"", "Observations"}, data2, position+20, 12);

        } catch (Exception e) {
            MyLogger.e(TAG, e);
        } finally {
            saveFile(ApplicationLoader.settingsManager.getValue(SettingsManager.REPORTS_DIR)+"prueba.pdf");
        }
    }

}
