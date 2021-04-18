package ui.formInternalReports;

import main.ApplicationLoader;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import ui.components.DateLabelFormatter;
import ui.formInternalReports.models.PdfTermEvaluations;

import javax.swing.*;
import java.sql.Date;
import java.util.Calendar;
import java.util.Properties;

public class InternalReportsForm {
    private static final String TAG = InternalReportsForm.class.getSimpleName();
    private JPanel mainPanel;
    private JButton evaluationButton;
    private JTabbedPane tabbedPane1;
    private JDatePickerImpl datePickerStart;
    private JDatePickerImpl datePickerEnd;
    private JComboBox cBClassroom;
    private JList listLog;
    private UtilDateModel dateModelStart;
    private UtilDateModel dateModelEnd;

    public static JPanel main() {
        InternalReportsForm form = new InternalReportsForm();
        return form.mainPanel;
    }

    private void createUIComponents() {
        dateModelStart = new UtilDateModel();
        dateModelEnd = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        datePickerStart = new JDatePickerImpl(new JDatePanelImpl(dateModelStart, p), new DateLabelFormatter());
        datePickerEnd = new JDatePickerImpl(new JDatePanelImpl(dateModelEnd, p), new DateLabelFormatter());
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.SEPTEMBER, 1);
        dateModelStart.setValue(cal.getTime());
        cal.set(2020, Calendar.DECEMBER, 31);
        dateModelEnd.setValue(cal.getTime());
        evaluationButton = new JButton();
        evaluationButton.addActionListener(e -> {
            Date ini = new Date(dateModelStart.getValue().getTime());
            Date fin = new Date(dateModelEnd.getValue().getTime());
            PdfTermEvaluations pdf = new PdfTermEvaluations(4, ini, fin);
            //String name = (String)cBClassroom.getSelectedItem() + "_" + ini + "_" + fin;
            addToLog("File created", pdf.fileName);
        });

        cBClassroom = new JComboBox(ApplicationLoader.cacheManager.getClassroomsNames());
        listLog = new JList(new DefaultListModel());
    }

    private void addToLog(String title, String text) {
        ((DefaultListModel)listLog.getModel()).insertElementAt(title + ": " + text, 0);
    }
}
