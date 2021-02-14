package ui.formInternalReports;

import ui.formInternalReports.models.PdfTermEvaluations;
import utils.MyLogger;

import javax.swing.*;
import java.io.File;
import java.sql.Date;
import java.util.Calendar;

public class InternalReportsForm {
    private static final String TAG = InternalReportsForm.class.getSimpleName();
    private JPanel mainPanel;
    private JButton evaluationButton;

    public static JPanel main() {
        InternalReportsForm form = new InternalReportsForm();
        return form.mainPanel;
    }

    private void createUIComponents() {
        evaluationButton = new JButton();
        evaluationButton.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            cal.set(2020, Calendar.SEPTEMBER, 1);
            Date ini = new Date(cal.getTime().getTime());
            cal.set(2020, Calendar.DECEMBER, 31);
            Date fin = new Date(cal.getTime().getTime());
            PdfTermEvaluations pdf = new PdfTermEvaluations(5, ini, fin);
            File f = new File("prueba.pdf");
            MyLogger.d(TAG, "New file: " + f.getPath());
        });
    }
}
