package ui.formReports;

import bd.BDManager;
import ui.components.DateLabelFormatter;
import ui.formReports.managers.ReportManager;
import ui.formReports.models.Pdf_EoY_Reports;
import utils.CacheManager;
import utils.MyLogger;
import utils.SettingsManager;
import utils.data.RawData;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

public class ReportsForm {
    private static final String TAG = ReportsForm.class.getSimpleName();
    private static BDManager bdManager;
    private static SettingsManager settingsManager;
    private static CacheManager cacheManager;
    private JPanel mainPanel;
    private JList<String> listClassrooms;
    private JList<String> listStudents;
    private JCheckBox checkBoxRecord;
    private JCheckBox checkBoxEmail;
    private JButton buttonGenerateReports;
    private JTextArea tALegend;
    private JTextArea tADoneWell;
    private JTextArea tAEvenBetter;
    private JTextArea tATask;
    private JButton buttonSaveYet;
    private JButton buttonLoadYet;
    private JCheckBox checkBoxYet;
    private JCheckBox checkBoxTargets;
    private JCheckBox checkBoxClassroom;
    private JProgressBar progressBar;
    private JButton buttonOpenFolder;
    private JCheckBox checkContacts;
    private JTextField textFieldSubject;
    private JTextArea textAreaBody;
    private JButton buttonLoadEoY;
    private JButton buttonSaveEoY;
    private JTabbedPane tabbedPane1;
    private JTextArea tAEoY1;
    private JTextArea tAEoY2;
    private JTextArea tAEoY3;
    private JTextArea tAEoY4;
    private JTextArea tAEoY5;
    private JTextArea tAEoY6;
    private JTextArea tAEoY7;
    private JTextArea tAEoY8;
    private JTextArea tAEoY9;
    private JTextArea tAEoY10;
    private JTextArea tAEoY11;
    private JTextArea tAEoY12;
    private JTextArea tAEoY13;
    private JTextArea tAEoY14;
    private JTextArea tAEoY15;
    private JTextArea tAEoY16;
    private JButton buttonGenerateEOY;
    private JButton buttonOpenFolderEoY;
    private JButton buttonGenerateTargets;
    private JCheckBox checkBoxEoY;
    private JCheckBox checkBoxEoYPhotos;
    private JCheckBox checkBoxEoYComments;
    private JCheckBox checkBoxDrive;
    private JList<String> listLog;
    private UtilDateModel dateModelMultiple;
    private UtilDateModel dateModelYet;
    private UtilDateModel dateModelEoY;
    private JDatePickerImpl datePickerYet;
    private JDatePickerImpl datePickerEoY;
    private JDatePickerImpl datePickerMultiple;
    private JTextArea tAEoY0;
    private ArrayList<Integer> students;
    BufferedImage logo;
    public static Boolean yetChanged = false;
    public static Boolean eoyChanged = false;
    private Integer currentClassroom = null;
    private Integer currentStudent = null;
    Boolean second = false;
    private static JFrame frame;
    private Properties p;
    ArrayList<JTextArea> textAreasEoY;
    public ReportsForm() {

    }

    public static JPanel main(SettingsManager settingsManager, BDManager bdManager, CacheManager cacheManager, JFrame frame) {
        ReportsForm.frame = frame;
        ReportsForm.bdManager = bdManager;
        ReportsForm.settingsManager = settingsManager;
        ReportsForm.cacheManager = cacheManager;
        ReportsForm form = new ReportsForm();
        return form.mainPanel;
    }

    private void createUIComponents() {
        textFieldSubject = new JTextField();
        textFieldSubject.setText(ReportManager.header);
        textAreaBody = new JTextArea();
        textAreaBody.setText(ReportManager.body);

        p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");

        try {
            logo = ImageIO.read(getClass().getResourceAsStream("logo.png"));
        } catch (Exception e) {
            MyLogger.e(TAG, e);
        }

        progressBar = new JProgressBar();
        listClassrooms = new JList<>(RawData.classrooms);
        listStudents = new JList<>(new DefaultListModel<>());
        students = new ArrayList<>();
        listClassrooms.addListSelectionListener(listSelectionEvent -> {
            if (listSelectionEvent.getValueIsAdjusting()) return;
            if (yetChanged && !second) {
                second = true;
                JOptionPane.showMessageDialog(mainPanel, "Please save before leaving");
                if (currentClassroom != null) listClassrooms.setSelectedIndex(currentClassroom-1);
                return;
            } else {
                DefaultListModel<String> model = (DefaultListModel<String>)listStudents.getModel();
                model.clear();

                currentClassroom = listClassrooms.getSelectedIndex() + 1;

                students.clear();
                ArrayList<Integer> listStudents = cacheManager.studentsperclassroom.get(currentClassroom);
                if (listStudents != null)
                    for (Integer id : listStudents) {
                        students.add(id);
                        model.addElement((String)cacheManager.students.get(id)[0]);
                    }
            }
            second = false;
        });
        listStudents.addListSelectionListener(listSelectionEvent -> {
            if (listSelectionEvent.getValueIsAdjusting()) {
                if (yetChanged && !second) {
                    second = true;
                    JOptionPane.showMessageDialog(mainPanel, "Please save before leaving");
                    if (currentStudent != null) listStudents.setSelectedIndex(currentStudent);
                    return;
                }
            }
            second = false;
        });

        createYetTab();
        createEOYTab();
        createReportGeneratorTab();
    }

