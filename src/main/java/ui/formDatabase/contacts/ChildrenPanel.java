package ui.formDatabase.contacts;

import ui.components.DateLabelFormatter;
import utils.CacheManager;
import org.jdatepicker.DateModel;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.util.Properties;

public class ChildrenPanel {
    private static CacheManager cacheManager;
    public JTextField tFName;
    public JDatePickerImpl datePickerBirthday;
    public JComboBox cBClassroom;
    public JTextField tFAddress;
    public JDatePickerImpl datePickerFirstDayComundi;
    public JDatePickerImpl datePickerFirstDayCDB;
    public JDatePickerImpl datePickerFirstDayPrimary;
    public JDatePickerImpl datePickerExitDay;
    public DateModel dMBirthday;
    public DateModel dMFirstDayComundi;
    public DateModel dMFirstDayCDB;
    public DateModel dMFirstDayPrimary;
    public DateModel dMExitDay;

    public JTextArea tANotes;
    public JCheckBox cBChronicDiseases;
    public JCheckBox cBMedicalTreatment;
    public JCheckBox cBAllergies;
    public JCheckBox cBSpecialNeeds;
    public JCheckBox cBTakingMedications;
    public JPanel mainChildrenPanel;


    public static ChildrenPanel main(CacheManager cacheManager) {
        ChildrenPanel.cacheManager = cacheManager;
        return new ChildrenPanel();
    }

    private void createUIComponents() {
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        cBClassroom = new JComboBox();

        cBClassroom = new JComboBox(cacheManager.classrooms.values().toArray());
        cBClassroom.setSelectedIndex(-1);

        dMBirthday = new UtilDateModel();
        datePickerBirthday = new JDatePickerImpl(new JDatePanelImpl(dMBirthday, p), new DateLabelFormatter());

        dMFirstDayComundi = new UtilDateModel();
        datePickerFirstDayComundi = new JDatePickerImpl(new JDatePanelImpl(dMFirstDayComundi, p), new DateLabelFormatter());

        dMFirstDayCDB = new UtilDateModel();
        datePickerFirstDayCDB = new JDatePickerImpl(new JDatePanelImpl(dMFirstDayCDB, p), new DateLabelFormatter());

        dMFirstDayPrimary = new UtilDateModel();
        datePickerFirstDayPrimary = new JDatePickerImpl(new JDatePanelImpl(dMFirstDayPrimary, p), new DateLabelFormatter());

        dMExitDay = new UtilDateModel();
        datePickerExitDay = new JDatePickerImpl(new JDatePanelImpl(dMExitDay, p), new DateLabelFormatter());

    }
}