    private void createReportGeneratorTab(){
        listLog = new JList<>(new DefaultListModel<>());
        dateModelMultiple = new UtilDateModel();
        JDatePanelImpl datePanel = new JDatePanelImpl(dateModelMultiple, p);
        datePickerMultiple = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        dateModelMultiple.setValue(new Date());

        buttonGenerateReports = new JButton();
        buttonGenerateReports.addActionListener(actionEvent -> {
            int index = listClassrooms.getSelectedIndex();
            int studentIndex = listStudents.getSelectedIndex();
            if (index != -1) {
                Integer student = (checkBoxClassroom.isSelected()) ? null : students.get(studentIndex);
                SwingReportGenerator generator = new SwingReportGenerator(bdManager, cacheManager,
                        settingsManager, frame, student, index+1, dateModelMultiple.getValue(), null,
                        checkBoxRecord.isSelected(), checkBoxEmail.isSelected(), checkBoxYet.isSelected(),
                        checkBoxTargets.isSelected(), checkBoxEoY.isSelected(), checkBoxEoYComments.isSelected(),
                        checkBoxEoYPhotos.isSelected(), checkContacts.isSelected(), checkBoxDrive.isSelected(),
                        textFieldSubject.getText(), textAreaBody.getText(), progressBar,
                        (DefaultListModel<String>) listLog.getModel());
                generator.addPropertyChangeListener(generator);
                generator.execute();
            }
        });

        buttonOpenFolder = new JButton();
        buttonOpenFolder.addActionListener(actionEvent -> {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.open(new File(settingsManager.getValue(SettingsManager.REPORTS_DIR)));
                } catch (IOException e) {
                    MyLogger.e(TAG, e);
                }
            }
        });
    }

    private void createYetTab() {
        dateModelYet = new UtilDateModel();
        JDatePanelImpl datePanel1 = new JDatePanelImpl(dateModelYet, p);
        datePickerYet = new JDatePickerImpl(datePanel1, new DateLabelFormatter());
        dateModelYet.setValue(new Date());

        buttonLoadYet = new JButton();
        buttonLoadYet.addActionListener(actionEvent -> {
            if (yetChanged) {
                JOptionPane.showMessageDialog(mainPanel, "Please save before leaving");
                return;
            }
            currentClassroom = listClassrooms.getSelectedIndex();
            currentStudent = listStudents.getSelectedIndex();
            if (currentClassroom == -1 || currentStudent == -1) {
                JOptionPane.showMessageDialog(mainPanel, "Please select classroom and student");
                return;
            }
            SwingDBYetUpdater updater = new SwingDBYetUpdater(bdManager, cacheManager, settingsManager, tALegend,
                    tADoneWell, tAEvenBetter, tATask, new java.sql.Date(dateModelYet.getValue().getTime()),
                    currentClassroom+1, students.get(currentStudent));
            updater.setLoad();
            updater.execute();
        });

        buttonSaveYet = new JButton();
        buttonSaveYet.addActionListener(actionEvent -> {
            currentClassroom = listClassrooms.getSelectedIndex();
            currentStudent = listStudents.getSelectedIndex();
            if (currentClassroom == -1 || currentStudent == -1) {
                JOptionPane.showMessageDialog(mainPanel, "Please select classroom and student");
                return;
            }
            SwingDBYetUpdater updater = new SwingDBYetUpdater(bdManager, cacheManager, settingsManager, tALegend,
                    tADoneWell, tAEvenBetter, tATask, new java.sql.Date(dateModelYet.getValue().getTime()),
                    currentClassroom + 1, students.get(currentStudent));
            updater.setSave();
            updater.execute();
            yetChanged = false;
        });

        buttonGenerateTargets = new JButton();
        buttonGenerateTargets.addActionListener(actionEvent -> {
            int index = listClassrooms.getSelectedIndex();
            int studentIndex = listStudents.getSelectedIndex();
            if (index != -1) {
                Integer student = (checkBoxClassroom.isSelected()) ? null : students.get(studentIndex);
                SwingReportGenerator generator = new SwingReportGenerator(bdManager, cacheManager,
                        settingsManager, frame, student, index+1, dateModelYet.getValue(), null,
                        false, false, false, true, false,
                        false, false, false, false,null,
                        null, progressBar, (DefaultListModel<String>) listLog.getModel());
                generator.addPropertyChangeListener(generator);
                generator.execute();
            }
        });
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        tALegend = new JTextArea();
        tALegend.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { yetChanged = true; }
        });
        tALegend.addMouseListener(new MyPopupListener());
        tALegend.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        tADoneWell = new JTextArea();
        tADoneWell.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { yetChanged = true; }
        });
        tADoneWell.addMouseListener(new MyPopupListener());
        tADoneWell.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        tAEvenBetter = new JTextArea();
        tAEvenBetter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { yetChanged = true; }
        });
        tAEvenBetter.addMouseListener(new MyPopupListener());
        tAEvenBetter.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        tATask = new JTextArea();
        tATask.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { yetChanged = true; }
        });
        tATask.addMouseListener(new MyPopupListener());
        tATask.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

    }

    private void createEOYTab() {
        dateModelEoY = new UtilDateModel();
        JDatePanelImpl datePanel2 = new JDatePanelImpl(dateModelEoY, p);
        datePickerEoY = new JDatePickerImpl(datePanel2, new DateLabelFormatter());
        dateModelEoY.setValue(new Date());
        textAreasEoY = new ArrayList<>();

        buttonLoadEoY = new JButton();
        buttonLoadEoY.addActionListener(actionEvent -> {
            if (eoyChanged) {
                JOptionPane.showMessageDialog(mainPanel, "Please save before leaving");
                return;
            }
            currentClassroom = listClassrooms.getSelectedIndex();
            currentStudent = listStudents.getSelectedIndex();
            if (currentClassroom == -1 || currentStudent == -1) {
                JOptionPane.showMessageDialog(mainPanel, "Please select classroom and student");
                return;
            }

            SwingDBEOYUpdater updater = new SwingDBEOYUpdater(bdManager, cacheManager, settingsManager, textAreasEoY,
                    new java.sql.Date(dateModelEoY.getValue().getTime()), currentClassroom+1,
                    students.get(currentStudent));
            updater.setLoad();
            updater.execute();
        });

        buttonSaveEoY = new JButton();
        buttonSaveEoY.addActionListener(actionEvent -> {
            currentClassroom = listClassrooms.getSelectedIndex();
            currentStudent = listStudents.getSelectedIndex();
            if (currentClassroom == -1 || currentStudent == -1) {
                JOptionPane.showMessageDialog(mainPanel, "Please select classroom and student");
                return;
            }
            SwingDBEOYUpdater updater = new SwingDBEOYUpdater(bdManager, cacheManager, settingsManager, textAreasEoY,
                    new java.sql.Date(dateModelEoY.getValue().getTime()), currentClassroom + 1,
                    students.get(currentStudent));
            updater.setSave();
            updater.execute();
            eoyChanged = false;
        });

        buttonOpenFolderEoY = new JButton();
        buttonOpenFolderEoY.addActionListener(actionEvent -> {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.open(new File(settingsManager.getValue(SettingsManager.REPORTS_DIR)));
                } catch (IOException e) {
                    MyLogger.e(TAG, e);
                }
            }
        });

        buttonGenerateEOY = new JButton();
        buttonGenerateEOY.addActionListener(e -> {
            java.sql.Date date = new java.sql.Date(dateModelEoY.getValue().getTime());
            Integer currentStudent = students.get(listStudents.getSelectedIndex());
            ReportManager reportManager = new ReportManager(cacheManager, date, currentStudent);
            Pdf_EoY_Reports report = new Pdf_EoY_Reports(bdManager, null, cacheManager, settingsManager,
                    currentStudent, listClassrooms.getSelectedIndex() + 1, date, logo, reportManager,
                    checkBoxEoYComments.isSelected(), checkBoxEoYPhotos.isSelected(),
                    (DefaultListModel<String>) listLog.getModel());
        });

        tAEoY0 = new JTextArea();
        tAEoY0.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY0.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY0);

        tAEoY1 = new JTextArea();
        tAEoY1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY1.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY1);

        tAEoY2 = new JTextArea();
        tAEoY2.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY2.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY2);

        tAEoY3 = new JTextArea();
        tAEoY3.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY3.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY3);

        tAEoY4 = new JTextArea();
        tAEoY4.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY4.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY4);

        tAEoY5 = new JTextArea();
        tAEoY5.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY5.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY5);

        tAEoY6 = new JTextArea();
        tAEoY6.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY6.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY6);

        tAEoY7 = new JTextArea();
        tAEoY7.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY7.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY7);

        tAEoY8 = new JTextArea();
        tAEoY8.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY8.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY8);

        tAEoY9 = new JTextArea();
        tAEoY9.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY9.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY9);

        tAEoY10 = new JTextArea();
        tAEoY10.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY10.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY10);

        tAEoY11 = new JTextArea();
        tAEoY11.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY11.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY11);

        tAEoY12 = new JTextArea();
        tAEoY12.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY12.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY12);

        tAEoY13 = new JTextArea();
        tAEoY13.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY13.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY13);

        tAEoY14 = new JTextArea();
        tAEoY14.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY14.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY14);

        tAEoY15 = new JTextArea();
        tAEoY15.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY15.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY15);

        tAEoY16 = new JTextArea();
        tAEoY16.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { eoyChanged = true; }
        });
        tAEoY16.addMouseListener(new MyPopupListener());
        textAreasEoY.add(tAEoY16);
    }

}
